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

import java.util.ArrayList;
import java.util.List;

/**
 * Final class containing all values needed for the timespan filter.
 * Created by Musenkishi on 2014-03-13 19:30.
 */
public final class FilterResolutionKeys {

    public static final String PARAMETER_KEY = "resolutions";

    public static final Filter<String, String> RES_ALL = new Filter<String, String>("All", "");
    public static final Filter<String, String> RES_1024X768 = new Filter<String, String>("1024x768", "1024x768");
    public static final Filter<String, String> RES_1280X800 = new Filter<String, String>("1280x800", "1280x800");
    public static final Filter<String, String> RES_1366X768 = new Filter<String, String>("1280x800", "1280x800");
    public static final Filter<String, String> RES_1280X960 = new Filter<String, String>("1280x960", "1280x960");
    public static final Filter<String, String> RES_1440X900 = new Filter<String, String>("1440x900", "1440x900");
    public static final Filter<String, String> RES_1600X900 = new Filter<String, String>("1600x900", "1600x900");
    public static final Filter<String, String> RES_1280X1024 = new Filter<String, String>("1280x1024", "1280x1024");
    public static final Filter<String, String> RES_1600X1200 = new Filter<String, String>("1600x1200", "1600x1200");
    public static final Filter<String, String> RES_1680X1050 = new Filter<String, String>("1680x1050", "1680x1050");
    public static final Filter<String, String> RES_1920X1080 = new Filter<String, String>("1920x1080", "1920x1080");
    public static final Filter<String, String> RES_1920X1200 = new Filter<String, String>("1920x1200", "1920x1200");
    public static final Filter<String, String> RES_2560X1440 = new Filter<String, String>("2560x1440", "2560x1440");
    public static final Filter<String, String> RES_2560X1600 = new Filter<String, String>("2560x1600", "2560x1600");
    public static final Filter<String, String> RES_3840X1080 = new Filter<String, String>("3840x1080", "3840x1080");
    public static final Filter<String, String> RES_5760X1080 = new Filter<String, String>("5760x1080", "5760x1080");
    public static final Filter<String, String> RES_3840X2160 = new Filter<String, String>("3840x2160", "3840x2160");

    public static Filter<String, String> RES_CUSTOM = new Filter<String, String>("Custom… ", "", true);

    public static List<Filter<String, String>> getOrderedList(){

        ArrayList<Filter<String, String>> filters = new ArrayList<Filter<String, String>>();
        
        filters.add(RES_ALL);
        filters.add(RES_CUSTOM);
        filters.add(RES_1024X768);
        filters.add(RES_1280X800);
        filters.add(RES_1366X768);
        filters.add(RES_1280X960);
        filters.add(RES_1440X900);
        filters.add(RES_1600X900);
        filters.add(RES_1280X1024);
        filters.add(RES_1600X1200);
        filters.add(RES_1680X1050);
        filters.add(RES_1920X1080);
        filters.add(RES_1920X1200);
        filters.add(RES_2560X1440);
        filters.add(RES_2560X1600);
        filters.add(RES_3840X1080);
        filters.add(RES_5760X1080);
        filters.add(RES_3840X2160);
        
        return filters;
    }

    /**
     * Loops through all filter options available and compares defaultResolution until one is
     * found that matches.
     * @param defaultResolution
     * @return true if
     */
    public static boolean isCustom(Filter<String, String> defaultResolution) {
        for (Filter<String, String> filter : getOrderedList()){
            if (defaultResolution.getValue().equalsIgnoreCase(filter.getValue())){
                return true;
            }
        }
        return false;
    }
}
