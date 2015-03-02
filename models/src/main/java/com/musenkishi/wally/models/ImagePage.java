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

import java.util.ArrayList;

import auto.parcel.AutoParcel;

/**
 * Contains all available data on an Image.
 * Created by Musenkishi on 2014-04-05 21:11.
 */
@AutoParcel
public abstract class ImagePage implements Parcelable {

    public abstract String title();
    public abstract String imageId();
    public abstract Uri imagePath();
    public abstract String resolution();
    public abstract String category();
    public abstract String rating();
    public abstract String uploader();
    public abstract String uploadDate();
    public abstract Author author();
    public abstract ArrayList<Tag> tags();

    public static ImagePage create(String title, String imageId, Uri imagePath, String resolution, String category, String rating, String uploader, String uploadDate, Author author, ArrayList<Tag> tags) {
        return new AutoParcel_ImagePage(title, imageId, imagePath, resolution, category, rating, uploader, uploadDate, author, tags);
    }

    public int getImageHeight() {
        String height = resolution().split("x")[1].trim();
        return Integer.parseInt(height);
    }

    public int getImageWidth() {
        String width = resolution().split("x")[0].trim();
        return Integer.parseInt(width);
    }
}
