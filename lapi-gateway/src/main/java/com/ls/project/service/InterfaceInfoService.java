package com.ls.project.service;

import com.ls.project.entity.InterfaceInfo;

/**
 *
 */
public interface InterfaceInfoService {
    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     */
    InterfaceInfo getInterfaceInfo(String url, String method);
}
