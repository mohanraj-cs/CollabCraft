package com.CollabCraft.dao;

import com.CollabCraft.entity.UserInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface UserInfoMapper extends Mapper<UserInfo> {
    Integer addUserInfo(@Param("username") String username,@Param("password") String password, @Param("email") String email);
    Integer modifyKey(@Param("username") String username,@Param("password") String password);
}