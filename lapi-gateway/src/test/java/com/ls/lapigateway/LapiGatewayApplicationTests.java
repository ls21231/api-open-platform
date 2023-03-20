package com.ls.lapigateway;

import com.ls.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LapiGatewayApplicationTests {

    @DubboReference
    private DemoService demoService;

    @Test
    void contextLoads() {
        String hello = demoService.hello();
        System.out.println(hello);
    }

}
