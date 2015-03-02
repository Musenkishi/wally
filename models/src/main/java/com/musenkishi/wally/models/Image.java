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

package com.musenkishi.wally.models;

import android.net.Uri;
import android.os.Parcelable;

import auto.parcel.AutoParcel;

/**
 * Class used for handling an image.
 * Created by Musenkishi on 2014-02-23.
 */
@AutoParcel
public abstract class Image implements Parcelable {

    public static final String TAG = "com.musenkishi.wally.model.image.tag";

    public abstract String imageId();
    public abstract String thumbURL();
    public abstract String imagePageURL();
    public abstract String resolution();

    public static Image create(String imageId, String thumbURL, String imageURL, String resolution) {
        return new AutoParcel_Image(imageId, thumbURL, imageURL, resolution);
    }

    public int getWidth(){
        if (resolution() != null && resolution().contains("x")) {
            String[] parts = resolution().split("x");
            String width = parts[0]; // width
            if (width != null) {
                return Integer.parseInt(width.trim());
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getHeight(){
        if (resolution() != null && resolution().contains("x")) {
            String[] parts = resolution().split("x");
            String height = parts[1]; // height
            if (height != null) {
                return Integer.parseInt(height.trim());
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Builds and returns the image page URL as a Uri with the scheme replaced with 'wally://'.
     */
    public Uri generateWallyUri() {
        return Uri.parse(imagePageURL()).buildUpon().scheme("wally").build();
    }
}
