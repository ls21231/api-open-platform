package com.ls.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ls.project.model.entity.InterfaceInfo;

/**
* @author 111
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-03-11 17:08:22
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);


    /**
     * dubbo远程调用的方法，未进行抽离
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
}
