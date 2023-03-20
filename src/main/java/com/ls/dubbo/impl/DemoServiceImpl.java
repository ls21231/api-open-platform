package com.ls.dubbo.impl;

import com.ls.dubbo.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *
 */
@DubboService
public class DemoServiceImpl implements DemoService {
    @Override
    public String hello() {
        System.out.println("你好 世界");

        return "你好,远程调用成功,这是调用返回";
    }
}
