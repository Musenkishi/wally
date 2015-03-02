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

package com.musenkishi.wally.observers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.musenkishi.wally.dataprovider.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A BroadcastReceiver that can be used when you want to be notified when
 * "com.musenkishi.wally.observers.GET_FILES" is broadcast.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-05-29.
 */
public class FileReceiver extends BroadcastReceiver implements Handler.Callback {

    public static final String GET_FILES = "com.musenkishi.wally.observers.GET_FILES";

    public static final int MSG_GET_FILES = 57348;
    public static final int MSG_SEND_FILES = 57349;

    private ArrayList<OnFileChangeListener> onFileChangeListeners;
    private final Handler uiHandler;
    private final Handler backgroundHandler;
    private Map<String, Boolean> existingFiles;

    public interface OnFileChangeListener{
        void onFileChange(Map<String, Boolean> existingFiles);
    }

    public FileReceiver() {
        onFileChangeListeners = new ArrayList<>();
        uiHandler = new Handler(Looper.getMainLooper(), this);
        HandlerThread handlerThread = new HandlerThread("FileChangeReceiver.background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), this);
    }

    public void addListener(OnFileChangeListener onFileChangeListener){
        if (onFileChangeListener != null) {
            onFileChangeListeners.add(onFileChangeListener);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (GET_FILES.equals(intent.getAction()) &&
                onFileChangeListeners != null &&
                !backgroundHandler.hasMessages(MSG_GET_FILES)) {
            backgroundHandler.sendEmptyMessage(MSG_GET_FILES);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_GET_FILES:
                Map<String, Boolean> savedFiles = new HashMap<>();
                FileManager fileManager = new FileManager();
                for (Uri uri : fileManager.getFiles()) {
                    String filename = uri.getLastPathSegment();
                    String[] filenames = filename.split("\\.(?=[^\\.]+$)"); //split filename from it's extension
                    savedFiles.put(filenames[0], true);
                }

                Message msgFiles = uiHandler.obtainMessage();
                msgFiles.what = MSG_SEND_FILES;
                msgFiles.obj = savedFiles;
                uiHandler.sendMessage(msgFiles);
                break;

            case MSG_SEND_FILES:
                existingFiles = (Map<String, Boolean>) msg.obj;
                for (OnFileChangeListener listener : onFileChangeListeners){
                    if (listener != null) {
                        listener.onFileChange(existingFiles);
                    }
                }
                break;
        }
        return false;
    }

    public Map<String, Boolean> getExistingFiles() {
        return existingFiles;
    }
}
