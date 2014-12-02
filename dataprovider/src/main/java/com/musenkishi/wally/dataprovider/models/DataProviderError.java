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

/**
 * A class used for errors by the DataProvider.
 * Created by Musenkishi on 2014-03-01 16:18.
 */
public class DataProviderError {

    public enum Type { NETWORK, LOCAL }

    private Type type;
    private int httpStatusCode;

    private String message;

    public DataProviderError(Type mType, int httpStatusCode, String message) {
        this.type = mType;
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return message;
    }
}
