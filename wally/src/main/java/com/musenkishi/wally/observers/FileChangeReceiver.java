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

import java.util.ArrayList;

/**
 * A BroadcastReceiver that can be used when you want to be notified when
 * "com.musenkishi.wally.observers.FILES_CHANGED" is broadcast.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-05-29.
 */
public class FileChangeReceiver extends BroadcastReceiver {

    public static final String FILES_CHANGED = "com.musenkishi.wally.observers.FILES_CHANGED";

    private ArrayList<OnFileChangeListener> onFileChangeListeners;

    public interface OnFileChangeListener{
        void onFileChange();
    }

    public FileChangeReceiver() {
        onFileChangeListeners = new ArrayList<OnFileChangeListener>();
    }

    public void addListener(OnFileChangeListener onFileChangeListener){
        if (onFileChangeListener != null) {
            onFileChangeListeners.add(onFileChangeListener);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (FILES_CHANGED.equals(intent.getAction())){
            if (onFileChangeListeners != null) {
                for (OnFileChangeListener listener : onFileChangeListeners){
                    if (listener != null) {
                        listener.onFileChange();
                    }
                }
            }
        }
    }
}
