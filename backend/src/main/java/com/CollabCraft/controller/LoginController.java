package com.CollabCraft.controller;

import com.CollabCraft.entity.UserInfo;
import com.CollabCraft.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class LoginController {
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping(value = "/login")
    @ResponseBody
    public String login(@RequestParam(value = "username") String username,
                        @RequestParam(value = "password") String password) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        String result = "";
        List<UserInfo> account = userInfoService.getUserByName(username);
        System.out.println(account.size());

                if(account.size() == 0)
                {
                    result = "false";
                } else {
                    if (account.get(0).getPassword().equals(password))
                    {
                        result = "true";
                        map.put("email", account.get(0).getUserEmail());
                    } else
                    {
                        result = "false";
            }
        }
        map.put("isValidate",result);

        {
            try {
                json = mapper.writeValueAsString(map);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return json;
    }

    @RequestMapping(value = "/modifyKey")
    @ResponseBody
    public String modifyKey(@RequestParam(value = "username") String username,
                        @RequestParam(value = "oldPassword") String oldPassword,
                            @RequestParam(value = "newPassword") String newPassword) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        String result = "";
        List<UserInfo> account = userInfoService.getUserByName(username);

        if(account.size() == 0)
        {
            result = "fail";
        } else {
            if (account.get(0).getPassword().equals(oldPassword))
            {
                if(userInfoService.modifyKey(username,newPassword))
                {
                    result = "success";
                } else
                {
                    result = "fail";
                }
            } else
            {
                result = "fail";
            }
        }
        map.put("message",result);

        {
            try {
                json = mapper.writeValueAsString(map);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return json;
    }
}
