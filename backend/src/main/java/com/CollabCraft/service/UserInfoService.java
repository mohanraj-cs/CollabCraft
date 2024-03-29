package com.CollabCraft.service;

import com.CollabCraft.entity.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("UserInfoService")
public interface UserInfoService {
    List<UserInfo> getUserByName(String username);
    boolean addUser(String username,String password, String email);
    List<UserInfo> getUserByID(int userID);
    List<UserInfo> getAllUser();
    boolean modifyKey(String username,String password);
}