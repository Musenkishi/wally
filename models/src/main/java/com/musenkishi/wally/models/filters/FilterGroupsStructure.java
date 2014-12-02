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
 * Class with all filters gathered in one place.
 * Created by Musenkishi on 2014-03-13 21:34.
 */
public class FilterGroupsStructure {

    //timespan
    private Filter<String, String> timespanFilter;
    //boards
    private String boardsFilter;
    //maturity
    private String purityFilter;
    //aspect ratio
    private Filter<String, String> aspectRatioFilter;
    //resolution-option
    private String resOptFilter;
    //resolution
    private Filter<String, String> resolutionFilter;

    public FilterGroupsStructure() {
    }

    public Filter<String, String> getTimespanFilter() {
        return timespanFilter;
    }

    public void setTimespanFilter(Filter<String, String> timespanFilter) {
        this.timespanFilter = timespanFilter;
    }

    public String getBoardsFilter() {
        return boardsFilter;
    }

    public void setBoardsFilter(String boardsFilter) {
        this.boardsFilter = boardsFilter;
    }

    public String getPurityFilter() {
        return purityFilter;
    }

    public void setPurityFilter(String maturityFilter) {
        this.purityFilter = maturityFilter;
    }

    public Filter<String, String> getAspectRatioFilter() {
        return aspectRatioFilter;
    }

    public void setAspectRatioFilter(Filter<String, String> aspectRatioFilter) {
        this.aspectRatioFilter = aspectRatioFilter;
    }

    public String getResOptFilter() {
        return resOptFilter;
    }

    public void setResOptFilter(String resOptFilter) {
        this.resOptFilter = resOptFilter;
    }

    public Filter<String, String> getResolutionFilter() {
        return resolutionFilter;
    }

    public void setResolutionFilter(Filter<String, String> resolutionFilter) {
        this.resolutionFilter = resolutionFilter;
    }
}
