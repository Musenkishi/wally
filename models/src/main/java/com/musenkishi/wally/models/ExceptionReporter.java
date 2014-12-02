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

/**
 * A class to send crash reports throughout the project.
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-10-17.
 */
public class ExceptionReporter {

    private Class fromClass;
    private String customMessage;
    private String exceptionMessage;

    public interface OnReportListener {
        abstract void report(Class fromClass, String reason, String exceptionMessage);
    }

    public ExceptionReporter(Class fromClass, String customMessage, String exceptionMessage) {
        this.fromClass = fromClass;
        this.customMessage = customMessage;
        this.exceptionMessage = exceptionMessage;
    }

    public Class getFromClass() {
        return fromClass;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
