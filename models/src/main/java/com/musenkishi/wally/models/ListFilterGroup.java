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

import com.musenkishi.wally.models.filters.FilterGroup;

import java.util.List;

/**
 * A Filter class for choosing an option in a list. Could be used with a Spinner.
 * Created by Musenkishi on 2014-03-12 19:22.
 */
public class ListFilterGroup extends FilterGroup {

    private List<Filter<String, String>> filters;
    private Filter<String, String> selectedFilter;

    public ListFilterGroup(String tag, List<Filter<String, String>> filters) {
        this.tag = tag;
        this.filters = filters;
    }

    public List<Filter<String, String>> getFilters() {
        return filters;
    }

    public Filter getFilter(int index){
        return filters.get(index);
    }

    @Override
    public void setSelectedOption(Filter filter) {
        this.selectedFilter = filter;
    }

    public Filter<String, String> getSelectedFilter(){
        return selectedFilter;
    }
}
