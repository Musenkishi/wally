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

package com.musenkishi.wally.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;

/**
 * Builder class for Spannable Strings. By default they are coloured light blue without underline.
 * No one likes underlines on links.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-04-23.
 */
public class TextLinkBuilder {

    private int start = 0;
    private int end = 0;

    private String text;
    private int color = 0xFF0E84FC; //default color which is light blue
    private OnTextClickedListener onTextClickedListener;

    private Context context;
    private String font = null;
    private String TAG = "TextLinkBuilder";

    public interface OnTextClickedListener{
        void onClick(View textView);
    }

    /**
     * IMPORTANT: In order for the click listener to work, you must call
     * setMovementMethod(LinkMovementMethod.getInstance()) on the TextView before you
     * set the Spannable String
     *
     * @param context
     * @param text text to be used in TextView.
     * @param match text to match in text. First occurrence will be selected.
     */
    public TextLinkBuilder(Context context, String text, String match) {
        this.context = context;
        this.text = text;

        int start = text.indexOf(match);
        int end = match.length() + start;

        if (start == -1){
            throw new StringIndexOutOfBoundsException("text is not a substring. Are you sure the strings match?");
        }

        this.start = start;
        this.end = end;
    }

    /**
     * IMPORTANT: In order for the click listener to work you must call
     * setMovementMethod(LinkMovementMethod.getInstance()) on the TextView before you
     * set the Spannable String
     *
     * @param context
     * @param textResourseId resource id of text to be used in TextView.
     * @param matchReourseId resource id of text to match in textResourceId. First occurrence will be selected.
     */
    public TextLinkBuilder(Context context, int textResourseId, int matchReourseId) {
        this.context = context;
        text = context.getString(textResourseId);

        String match = context.getString(matchReourseId);

        int start = text.indexOf(match);
        int end = match.length() + start;

        if (start == -1){
            throw new StringIndexOutOfBoundsException("text is not a substring");
        }

        this.start = start;
        this.end = end;
    }


    public TextLinkBuilder font(String font){
        this.font = font;
        return this;
    }

    public TextLinkBuilder color(int color){
        this.color = color;
        return this;
    }

    public TextLinkBuilder onClick(OnTextClickedListener onTextClickedListener){
        this.onTextClickedListener = onTextClickedListener;
        return this;
    }

    public SpannableString build(){
        SpannableString spannableString = new SpannableString(text);
        TextClickableSpan clickableSpan = new TextClickableSpan(color) {
            @Override
            public void onClick(View textView) {
                if (onTextClickedListener != null) {
                    onTextClickedListener.onClick(textView);
                }
            }
        };
        if (font != null) {
            spannableString.setSpan(new TypefaceSpan(context, font), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }



}