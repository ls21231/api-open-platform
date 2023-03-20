package com.ls.lapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;

import com.ls.lapiclientsdk.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.ls.lapiclientsdk.utils.SignUtils.getSign;


/**
 *
 * @author ls
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LSClient {

    public static final String GATEWAY_HOST = "http://localhost:8090";
    String accessKey = "liushun";
    String secretKey = "abcdefgh";

    private Map<String, String> getHeaders(String body) {

        Map<String,String> hashMap = new HashMap<>();
        hashMap.put("accessKey",accessKey);
        //一定不能发给后端
        //headers.put("secretKey",secretKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body",body);
        hashMap.put("timestamp ",String.valueOf(System.currentTimeMillis()/ 1000));
        hashMap.put("sign", getSign(body,secretKey));
        return hashMap;
    }
    /**
     * http调用接口服务
     * @param user
     * @return
     */
    public String getUserName(User user) {
        String json = JSONUtil.toJsonStr(user);
        String result = HttpRequest.post(GATEWAY_HOST + "/api/name/json")
                .addHeaders(getHeaders(json))
                .body(json)
                .execute().body();

        return result;
    }


}
