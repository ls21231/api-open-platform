package com.ls.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ls.project.model.entity.UserInterfaceInfo;

/**
* @author 111
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service
* @createDate 2023-03-14 19:49:13
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 对请求参数进行校验
     * @param userInterfaceInfo
     * @param b
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    Boolean invokeCount(long interfaceInfoId,long userId);
}
