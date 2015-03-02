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
 * Class for the original author
 * Created by Musenkishi on 2014-04-05 21:17.
 */

@AutoParcel
public abstract class Author implements Parcelable {

    public abstract String name();
    public abstract Uri page();

    public static Author create(String name, Uri page) {
        return new AutoParcel_Author(name, page);
    }
}
