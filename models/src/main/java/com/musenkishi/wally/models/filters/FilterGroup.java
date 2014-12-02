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

package com.musenkishi.wally.models.filters;

import com.musenkishi.wally.models.Filter;

/**
 * Interface for defining a filter
 * Created by Musenkishi on 2014-03-12 19:16.
 */
public abstract class FilterGroup {

    protected String tag;

    protected abstract void setSelectedOption(Filter filter);

    public String getTag(){
        return tag;
    }

}
