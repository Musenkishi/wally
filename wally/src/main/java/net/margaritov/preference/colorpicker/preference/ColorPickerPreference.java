package net.margaritov.preference.colorpicker.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.musenkishi.wally.R;

import net.margaritov.preference.colorpicker.view.ColorPanelView;
import net.margaritov.preference.colorpicker.view.ColorPickerView;

public class ColorPickerPreference extends DialogPreference implements ColorPickerView.OnColorChangedListener{


    private ColorPickerView colorPickerView;
    private ColorPanelView oldColorView;
    private ColorPanelView newColorView;

    private int color;

    private boolean alphaChannelVisible = false;
    private String alphaChannelText = null;
    private boolean showDialogTitle = false;
    private boolean showPreviewSelectedColorInList = true;
    private int colorPickerSliderColor = -1;
    private int colorPickerBorderColor = -1;


    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);

    }


    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);

        showDialogTitle = a.getBoolean(R.styleable.ColorPickerPreference_showDialogTitle, false);
        showPreviewSelectedColorInList = a.getBoolean(R.styleable.ColorPickerPreference_showSelectedColorInList, true);

        a.recycle();
        a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);

        alphaChannelVisible = a.getBoolean(R.styleable.ColorPickerView_alphaChannelVisible, false);
        alphaChannelText = a.getString(R.styleable.ColorPickerView_alphaChannelText);
        colorPickerSliderColor = a.getColor(R.styleable.ColorPickerView_colorPickerSliderColor, -1);
        colorPickerBorderColor = a.getColor(R.styleable.ColorPickerView_colorPickerBorderColor, -1);

        a.recycle();

        if(showPreviewSelectedColorInList) {
            setWidgetLayoutResource(R.layout.preference_preview_layout);
        }

        if(!showDialogTitle) {
            setDialogTitle(null);
        }

        setDialogLayoutResource(R.layout.view_color_picker_dialog);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setPersistent(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current setting value


        if(getDialog() != null && colorPickerView != null) {
            myState.currentColor = colorPickerView.getColor();
        }
        else {
            myState.currentColor = 0;
        }

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());


        // Set this Preference's widget to reflect the restored state
        if(getDialog() != null && colorPickerView != null) {
            Log.d("mColorPicker", "Restoring color!");
            colorPickerView.setColor(myState.currentColor, true);
        }



    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ColorPanelView preview = (ColorPanelView) view.findViewById(R.id.preference_preview_color_panel);

        if(preview != null) {
            preview.setColor(color);
        }

    }

    @Override
    protected void onBindDialogView(View layout) {
        super.onBindDialogView(layout);

        boolean isLandscapeLayout = false;

        colorPickerView = (ColorPickerView) layout.findViewById(R.id.color_picker_view);

        colorPickerView = (ColorPickerView) layout
                .findViewById(R.id.color_picker_view);
        oldColorView = (ColorPanelView) layout.findViewById(R.id.color_panel_old);
        newColorView = (ColorPanelView) layout.findViewById(R.id.color_panel_new);

        ((LinearLayout) oldColorView.getParent()).setPadding(Math
                .round(colorPickerView.getDrawingOffset()), 0, Math
                .round(colorPickerView.getDrawingOffset()), 0);

        colorPickerView.setAlphaSliderVisible(alphaChannelVisible);
        colorPickerView.setAlphaSliderText(alphaChannelText);
        colorPickerView.setSliderTrackerColor(colorPickerSliderColor);

        if(colorPickerSliderColor != -1) {
            colorPickerView.setSliderTrackerColor(colorPickerSliderColor);
        }

        if(colorPickerBorderColor != -1) {
            colorPickerView.setBorderColor(colorPickerBorderColor);
        }


        colorPickerView.setOnColorChangedListener(this);

        //Log.d("mColorPicker", "setting initial color!");
        oldColorView.setColor(color);
        colorPickerView.setColor(color, true);
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            color = colorPickerView.getColor();
            persistInt(color);

            notifyChanged();

        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if(restorePersistedValue) {
            color = getPersistedInt(0xFF000000);
            //Log.d("mColorPicker", "Load saved color: " + color);
        }
        else {
            color = (Integer)defaultValue;
            persistInt(color);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0xFF000000);
    }


    @Override
    public void onColorChanged(int newColor) {
        newColorView.setColor(newColor);
    }



    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        int currentColor;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            currentColor = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(currentColor);
        }

        // Standard creator object using an instance of this class
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}
