package com.CollabCraft.file;

import java.io.*;
import java.util.ArrayList;

public class FileRow {
    private File file;

    public FileRow(String path){
        this.file=new File(path);
    }


    public void writeFile(ArrayList<String> content){
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (String l:content) {
                bw.write(l);
                if (l != content.get(content.size()-1)){
                    bw.newLine();
                }
            }
            bw.flush();
            bw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public ArrayList<String> readFile(){
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader bf = new BufferedReader(fr);
            String str="";
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }

            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }
}
