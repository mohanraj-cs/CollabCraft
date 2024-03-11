package com.CollabCraft.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileList {
    private List<FileOperation> FopList;

    public FileList(){
        this.FopList=new ArrayList<FileOperation>();
    }


    private int isContains(String path_t){
        int temp=-1;
        for (FileOperation f:this.FopList){
            if (f.getNewFilePath().equals(path_t)){
                temp=this.FopList.indexOf(f);
                break;
            }
        }
        return temp;
    }


    public boolean deleteFop(String path_t){
        boolean result=false;
        for (FileOperation f:this.FopList){
            if (f.getNewFilePath().equals(path_t)){
                this.FopList.remove(f);
                result=true;
                break;
            }
        }
        return result;
    }


    public FileOperation getFop(String newpath){
        FileOperation fop_t;
        int int_temp=isContains(newpath);
        if (int_temp>=0){
            fop_t=this.FopList.get(int_temp);
        }
        else{
            return null;
        }
        return fop_t;
    }



    public FileOperation createFop(String newpath,String oldpath){
        FileOperation fop_t;
        fop_t=new FileOperation(oldpath,newpath,0,new HashMap<String, Integer>(),new ArrayList<String[]>());
        this.FopList.add(fop_t);
        return fop_t;
    }
}
