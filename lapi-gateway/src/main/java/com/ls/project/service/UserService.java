package com.ls.project.service;

import com.ls.project.entity.User;

/**
 *
 */
public interface UserService {
    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
