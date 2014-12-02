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
public final class FilterTimeSpanKeys {

    public static final String PARAMETER_KEY = "ts";

    public static final Filter<String, String> TIMESPAN_24H = new Filter<String, String>("1 day (24h)", "1d");
    public static final Filter<String, String> TIMESPAN_3_DAYS = new Filter<String, String>("3 days", "3d");
    public static final Filter<String, String> TIMESPAN_1_WEEK = new Filter<String, String>("1 week", "1w");
    public static final Filter<String, String> TIMESPAN_2_WEEKS = new Filter<String, String>("2 weeks", "2w");
    public static final Filter<String, String> TIMESPAN_1_MONTH = new Filter<String, String>("1 month", "1m");
    public static final Filter<String, String> TIMESPAN_2_MONTHS = new Filter<String, String>("2 months", "2m");
    public static final Filter<String, String> TIMESPAN_3_MONTHS = new Filter<String, String>("3 months", "3m");
    public static final Filter<String, String> TIMESPAN_ALL_TIME = new Filter<String, String>("All time", "1");

    public static List<Filter<String, String>> getOrderedList(){

        ArrayList<Filter<String, String>> filters = new ArrayList<Filter<String, String>>();

        filters.add(TIMESPAN_24H);
        filters.add(TIMESPAN_3_DAYS);
        filters.add(TIMESPAN_1_WEEK);
        filters.add(TIMESPAN_2_WEEKS);
        filters.add(TIMESPAN_1_MONTH);
        filters.add(TIMESPAN_2_MONTHS);
        filters.add(TIMESPAN_3_MONTHS);
        filters.add(TIMESPAN_ALL_TIME);

        return filters;
    }

}
