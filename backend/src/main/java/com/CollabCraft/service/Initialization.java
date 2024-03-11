package com.CollabCraft.service;

import com.CollabCraft.file.FileList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import com.CollabCraft.file.FileListInstance;

@Service
public class Initialization implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception{
        FileListInstance.FopList=new FileList();
    }

}
