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
public final class FilterAspectRatioKeys {

    public static final String PARAMETER_KEY = "ratios";

    public static final Filter<String, String> RATIO_ALL = new Filter<String, String>("All", "");
    public static final Filter<String, String> RATIO_PORTRAIT = new Filter<String, String>("Portrait", "24x32,23x32,22x32,21x32,20x32,19x32,18x32,17x32,16x32,15x32,14x32,13x32,12x32");
    public static final Filter<String, String> RATIO_4_3 = new Filter<String, String>("4:3", "4x3");
    public static final Filter<String, String> RATIO_5_4 = new Filter<String, String>("5:4", "5x4");
    public static final Filter<String, String> RATIO_16_9 = new Filter<String, String>("16:9", "16x9");
    public static final Filter<String, String> RATIO_16_10 = new Filter<String, String>("16:10", "16x10");
    public static final Filter<String, String> RATIO_32_9 = new Filter<String, String>("32:9", "32x9");
    public static final Filter<String, String> RATIO_48_9 = new Filter<String, String>("48:9", "48x9");

    public static List<Filter<String, String>> getOrderedList(){

        ArrayList<Filter<String, String>> ratios = new ArrayList<Filter<String, String>>();

        ratios.add(RATIO_ALL);
        ratios.add(RATIO_PORTRAIT);
        ratios.add(RATIO_4_3);
        ratios.add(RATIO_5_4);
        ratios.add(RATIO_16_9);
        ratios.add(RATIO_16_10);
        ratios.add(RATIO_32_9);
        ratios.add(RATIO_48_9);

        return ratios;
    }
}
