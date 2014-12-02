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
 * Created by Musenkishi on 2014-03-14 20:32.
 */
public class FilterBoards {

    private boolean generalChecked = true;
    private boolean animeChecked = true;
    private boolean peopleChecked = false;

    public FilterBoards(boolean generalChecked, boolean animeChecked, boolean peopleChecked) {
        this.generalChecked = generalChecked;
        this.animeChecked = animeChecked;
        this.peopleChecked = peopleChecked;
    }

    public FilterBoards(String paramValue) {
        this.generalChecked = "1".equals(paramValue.subSequence(0,1));
        this.animeChecked = "1".equals(paramValue.subSequence(1,2));
        this.peopleChecked = "1".equals(paramValue.subSequence(2,3));
    }

    public boolean isGeneralChecked() {
        return generalChecked;
    }

    public void setGeneralChecked(boolean generalChecked) {
        this.generalChecked = generalChecked;
    }

    public boolean isAnimeChecked() {
        return animeChecked;
    }

    public void setAnimeChecked(boolean animeChecked) {
        this.animeChecked = animeChecked;
    }

    public boolean isPeopleChecked() {
        return peopleChecked;
    }

    public void setPeopleChecked(boolean peopleChecked) {
        this.peopleChecked = peopleChecked;
    }

    /**
     * This will return a formatted String that can be used as a parameter value.
     * @return returns a formatted value depending on the boolean values.
     */
    public String getFormattedValue(){
        String formattedString = "";

        formattedString += generalChecked ? "1" : "0";

        formattedString += animeChecked ? "1" : "0";

        formattedString += peopleChecked ? "1" : "0";

        return formattedString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterBoards)) return false;

        FilterBoards that = (FilterBoards) o;

        if (animeChecked != that.animeChecked) return false;
        if (generalChecked != that.generalChecked) return false;
        if (peopleChecked != that.peopleChecked) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (generalChecked ? 1 : 0);
        result = 31 * result + (animeChecked ? 1 : 0);
        result = 31 * result + (peopleChecked ? 1 : 0);
        return result;
    }
}
