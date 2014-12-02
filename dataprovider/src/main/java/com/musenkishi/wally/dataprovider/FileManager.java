/*
 * Copyright (C) 2014 Freddie (Musenkishi) Lust-Hed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musenkishi.wally.dataprovider;

import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Use this to manage files on the external storage.
 * Created by Musenkishi on 2014-03-04 19:24.
 */
public class FileManager {

    public static final String DIRECTORY_BASE = "/Wally";

    public FileManager() {
    }

    public boolean fileExists(String filename){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + DIRECTORY_BASE);
        myDir.mkdirs();
        File[] listOfFiles = myDir.listFiles();

        for (File file : listOfFiles)
        {
            if (file.isFile())
            {
                String[] filenames = file.getName().split("\\.(?=[^\\.]+$)"); //split filename from it's extension
                if(filenames[0].equalsIgnoreCase(filename)) { //matching defined filename
                    return true;
                }
            }
        }
        return false;
    }

    public File getFile(String filename){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + DIRECTORY_BASE);
        myDir.mkdirs();
        File[] listOfFiles = myDir.listFiles();

        for (File file : listOfFiles)
        {
            if (file.isFile())
            {
                String[] filenames = file.getName().split("\\.(?=[^\\.]+$)"); //split filename from it's extension
                if(filenames[0].equalsIgnoreCase(filename)) { //matching defined filename
                    return file;
                }
            }
        }
        return null;
    }

    public ArrayList<Uri> getFiles(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        ArrayList<Uri> f = new ArrayList<Uri>();// list of file paths
        File[] listFile;
        File myDir = new File(root + DIRECTORY_BASE);

        if (myDir.isDirectory()) {
            listFile = myDir.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                String path = listFile[i].getAbsolutePath();
                f.add(Uri.parse(path));
            }
        }
        return f;
    }

}
