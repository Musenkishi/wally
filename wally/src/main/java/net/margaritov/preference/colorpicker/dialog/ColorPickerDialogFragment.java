/*
 * Copyright (C) 2010 Daniel Nilsson
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

package net.margaritov.preference.colorpicker.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.musenkishi.wally.R;

import net.margaritov.preference.colorpicker.view.ColorPanelView;
import net.margaritov.preference.colorpicker.view.ColorPickerView;

public class ColorPickerDialogFragment extends DialogFragment implements ColorPickerView.OnColorChangedListener {

    public static final String TAG = "ColorPickerDialogFragment";
    private static final String STATE_CURRENT_COLOR = TAG + ".CurrentColor";

    private ColorPickerView colorPicker;

    private ColorPanelView oldColor;
    private ColorPanelView newColor;

    private ColorPickerView.OnColorChangedListener onColorChangedListener;

    private OnDialogButtonClickedListener onDialogButtonClickedListener;

    private Button buttonPositive;
    private Button buttonNegative;

    private int initialColor = Color.BLACK;

    public interface OnDialogButtonClickedListener{
        abstract void onPositiveButtonClicked(DialogFragment dialogFragment);
        abstract void onNegativeButtonClicked(DialogFragment dialogFragment);
    }

    public ColorPickerDialogFragment() {
    }

    public ColorPickerDialogFragment(int initialColor, ColorPickerView.OnColorChangedListener onColorChangedListener, OnDialogButtonClickedListener onDialogButtonClickedListener) {
        this.onColorChangedListener = onColorChangedListener;
        this.initialColor = initialColor;
        this.onDialogButtonClickedListener = onDialogButtonClickedListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog);
            dialog.getWindow().setFormat(PixelFormat.RGBA_8888);

            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.setContentView(R.layout.view_color_picker_dialog);

            colorPicker = (ColorPickerView) dialog.findViewById(R.id.color_picker_view);
            oldColor = (ColorPanelView) dialog.findViewById(R.id.color_panel_old);
            newColor = (ColorPanelView) dialog.findViewById(R.id.color_panel_new);
            ((LinearLayout) oldColor.getParent()).setPadding(Math
                    .round(colorPicker.getDrawingOffset()), 0, Math
                    .round(colorPicker.getDrawingOffset()), 0);
            colorPicker.setOnColorChangedListener(this);
            oldColor.setColor(initialColor);
            if (savedInstanceState == null) {
                colorPicker.setColor(initialColor, true);
            }

            buttonNegative = (Button) dialog.findViewById(R.id.dialog_button_negative);
            buttonPositive = (Button) dialog.findViewById(R.id.dialog_button_positive);

            buttonNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDialogButtonClickedListener != null) {
                        onDialogButtonClickedListener.onNegativeButtonClicked(ColorPickerDialogFragment.this);
                    }
                    dismiss();
                }
            });
            buttonPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDialogButtonClickedListener != null) {
                        onDialogButtonClickedListener.onPositiveButtonClicked(ColorPickerDialogFragment.this);
                    }
                    dismiss();
                }
            });
            return dialog;
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_COLOR, colorPicker.getColor());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) { //FIXME not getting called when rotating
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_CURRENT_COLOR)){
                final int color = savedInstanceState.getInt(STATE_CURRENT_COLOR);
                colorPicker.setColor(color, true);
            } else {
                colorPicker.setColor(initialColor, true);
            }
        } else {
            colorPicker.setColor(initialColor, true);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onColorChanged(int color) {
        newColor.setColor(color);

        if (onColorChangedListener != null) {
            onColorChangedListener.onColorChanged(color);

        }

    }

    public void setOnDialogButtonClickedListener(OnDialogButtonClickedListener onDialogButtonClickedListener){
        this.onDialogButtonClickedListener = onDialogButtonClickedListener;
    }

    public void setAlphaSliderVisible(boolean visible) {
        colorPicker.setAlphaSliderVisible(visible);
    }

    public int getColor() {
        return colorPicker.getColor();
    }

}
