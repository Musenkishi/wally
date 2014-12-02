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
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.musenkishi.wally.R;

/**
 * A dialog matching the Material Design specified in the preview documents.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-09-23.
 */
public class MaterialDialogFragment extends DialogFragment {
    public static final String TAG = "MaterialDialogFragment";

    private static final String STATE_PRIMARY_COLOR = TAG + ".state.primaryColor";
    private static final String STATE_BUTTON_POSITIVE_TEXT_ID = TAG + ".state.button.positive.textId";
    private static final String STATE_BUTTON_NEGATIVE_TEXT_ID = TAG + ".state.button.negative.textId";
    private static final String STATE_TITLE_TEXT_ID = TAG + ".state.title.textId";
    private static final String STATE_TITLE_TEXT_STRING = TAG + ".state.title.textString";
    private static final String STATE_LAYOUT_RES_ID = TAG + ".state.layout.resId";
    private static final String STATE_MESSAGE_TEXT_ID = TAG + ".state.message.resId";
    private static final String STATE_MESSAGE_TEXT_STRING = TAG + ".state.message.resString";

    private int primaryColor;

    private Button buttonPositive;
    private Button buttonNegative;
    private int positiveButtonTextResourceId;
    private int negativeButtonTextResourceId;

    private DialogInterface.OnClickListener positiveButtonOnClickListener;
    private DialogInterface.OnClickListener negativeButtonOnClickListener;

    private TextView textViewTitle;
    private int titleResourceId;
    private String titleString;

    private ScrollView scrollView;
    private int layoutResourceId;
    private ViewStub viewStub;

    private String message;
    private int messageResourceId;

    public MaterialDialogFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_PRIMARY_COLOR)) {
                primaryColor = savedInstanceState.getInt(STATE_PRIMARY_COLOR);
            }
            if (savedInstanceState.containsKey(STATE_BUTTON_POSITIVE_TEXT_ID)) {
                positiveButtonTextResourceId = savedInstanceState.getInt(STATE_BUTTON_POSITIVE_TEXT_ID);
            }
            if (savedInstanceState.containsKey(STATE_BUTTON_NEGATIVE_TEXT_ID)) {
                negativeButtonTextResourceId = savedInstanceState.getInt(STATE_BUTTON_NEGATIVE_TEXT_ID);
            }
            if (savedInstanceState.containsKey(STATE_TITLE_TEXT_ID)) {
                titleResourceId = savedInstanceState.getInt(STATE_TITLE_TEXT_ID);
            }
            if (savedInstanceState.containsKey(STATE_TITLE_TEXT_STRING)) {
                titleString = savedInstanceState.getString(STATE_TITLE_TEXT_STRING);
            }
            if (savedInstanceState.containsKey(STATE_LAYOUT_RES_ID)) {
                layoutResourceId = savedInstanceState.getInt(STATE_LAYOUT_RES_ID);
            }
            if (savedInstanceState.containsKey(STATE_MESSAGE_TEXT_ID)) {
                messageResourceId = savedInstanceState.getInt(STATE_MESSAGE_TEXT_ID);
            }
            if (savedInstanceState.containsKey(STATE_MESSAGE_TEXT_STRING)) {
                message = savedInstanceState.getString(STATE_MESSAGE_TEXT_STRING);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity(), android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog );
//                    android.R.style.Theme_DeviceDefault_Light_Dialog);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_base_material);
            textViewTitle = (TextView) dialog.findViewById(R.id.dialog_title);
            buttonNegative = (Button) dialog.findViewById(R.id.dialog_button_negative);
            buttonPositive = (Button) dialog.findViewById(R.id.dialog_button_positive);
            scrollView = (ScrollView) dialog.findViewById(R.id.dialog_scrollview);
            viewStub = (ViewStub) dialog.findViewById(R.id.dialog_viewstub);

            setupViews(dialog.getContext());
            hideEmptyViews();

            if (!isCancelable()) {
                buttonNegative.setVisibility(View.GONE);
            }

            return dialog;
        } else {
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (primaryColor != 0) {
            outState.putInt(STATE_PRIMARY_COLOR, primaryColor);
        }
        if (positiveButtonTextResourceId != 0) {
            outState.putInt(STATE_BUTTON_POSITIVE_TEXT_ID, positiveButtonTextResourceId);
        }
        if (negativeButtonTextResourceId != 0) {
            outState.putInt(STATE_BUTTON_NEGATIVE_TEXT_ID, negativeButtonTextResourceId);
        }
        if (titleResourceId != 0) {
            outState.putInt(STATE_TITLE_TEXT_ID, titleResourceId);
        }
        if (titleString != null) {
            outState.putString(STATE_TITLE_TEXT_STRING, titleString);
        }
        if (layoutResourceId != 0) {
            outState.putInt(STATE_LAYOUT_RES_ID, layoutResourceId);
        }
        if (messageResourceId != 0) {
            outState.putInt(STATE_MESSAGE_TEXT_ID, messageResourceId);
        }
        if (message != null) {
            outState.putString(STATE_MESSAGE_TEXT_STRING, message);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    public void setContentView(int layoutResourceId) {
        this.layoutResourceId = layoutResourceId;
    }

    public void setPositiveButton(int textResourceId, DialogInterface.OnClickListener onClickListener) {
        this.positiveButtonTextResourceId = textResourceId;
        this.positiveButtonOnClickListener = onClickListener;
    }

    public void setNegativeButton(int textResourceId, DialogInterface.OnClickListener onClickListener) {
        this.negativeButtonTextResourceId = textResourceId;
        this.negativeButtonOnClickListener = onClickListener;
    }

    public void setTitle(int titleResourceId) {
        this.titleResourceId = titleResourceId;
    }

    public void setTitle(String title) {
        this.titleString = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(int stringResourceId) {
        this.messageResourceId = stringResourceId;
    }

    public void setPrimaryColor(int color) {
        this.primaryColor = color;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Prepare views to be hidden if they don't have any content.
     */
    private void hideEmptyViews() {
        if (titleResourceId == 0 && titleString == null) {
            textViewTitle.setVisibility(View.GONE);
        }
        if (layoutResourceId == 0 && message == null && messageResourceId == 0) {
            scrollView.setVisibility(View.GONE);
        }
    }

    protected void setupViews(Context context) {

        if (titleResourceId != 0) {
            textViewTitle.setText(titleResourceId);
        } else if (titleString != null) {
            textViewTitle.setText(titleString);
        }

        if (primaryColor != 0) {
            buttonPositive.setTextColor(primaryColor);
        }

        if (positiveButtonTextResourceId != 0) {
            buttonPositive.setText(positiveButtonTextResourceId);
        }

        if (negativeButtonTextResourceId != 0) {
            buttonNegative.setText(negativeButtonTextResourceId);
        }

        buttonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeButtonOnClickListener != null) {
                    negativeButtonOnClickListener.onClick(null, 1);
                }
                dismiss();
            }
        });
        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveButtonOnClickListener != null) {
                    positiveButtonOnClickListener.onClick(null, 0);
                }
                dismiss();
            }
        });

        if (layoutResourceId != 0) {
            viewStub.setLayoutResource(layoutResourceId);
            viewStub.inflate();
        } else if (message != null || messageResourceId != 0) {
            TextView textView = new TextView(context);
            if (messageResourceId != 0) {
                textView.setText(messageResourceId);
            } else {
                textView.setText(message);
            }
            textView.setTextColor(getResources().getColor(R.color.Black_Light));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setLineSpacing(1f, 1.2f);
            scrollView.removeAllViews();
            scrollView.addView(textView);
        }
    }
}
