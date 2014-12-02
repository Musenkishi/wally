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

/**
 * A class for defining Filter items.
 * Created by Musenkishi on 2014-03-12 22:20.
 */
public class Filter<K, V> {

    private K key;
    private V value;
    private boolean isCustom = false;

    public Filter(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Filter(K key, V value, boolean isCustom) {
        this.key = key;
        this.value = value;
        this.isCustom = isCustom;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    @Override
    public String toString() {
        return key + "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filter)) return false;

        Filter filter = (Filter) o;

        if (isCustom != filter.isCustom) return false;
        if (key != null ? !key.equals(filter.key) : filter.key != null) return false;
        if (value != null ? !value.equals(filter.value) : filter.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (isCustom ? 1 : 0);
        return result;
    }
}
