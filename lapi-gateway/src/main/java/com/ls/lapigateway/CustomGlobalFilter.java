package com.ls.lapigateway;

import com.ls.lapiclientsdk.utils.SignUtils;
import com.ls.project.entity.InterfaceInfo;
import com.ls.project.entity.User;
import com.ls.project.service.InterfaceInfoService;
import com.ls.project.service.UserInterfaceInfoService;
import com.ls.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    public static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    @DubboReference
    private UserService userService;

    @DubboReference
    private UserInterfaceInfoService userInterfaceInfoService;

    @DubboReference
    private InterfaceInfoService interfaceInfoService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*
        1. 用户发送请求到API网关(√)
        2. 请求日志(√)
        3. 黑白名单(√)
        4. 用户鉴权（判断ak，sk是否合法）
        5. 请求的模拟接口是否存在？
        6. 请求转发，调用模拟接口
        7. 响应日志
        8. 调用成功，接口调用次数+1
        9. 调用失败，返回规范错误码*/


        // 2.打日志

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        System.out.println("请求唯一标识" + request.getId());
        System.out.println("请求路径" + path);
        System.out.println("请求方法" + method);
        System.out.println("请求唯一参数" + request.getQueryParams());
        System.out.println("请求来源地址" + request.getRemoteAddress().getHostName());

        // 3. 黑白名单
        // if(!IP_WHITE_LIST.contains(request.getRemoteAddress().getHostName())) {
        //     ServerHttpResponse response = exchange.getResponse();
        //     // 设置状态码
        //     response.setStatusCode(HttpStatus.FORBIDDEN);
        //     // 表示结束请求
        //     response.setComplete();
        // }

        // 4. 用户鉴权（判断ak，sk是否合法）
        // 通过请求头拿取参数
        HttpHeaders headers = request.getHeaders();

        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");
        // 客户端经过加密算法传过来的签名
        String sign = headers.getFirst("sign");
        //  去数据库查询数据是否给该用户分配了ak，sk
        User invokeUser = null;
         try{
             invokeUser = userService.getInvokeUser(accessKey);
         }catch (Exception e) {
             log.info("get invokeUser error",e);
         }
         if(invokeUser == null) {
             return handleNoAuth(exchange.getResponse());
         }
        //  还可以对时间戳和随机数相应的校验
        //  secretKey 同样是从数据中查出来
        String secreyKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getSign(body, secreyKey);
        if(sign == null || !sign.equals(serverSign)) {
            handleNoAuth(exchange.getResponse());
        }
        // 5. 请求的模拟接口是否存在?
        //  从数据库查询该接口的状态，使用Dubbo RPC 调用api项目中的增删改查接口
        InterfaceInfo interfaceInfo = interfaceInfoService.getInterfaceInfo(path, method);
        // TODO 请求路径没有做处理 无法和数据库的数据对应起来 导致查询出来为空  小问题
        if(interfaceInfo == null) {
            //return handleNoAuth(exchange.getResponse());
        }

        /**
         * 这段代码因为异步返回的问题，本质上是在对响应判断过后再去执行真正的接口调用
         * 和想象的先调用真实的接口再对响应进行判断是不符合的，所以用到一个装饰器模式对响应进行了装饰，在拿到真正的响应结果之后再
         * 对相应进行判断，比如调用invokeCount方法对调用次数增加1的操作和响应日志的打印
         */
        /*6.请求转发，调用模拟接口
        Mono<Void> filter = chain.filter(exchange);
        7. 响应日志
        log.info("响应日志");
        8. 调用成功，接口调用次数+1
         同样使用Dubbo调用后台接口

        if(exchange.getResponse().getStatusCode() == HttpStatus.OK) {

        } else {
            // 9.调用失败，返回错误码
            handleInvokeError(exchange.getResponse());
         return filter;
        }*/
        return handleResponse(exchange,chain,interfaceInfo.getId(),invokeUser.getId());
    }

    /**
     * 处理响应
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,long interfaceInfoId,long userId){
        try {
            //从交换寄拿响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            //缓冲区工厂，拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if(statusCode == HttpStatus.OK){
                //装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    //等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        //对象是响应式的
                        if (body instanceof Flux) {
                            //我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //往返回值里面写数据
                            //拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 8. 调用成功，  接口调用次数+1 invokeCount
                                        userInterfaceInfoService.invokeCount(interfaceInfoId,userId);
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8);//data
                                        sb2.append(data);
                                        //打印日志
                                        log.info("响应结果" + data);
                                        log.info("这是真实接口调用之后的响应日志");
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 9. 调用失败，返回规范错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }

    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    private Mono handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}