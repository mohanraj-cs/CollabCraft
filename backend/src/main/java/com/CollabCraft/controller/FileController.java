package com.CollabCraft.controller;

import com.CollabCraft.file.FileList;
import com.CollabCraft.file.FileListInstance;
import com.CollabCraft.file.FileRow;
import com.CollabCraft.utils.ColorUtil;
import com.CollabCraft.utils.CommonOperation;
import com.CollabCraft.utils.LineSeperatorUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.CollabCraft.entity.Cooperate;
import com.CollabCraft.entity.Document;
import com.CollabCraft.entity.UserInfo;
import com.CollabCraft.service.CooperateService;
import com.CollabCraft.service.DocumentService;
import com.CollabCraft.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class FileController {
    @Autowired
    DocumentService documentService;

    @Autowired
    CooperateService cooperateService;

    @Autowired
    UserInfoService userInfoService;

    @RequestMapping(value = "/delete")
    @ResponseBody
    public String delete(@RequestParam(value = "oldPath") String oldPath,
                        @RequestParam(value = "newPath") String newPath) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        String result = "";

        try{
            Files.deleteIfExists(Paths.get(oldPath));
            Files.deleteIfExists(Paths.get(newPath));
            Files.deleteIfExists(Paths.get(CommonOperation.getLogPath(oldPath)));
            Files.deleteIfExists(Paths.get(CommonOperation.getEditLogByFilePath(oldPath)));

//            File oldFile = new File(oldPath);
//            File newFile = new File(newPath);
//            boolean d1 = oldFile.delete();
//            boolean d2 = newFile.delete();
//
//            System.gc();
//            String logPath = CommonOperation.getLogPath(oldPath);
//            File logFile = new File(logPath);
//            boolean d3 = logFile.delete();
//
//            System.gc();
//            String editPath = CommonOperation.getEditLogByFilePath(oldPath);
//            File editFile = new File(editPath);
//            boolean d4 = editFile.delete();

            List<Document> doc=documentService.getDocumentbyFilePath(oldPath.replace("\\","/"));

            int successDeleteCooperate = cooperateService.deleteCooperatebyDocumentID(doc.get(0).getIddocument());
            int successDeleteDocument = documentService.deleteDocumentbyID(doc.get(0).getIddocument());

            FileList fList = FileListInstance.FopList;
            boolean s = fList.deleteFop(newPath);

            if (successDeleteCooperate>0 && successDeleteDocument>0 && s){
                result="success";
            }
            else{
                result="fail";
            }
        }catch (Exception e){
            result="fail";
        }

        map.put("message",result);
        try {
            json = mapper.writeValueAsString(map);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    @RequestMapping(value = "/upload")
    @ResponseBody
    public String upload(@RequestParam(value = "username") String username,
                         @RequestParam(value = "file") MultipartFile fileUpload,
                         @RequestParam(value = "fileUploadFileName") String fileUploadFileName) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        String result = "";


        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        ServletContext servletContext = webApplicationContext.getServletContext();
        String realPath = servletContext.getRealPath("/fileUpload");


        char sep=LineSeperatorUtil.getLineSeperator();
        //char sep='/';

        try{
//            CommonsMultipartFile cFile = (CommonsMultipartFile) fileUpload;
//            DiskFileItem fileItem = (DiskFileItem) cFile.getFileItem();
//            InputStream inputStream = fileItem.getInputStream();


            File log=new File(realPath+sep+username+sep+"log");
            log.mkdirs();

            String oldPath=realPath+sep+ username + sep+"create"+sep + fileUploadFileName;
            File uploadFile=new File(oldPath);
            fileUpload.transferTo(uploadFile);

            //FTPUtils.uploadFile(oldPath,fileUploadFileName,inputStream);

            String newPath=CommonOperation.getNewFilePathbyOldFilePath(oldPath);

            //FTPUtils.uploadFile(newPath,fileUploadFileName,inputStream);
            File logFile=new File(newPath);
            fileUpload.transferTo(logFile);

            FileList fileList = FileListInstance.FopList;
            fileList.createFop(newPath,oldPath);

            //FileUtils.copyFile(ftemp,new File(realPath+"\\"+ username + "\\create\\" + fileUploadFileName));
            //FileUtils.copyFile(ftemp,new File(realPath+"\\"+ username + "\\create\\" + s[0]+"_t."+s[1]));

            Date date = new Date();

            int addDocumentSuccess=documentService.addDocument(
                    fileUploadFileName,date,date,
                    realPath.replace("\\","/")+ "/"+username + "/create/" + fileUploadFileName,
                    realPath.replace("\\","/")+ "/"+username + "/log/" + fileUploadFileName);

            List<UserInfo> user=userInfoService.getUserByName(username);

            List<Document> doc = documentService.getDocumentbyFilePath(
                    realPath.replace("\\","/")+"/"+username + "/create/" + fileUploadFileName);

            int addCoopSuccess=cooperateService.addCooperate("own",user.get(0).getIduser(),doc.get(0).getIddocument());
            if (addDocumentSuccess==1 && addCoopSuccess==1){
                result="success";
            }
            else{
                result="fail";
            }

        }catch (Exception e){
            e.printStackTrace();
        }


        map.put("message",result);

        try {
            json = mapper.writeValueAsString(map);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    @RequestMapping(value = "/download")
    @ResponseBody
    public String download(@RequestParam(value = "path") String path) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        List<String> content=new ArrayList<>();
        String fileName = CommonOperation.getFilename(path);
        content = new FileRow(path).readFile();

        String result="";
        for (String c:content){
            result+=c+"\n";
        }

        map.put("fileName",fileName);
        map.put("content",result);

        try {
            json = mapper.writeValueAsString(map);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    @RequestMapping(value = "/edit")
    @ResponseBody
    public String edit(@RequestParam(value = "oldPath") String oldPath,
                       @RequestParam(value = "newPath") String newPath) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        List<Document> doc = documentService.getDocumentbyFilePath(oldPath.replace("\\","/"));

        map.put("docName",doc.get(0).getDocumentName());
        map.put("docID",doc.get(0).getIddocument());

        List<Cooperate> sharer=cooperateService.getCoopbyDocIDandAuth("share",doc.get(0).getIddocument());

        List<String> docSharer=new ArrayList<String>();
        for (int i=0;i<sharer.size();i++){
            docSharer.add(userInfoService.getUserByID(sharer.get(i).getUserIduser()).get(0).getUserName());
        }
        map.put("docSharer",docSharer);

        List<Cooperate> owner=cooperateService.getCoopbyDocIDandAuth("own",doc.get(0).getIddocument());

        String docOwner=userInfoService.getUserByID(owner.get(0).getUserIduser()).get(0).getUserName();

        map.put("docOwner",docOwner);

        try{
            //FileUtils.copyFile(new File(newPath),new File(oldPath));
            VersionController.addVersion_atom(oldPath);
            List<String> content=new FileRow(oldPath).readFile();
            String result="";
            for (String c:content){
                result+=c;
                if (c!=content.get(content.size()-1)){
                    result+='\n';
                }
            }
            map.put("content",result);


            Gson gson = new Gson();
            String logpath = CommonOperation.getLogPath(oldPath);
            map.put("logpath",logpath);

            String sourceString = "{}";
            Map<String, String> versionLog = new HashMap<String, String>();
            byte[] sourceByte = sourceString.getBytes();
            if(null != sourceByte){
                File versionLogFile = new File(logpath);
                if (!versionLogFile.exists()) {
                    File dir = new File(versionLogFile.getParent());
                    dir.mkdirs();
                    versionLogFile.createNewFile();
                    FileOutputStream outStream = new FileOutputStream(versionLogFile);
                    outStream.write(sourceByte);
                    outStream.close();
                }
                else{
                    String content_temp = new Scanner(versionLogFile).useDelimiter("\\Z").next();
                    Type type = new TypeToken<Map<String, String>>() {
                    }.getType();
                    versionLog = gson.fromJson(content_temp, type);
                }
            }
            map.put("versionLog",versionLog);

            String[] color = new String[1];

            if(versionLog != null){
                color = new String[versionLog.size()];
                for (int i = 0; i < versionLog.size(); i++) {
                    color[i] = ColorUtil.getColor();
                }
            }
            map.put("color",color);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try {
            json = mapper.writeValueAsString(map);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return json;
    }
}
