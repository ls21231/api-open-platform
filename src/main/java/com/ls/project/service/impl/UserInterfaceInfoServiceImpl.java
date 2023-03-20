package com.ls.project.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ls.project.common.ErrorCode;
import com.ls.project.exception.BusinessException;
import com.ls.project.model.entity.UserInterfaceInfo;
import com.ls.project.service.UserInterfaceInfoService;
import com.ls.project.mapper.UserInterfaceInfoMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
* @author 111
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service实现
* @createDate 2023-03-14 19:49:13
*/
@Service
@DubboService
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b) {
     Long id = userInterfaceInfo.getId();
     Long userId = userInterfaceInfo.getUserId();
     Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
     Integer totalNum = userInterfaceInfo.getTotalNum();
     Integer leftNum = userInterfaceInfo.getLeftNum();
     Integer status = userInterfaceInfo.getStatus();
     Date createTime = userInterfaceInfo.getCreateTime();
     Date updateTime = userInterfaceInfo.getUpdateTime();
     Integer isDelete = userInterfaceInfo.getIsDelete();

     // 定义自己的校验规则

    }

    @Override
    public Boolean invokeCount(long interfaceInfoId, long userId) {

        if(interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId",interfaceInfoId);
        updateWrapper.eq("userId",userId);
        updateWrapper.setSql("leftNum = leftNum - 1 ,totalNum  = totalNum + 1");
        return this.update(updateWrapper);
    }
}




