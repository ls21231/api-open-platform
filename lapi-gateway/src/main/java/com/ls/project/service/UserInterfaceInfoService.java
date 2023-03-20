package com.ls.project.service;

/**
 *
 */
public interface UserInterfaceInfoService {
    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    Boolean invokeCount(long interfaceInfoId,long userId);
}
