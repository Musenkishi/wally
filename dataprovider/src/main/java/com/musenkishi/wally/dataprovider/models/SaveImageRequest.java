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

package com.musenkishi.wally.dataprovider.models;

import android.net.Uri;

/**
 * A class that contains either a download ID, or a SavedImageData. Not both.
 *
 * Created by Musenkishi on 2014-08-21.
 */
public class SaveImageRequest {

    private Long downloadID;
    private Uri filePath;

    public SaveImageRequest(Long downloadID) {
        this.downloadID = downloadID;
        filePath = null;
    }

    public SaveImageRequest(Uri filePath) {
        this.filePath = filePath;
        downloadID = null;
    }

    public Long getDownloadID() {
        return downloadID;
    }

    public void setDownloadID(Long downloadID) {
        this.downloadID = downloadID;
    }

    public Uri getFilePath() {
        return filePath;
    }

    public void setFilePath(Uri filePath) {
        this.filePath = filePath;
    }
}
