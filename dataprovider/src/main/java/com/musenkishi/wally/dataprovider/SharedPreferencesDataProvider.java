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

package com.musenkishi.wally.dataprovider;

import android.content.Context;
import android.content.SharedPreferences;

import com.musenkishi.wally.models.Filter;
import com.musenkishi.wally.models.filters.FilterAspectRatioKeys;
import com.musenkishi.wally.models.filters.FilterBoardsKeys;
import com.musenkishi.wally.models.filters.FilterPurityKeys;
import com.musenkishi.wally.models.filters.FilterResOptKeys;
import com.musenkishi.wally.models.filters.FilterResolutionKeys;
import com.musenkishi.wally.models.filters.FilterTimeSpanKeys;

/**
 * A class that handles shared preferences data.
 * Created by Musenkishi on 2014-03-13 21:05.
 */
public class SharedPreferencesDataProvider {

    public static final String SHARED_PREF_KEY = "com.musenkishi.wally.sharedpreferences";

    public static final String FILTER_KEY = ".filterKey";
    public static final String FILTER_VALUE = ".filterValue";
    private static final String FILTER_CUSTOM = ".filterIsCustom";
    public static final String CRASH_LOGGING = ".crashlogging";
    public static final String APP_START_COUNT = ".appStartCount";
    public static final String LATEST_VERSION_INSTALLED = ".lastVersionInstalled";

    public static final int CRASH_LOGGING_NOT_READ = 193784;
    public static final int CRASH_LOGGING_NOT_APPROVED = 193785;
    public static final int CRASH_LOGGING_APPROVED = 193786;

    private SharedPreferences sharedPreferences;

    public SharedPreferences getPrefs() {
        return sharedPreferences;
    }

    public SharedPreferencesDataProvider(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    public int getLatestVersion(int defaultVersion) {
        return sharedPreferences.getInt(LATEST_VERSION_INSTALLED, defaultVersion);
    }

    public void setLatestVersion(int version) {
        sharedPreferences.edit().putInt(LATEST_VERSION_INSTALLED, version).apply();
    }

    public int getAppStartCount(){
        return sharedPreferences.getInt(APP_START_COUNT, 1);
    }

    public void incrementAppStartCount(){
        int count = getAppStartCount() + 1;
        sharedPreferences.edit().putInt(APP_START_COUNT, count).apply();
    }

    public int hasUserApprovedCrashLogging(){
        return sharedPreferences.getInt(CRASH_LOGGING, CRASH_LOGGING_NOT_READ);
    }

    public void setUserApprovedCrashLogging(int crashLoggingState){
        sharedPreferences.edit().putInt(CRASH_LOGGING, crashLoggingState).apply();
    }

    public Filter<String, String> getTimespan(String tag){
        String savedKey = sharedPreferences.getString(tag + FILTER_KEY, FilterTimeSpanKeys.TIMESPAN_3_DAYS.getKey());
        String savedValue = sharedPreferences.getString(tag + FILTER_VALUE, FilterTimeSpanKeys.TIMESPAN_3_DAYS.getValue());
        return new Filter<String, String>(savedKey, savedValue);
    }

    public void setTimespan(String tag, Filter<String, String> timespan){
        sharedPreferences.edit().putString(tag + FILTER_KEY, timespan.getKey()).apply();
        sharedPreferences.edit().putString(tag + FILTER_VALUE, timespan.getValue()).apply();
    }

    public String getBoards(String tag) {
        return sharedPreferences.getString(tag, FilterBoardsKeys.BOARD_GENERAL_KEY + FilterBoardsKeys.BOARD_ANIME_KEY + "0");
    }

    public void setBoards(String tag, String paramValue) {
        sharedPreferences.edit().putString(tag, paramValue).apply();
    }

    public String getPurity(String tag) {
        return sharedPreferences.getString(tag, FilterPurityKeys.SFW_KEY + "00"); //This will default to SFW purity
    }

    public void setPurity(String tag, String paramValue) {
        sharedPreferences.edit().putString(tag, paramValue).apply();
    }

    public Filter<String, String> getAspectRatio(String tag){
        String savedKey = sharedPreferences.getString(tag + FILTER_KEY, FilterAspectRatioKeys.RATIO_ALL.getKey());
        String savedValue = sharedPreferences.getString(tag + FILTER_VALUE, FilterAspectRatioKeys.RATIO_ALL.getValue());
        return new Filter<String, String>(savedKey, savedValue);
    }

    public void setAspectRatio(String tag, Filter<String, String> aspectRatio){
        sharedPreferences.edit().putString(tag + FILTER_KEY, aspectRatio.getKey()).apply();
        sharedPreferences.edit().putString(tag + FILTER_VALUE, aspectRatio.getValue()).apply();
    }

    public String getResolutionOption(String tag) {
        return sharedPreferences.getString(tag, FilterResOptKeys.EXACTLY);
    }

    public void setResolutionOption(String tag, String paramValue){
        sharedPreferences.edit().putString(tag, paramValue).apply();
    }

    public Filter<String, String> getResolution(String tag){
        String savedKey = sharedPreferences.getString(tag + FILTER_KEY, FilterResolutionKeys.RES_ALL.getKey());
        String savedValue = sharedPreferences.getString(tag + FILTER_VALUE, FilterResolutionKeys.RES_ALL.getValue());
        boolean savedIsCustom = sharedPreferences.getBoolean(tag + FILTER_CUSTOM, FilterResolutionKeys.RES_ALL.isCustom());
        return new Filter<String, String>(savedKey, savedValue, savedIsCustom);
    }

    public void setResolution(String tag, Filter<String, String> resolution) {
        sharedPreferences.edit().putString(tag + FILTER_KEY, resolution.getKey()).apply();
        sharedPreferences.edit().putString(tag + FILTER_VALUE, resolution.getValue()).apply();
        sharedPreferences.edit().putBoolean(tag + FILTER_CUSTOM, resolution.isCustom()).apply();
    }
}
