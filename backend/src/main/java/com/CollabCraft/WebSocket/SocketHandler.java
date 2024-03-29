package com.CollabCraft.WebSocket;
import java.io.IOException;
import java.util.*;

import com.CollabCraft.controller.VersionController;
import com.CollabCraft.entity.UserInfo;
import com.CollabCraft.file.*;
import com.CollabCraft.service.UserInfoService;
import com.CollabCraft.utils.CommonOperation;
import com.CollabCraft.utils.LineSeperatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;

import javax.servlet.ServletContext;


@Service
public class SocketHandler implements WebSocketHandler{
    @Autowired
    UserInfoService userInfoService;

    class Task extends Thread {
        private boolean cancelled;
        private int timeInterval;
        private List<String> content;
        private String newPath;
        private int userID;

        public Task(String newPath,int userID){
            this.cancelled = false;
            this.timeInterval = 1000;
            this.newPath=newPath;
            this.userID=userID;
            this.content=null;
        }

        private void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run() {
            while (!cancelled) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                ObjectMapper mapper = new ObjectMapper();
                String json = "";
                String result = "";
                log.info("\n user " + userID + "timer use \n" + this.content);
                if (this.content == null) {
                    this.content = new FileRow(this.newPath).readFile();
                    for (int i = 0; i < this.content.size(); i++) {
                        result += this.content.get(i);
                        if (i < this.content.size() - 1) {
                            result += '\n';
                        }
                    }
                    map.put("content", result);

                    try {
                        json = mapper.writeValueAsString(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendContent(this.userID, new TextMessage(json));
                } else {
                    List<String> content_t = new FileRow(this.newPath).readFile();
                    log.info("\n user " + userID + "timer use \n" + this.content + '\n' + content_t);
                    boolean diff = false;
                    if (this.content.size() == content_t.size()) {
                        for (int i = 0; i < this.content.size(); i++) {
                            if (!this.content.get(i).equals(content_t.get(i))) {
                                diff = true;
                                break;
                            }
                        }
                    } else {
                        diff = true;
                    }
                    if (diff) {
                        this.content = content_t;
                        for (int i = 0; i < this.content.size(); i++) {
                            result += this.content.get(i);
                            if (i < this.content.size() - 1) {
                                result += '\n';
                            }
                        }
                        map.put("content", result);

                        try {
                            json = mapper.writeValueAsString(map);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendContent(this.userID, new TextMessage(json));
                    }
                }
                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private final static Logger log=Logger.getLogger(SocketHandler.class);
    private final static Map<Integer,WebSocketSession> userMap;
    private final static Map<Integer,Task> userTask;

    static {
        userMap = new HashMap<Integer,WebSocketSession>();
        userTask = new HashMap<Integer,Task>();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Websocket："+session.getId());
        int userId = this.getUserId(session);
        if(userMap.get(userId) != null){
            userTask.get(userId).cancel();
            userTask.remove(userId);
            userMap.remove(userId);
            log.info("Websocket："+userId);
        }

        String docName = session.getAttributes().get("docName").toString();
        String docOwner = session.getAttributes().get("docOwner").toString();


        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        ServletContext servletContext = webApplicationContext.getServletContext();
        String realPath = servletContext.getRealPath("/fileUpload");

        char sep= LineSeperatorUtil.getLineSeperator();

        String path = realPath + sep + docOwner + sep + "create" + sep + docName;
        //FileUtils.copyFile(new File(CommonOperation.getNewFilePathbyOldFilePath(path)),new File(path));
        VersionController.addVersion_atom(path);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("Socket connection successfully established\n"));
        int userId=this.getUserId(session);
        if(userMap.get(userId) == null){
            userMap.put(userId, session);
            log.info("Websocket："+userId+"User establishes connection successfully\n！");
        }else{
            log.info("Websocket："+userId+"User is already connected！");
        }
        String docName = session.getAttributes().get("docName").toString();
        String username = session.getAttributes().get("username").toString();
        String docOwner = session.getAttributes().get("docOwner").toString();

        //获取项目真实路径
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        ServletContext servletContext = webApplicationContext.getServletContext();
        String realPath = servletContext.getRealPath("/fileUpload");

        char sep= LineSeperatorUtil.getLineSeperator();

        String path = realPath + sep + docOwner + sep + "create" + sep + docName;

        Task task = new Task(
                CommonOperation.getNewFilePathbyOldFilePath(path) ,userId);
        task.start();
        userTask.put(userId,task);

    }


    /**
     * Accept messages sent by clients
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Map maps = (Map)JSON.parse(message.getPayload().toString());
        String json = conflictHandle(maps.get("opList").toString(),maps.get("newPath").toString(),maps.get("oldPath").toString()
                ,maps.get("username").toString(),maps.get("timeStamp").toString());
        List<UserInfo> user = userInfoService.getUserByName(maps.get("username").toString());
        sendContent(user.get(0).getIduser(),new TextMessage(json));
        //session.sendMessage(message);
        log.info("Websocket：handleMessage send message："+message);
    }


    /**
     * This method is called when an exception is sent
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) throws Exception {
        log.error("Websocket：handleMessage send message："+error);
    }


    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    public void sendContent(int user_id, TextMessage message) {
        log.info("User "+user_id+"send "+message.getPayload().toString());
        WebSocketSession session = userMap.get(user_id);
        if(session !=null && session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getUserId(WebSocketSession session){
        try {
            List<UserInfo> user = userInfoService.getUserByName((String)session.getAttributes().get("username"));
            return user.get(0).getIduser();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private SaveEditVersion saveEditVersion = new SaveEditVersion();

    private String conflictHandle( String opList,//Insertion/deletion, number of rows, position, content, whether to insert/delete by new row

                                   String newPath,
                                  String oldPath,
                                  String username,
                                  String timeStamp) {

        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        String result="";

        ArrayList<String> content=new ArrayList<String>();

        if (opList!=""){
            log.info(opList+'\n'+newPath+'\n'+oldPath+'\n'+username+'\n'+timeStamp+'\n');
            FileList fileList = FileListInstance.FopList;
            FileOperation Fop= fileList.getFop(newPath);
            boolean has_user = Fop.insertUser_index(username);
            FileRow file=new FileRow(newPath);
            content=file.readFile();

            String[] operationList=opList.split("\\.");

            try{
                for (int i=0;i<operationList.length;i++){
                    String[] s = (operationList[i] + "," + username+ "," + timeStamp ).split(",");
                    s[1]=String.valueOf(Integer.valueOf(s[1])-1);
                    //s = Fop.merge(s);
                    content=Fop.update_doc(s,content);

                    if (has_user){
                        Fop.deleteLastUserRecord(username);
                    }
                    Fop.addOptable(s);
                    //Record user atomic operations
                    saveEditVersion.addEdit(s,oldPath);
                    Fop.setUser_index(username, Fop.getOptable().size());
                    has_user = true;
                }
                file.writeFile(content);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        content=new FileRow(newPath).readFile();
        for (int i=0;i<content.size();i++){
            result+=content.get(i);
            if (i<content.size()-1){
                result+='\n';
            }
        }
        map.put("content",result);
        //Convert HashMap of all data to json
        try {
            json = mapper.writeValueAsString(map);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }


}