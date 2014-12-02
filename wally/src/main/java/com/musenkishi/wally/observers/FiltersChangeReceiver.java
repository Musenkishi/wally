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
 * "com.musenkishi.wally.observers.FILTERS_CHANGED" is broadcast.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-09-24.
 */
public class FiltersChangeReceiver extends BroadcastReceiver {

    public static final String FILTERS_CHANGED = "com.musenkishi.wally.observers.FILTERS_CHANGED";

    private ArrayList<OnFiltersChangeListener> onFiltersChangeListeners;

    public interface OnFiltersChangeListener{
        void onFiltersChange();
    }

    public FiltersChangeReceiver() {
        onFiltersChangeListeners = new ArrayList<OnFiltersChangeListener>();
    }

    public void addListener(OnFiltersChangeListener onFileChangeListener){
        if (onFileChangeListener != null) {
            onFiltersChangeListeners.add(onFileChangeListener);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (FILTERS_CHANGED.equals(intent.getAction())){
            if (onFiltersChangeListeners != null) {
                for (OnFiltersChangeListener listener : onFiltersChangeListeners){
                    if (listener != null) {
                        listener.onFiltersChange();
                    }
                }
            }
        }
    }
}
