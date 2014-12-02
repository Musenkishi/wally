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

package com.musenkishi.wally.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;

import com.musenkishi.wally.R;
import com.musenkishi.wally.models.Size;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * DialogFragment that provides the user with a prefilled, editable resolution input.
 *
 * Created by Musenkishi on 2014-03-11 19:25.
 */
public class CustomResolutionDialogFragment extends MaterialDialogFragment implements Handler.Callback {

    public static final String TAG = "CustomResolutionDialogFragment";
    private static final int SET_PREFILLED_RESOLUTION = 13502;

    private Handler uiHandler;

    private FloatLabeledEditText editTextWidth;
    private FloatLabeledEditText editTextHeight;

    public CustomResolutionDialogFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        uiHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() != null) {
            setContentView(R.layout.dialog_content_custom_resolution);
            setCancelable(false);
            final Dialog dialog = super.onCreateDialog(savedInstanceState);

            editTextWidth = (FloatLabeledEditText) dialog.findViewById(R.id.custom_res_width);
            editTextHeight = (FloatLabeledEditText) dialog.findViewById(R.id.custom_res_height);

            uiHandler.sendEmptyMessage(SET_PREFILLED_RESOLUTION);

            return dialog;
        } else {
            return null;
        }
    }

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {

            case SET_PREFILLED_RESOLUTION:
                if (getActivity() != null) {
                    Size size = getRealScreenDimensions();

                    editTextHeight.setText(Integer.toString(size.getHeight()));
                    editTextWidth.setText(Integer.toString(size.getWidth()));
                }
                break;

        }

        return false;
    }

    private Size getRealScreenDimensions(){
        if (getActivity() != null) {
            final DisplayMetrics metrics = new DisplayMetrics();
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Method mGetRawH = null;
            Method mGetRawW = null;

            int realWidth = 0;
            int realHeight = 0;

            // For JellyBeans and onward
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN){
                display.getRealMetrics(metrics);

                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }else{
                try {
                    mGetRawH = Display.class.getMethod("getRawHeight");
                    mGetRawW = Display.class.getMethod("getRawWidth");

                    try {
                        realWidth = (Integer) mGetRawW.invoke(display);
                        realHeight = (Integer) mGetRawH.invoke(display);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return new Size(realWidth, realHeight);
        } else {
            return null;
        }
    }

    public Size getSize(){
        int width;
        int height;
        try {
            width = Integer.parseInt(editTextWidth.getTextString());
        } catch (NumberFormatException e) {
            width = 0;
        }
        try {
            height = Integer.parseInt(editTextHeight.getTextString());
        } catch (NumberFormatException e) {
            height = 0;
        }

        return new Size(width, height);
    }

}
