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
 * A class for the resolution option filter.
 * Created by Musenkishi on 2014-03-15 00:23.
 */
public class FilterResOpt {

    private boolean exactly = true;
    private boolean atLeast = true;

    public FilterResOpt(boolean exactly, boolean atLeast) {
        this.exactly = exactly;
        this.atLeast = atLeast;
    }

    public FilterResOpt(String paramValue) {
        this.exactly = paramValue.contains(FilterResOptKeys.EXACTLY);
        this.atLeast = paramValue.contains(FilterResOptKeys.AT_LEAST);
    }

    public boolean isExactly() {
        return exactly;
    }

    public void setExactly(boolean exactly) {
        this.exactly = exactly;
    }

    public boolean isAtLeast() {
        return atLeast;
    }

    public void setAtLeast(boolean atLeast) {
        this.atLeast = atLeast;
    }

    /**
     * This will return a formatted String that can be used as a parameter value.
     *
     * @return returns a formatted value depending on the boolean values.
     */
    public String getFormattedValue() {
        return exactly ? FilterResOptKeys.EXACTLY : FilterResOptKeys.AT_LEAST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterResOpt)) return false;

        FilterResOpt that = (FilterResOpt) o;

        if (atLeast != that.atLeast) return false;
        if (exactly != that.exactly) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (exactly ? 1 : 0);
        result = 31 * result + (atLeast ? 1 : 0);
        return result;
    }
}
