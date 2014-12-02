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

/**
 * A class for the board filtering.
 * Created by Musenkishi on 2014-03-14 21:17.
 */
public class FilterPurity {

    private boolean sfwChecked = true;
    private boolean sketchyChecked = true;

    public FilterPurity(boolean sfwChecked, boolean sketchyChecked) {
        this.sfwChecked = sfwChecked;
        this.sketchyChecked = sketchyChecked;
    }

    public FilterPurity(String paramValue) {
        this.sfwChecked = String.valueOf(paramValue.charAt(0)).equalsIgnoreCase("1");
        this.sketchyChecked = String.valueOf(paramValue.charAt(1)).equalsIgnoreCase("1");
    }

    public boolean isSfwChecked() {
        return sfwChecked;
    }

    public void setSfwChecked(boolean sfwChecked) {
        this.sfwChecked = sfwChecked;
    }

    public boolean isSketchyChecked() {
        return sketchyChecked;
    }

    public void setSketchyChecked(boolean sketchyChecked) {
        this.sketchyChecked = sketchyChecked;
    }

    /**
     * This will return a formatted String that can be used as a parameter value.
     *
     * @return returns a formatted value depending on the boolean values.
     */
    public String getFormattedValue() {
        String formattedString = "";
        formattedString += sfwChecked ? FilterPurityKeys.SFW_KEY : "0";
        formattedString += sketchyChecked ? FilterPurityKeys.SKETCHY_KEY : "0";
        formattedString += "0";
        return formattedString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterPurity)) return false;

        FilterPurity that = (FilterPurity) o;

        if (sketchyChecked != that.sketchyChecked) return false;
        if (sfwChecked != that.sfwChecked) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (sfwChecked ? 1 : 0);
        result = 31 * result + (sketchyChecked ? 1 : 0);
        return result;
    }
}
