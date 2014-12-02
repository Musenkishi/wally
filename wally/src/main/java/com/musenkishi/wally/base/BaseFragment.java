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

package com.musenkishi.wally.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.musenkishi.wally.R;

/**
 * A base class where you can put logic that is needed in all fragments.
 * Created by Musenkishi on 2014-03-07 15:03.
 */
public abstract class BaseFragment extends Fragment {

    protected int color;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Will be called when the fragment comes visible to the user (e.g. inside a viewpager)
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && color != 0) {
            ((BaseActivity) getActivity()).colorizeActionBar(color);
        }
    }

    protected void setActionBarColor(int color){
        this.color = color;
    }

    public int getAppBarColor(){
        return color;
    }

    public static void setInsets(Activity context, View view, boolean useStatusBarHeight, int extraHeight, int extraBottom) {
        int otherPadding = view.getResources().getDimensionPixelSize(R.dimen.gridview_other_padding);
        int bottomPadding = otherPadding + extraBottom;
        int statusbarHeight = 0;
        view.setPadding(
                otherPadding,
                statusbarHeight + extraHeight + otherPadding,
                otherPadding,
                bottomPadding
        );
    }

}
