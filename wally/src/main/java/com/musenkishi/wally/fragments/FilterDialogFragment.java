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
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.musenkishi.wally.R;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.models.Filter;
import com.musenkishi.wally.models.ListFilterGroup;
import com.musenkishi.wally.models.Size;
import com.musenkishi.wally.models.filters.FilterAspectRatioKeys;
import com.musenkishi.wally.models.filters.FilterBoards;
import com.musenkishi.wally.models.filters.FilterBoardsKeys;
import com.musenkishi.wally.models.filters.FilterPurity;
import com.musenkishi.wally.models.filters.FilterPurityKeys;
import com.musenkishi.wally.models.filters.FilterResolutionKeys;

/**
 * DialogFragment responsible for showing applicable filters to the user.
 *
 * Created by Musenkishi on 2014-03-11.
 */
public class FilterDialogFragment extends MaterialDialogFragment {

    public static final String TAG = "FilterDialogFragment";

    private static final String STATE_HAS_ANYTHING_CHANGED = TAG + ".state.hasAnythingChanged";
    private static final int NUM_OF_RETRIES = 2;

    private Spinner spinnerAspectRatio;
    private CheckBox checkBoxBoardGeneral;
    private CheckBox checkBoxBoardAnime;
    private CheckBox checkBoxBoardPeople;
    private CheckBox checkBoxPuritySFW;
    private CheckBox checkBoxPuritySketchy;
    private Spinner spinnerResolution;

    private boolean hasAnythingChanged = false;

    private int numberOfRetriesOnCategory = 0;
    private int numberOfRetriesOnRating = 0;

    private CompoundButton.OnCheckedChangeListener categoryCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!hasAtLeastOneCategoryChecked()){
                compoundButton.setChecked(true);
                numberOfRetriesOnCategory++;
                if (numberOfRetriesOnCategory == NUM_OF_RETRIES) {
                    Toast.makeText(compoundButton.getContext(), R.string.you_must_have_at_least_one_category_checked, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            numberOfRetriesOnCategory = 0;
                        }
                    }, 2 * 1000);
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener ratingCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!hasAtLeastOneRatingChecked()){
                compoundButton.setChecked(true);
                numberOfRetriesOnRating++;
                if (numberOfRetriesOnRating == NUM_OF_RETRIES) {
                    Toast.makeText(compoundButton.getContext(), R.string.you_must_have_at_least_one_rating_checked, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            numberOfRetriesOnRating = 0;
                        }
                    }, 2 * 1000);
                }
            }
        }
    };

    public FilterDialogFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_HAS_ANYTHING_CHANGED)) {
            hasAnythingChanged = savedInstanceState.getBoolean(STATE_HAS_ANYTHING_CHANGED);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() != null) {

            setContentView(R.layout.dialog_content_filter);
            final Dialog dialog = super.onCreateDialog(savedInstanceState);

            checkBoxBoardGeneral = (CheckBox) dialog.findViewById(R.id.filter_boards_general);
            checkBoxBoardAnime = (CheckBox) dialog.findViewById(R.id.filter_boards_anime);
            checkBoxBoardPeople = (CheckBox) dialog.findViewById(R.id.filter_boards_high_resolution);
            checkBoxPuritySFW = (CheckBox) dialog.findViewById(R.id.filter_purity_sfw);
            checkBoxPuritySketchy = (CheckBox) dialog.findViewById(R.id.filter_purity_sketchy);
            spinnerAspectRatio = (Spinner) dialog.findViewById(R.id.filter_aspect_ratio_spinner);
            spinnerResolution = (Spinner) dialog.findViewById(R.id.filter_resolution_spinner);

            int[] titleIds = new int[]{
                    R.id.filter_title_categories,
                    R.id.filter_title_rating,
                    R.id.filter_title_aspect_ratio,
                    R.id.filter_title_resolution
            };
            colorizeTextViews(titleIds, dialog);

            setupFilterViews();
            return dialog;
        } else {
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        saveChanges();
        outState.putBoolean(STATE_HAS_ANYTHING_CHANGED, hasAnythingChanged);
        super.onSaveInstanceState(outState);
    }

    private void colorizeTextViews(int[] resourceIds, Dialog dialog) {
        for (int id : resourceIds) {
            TextView textView = (TextView) dialog.findViewById(id);
            textView.setTextColor(getPrimaryColor());
        }
    }

    private void setupFilterViews() {
        setupBoardCheckBoxes();
        setupPurityCheckBoxes();
        setupAspectRatioSpinner();
        setupResolutionSpinner();
    }

    private void setupBoardCheckBoxes() {
        FilterBoards filterBoards = new FilterBoards(WallyApplication.getDataProviderInstance().getBoards(FilterBoardsKeys.PARAMETER_KEY));
        checkBoxBoardGeneral.setChecked(filterBoards.isGeneralChecked());
        checkBoxBoardAnime.setChecked(filterBoards.isAnimeChecked());
        checkBoxBoardPeople.setChecked(filterBoards.isPeopleChecked());
        checkBoxBoardGeneral.setOnCheckedChangeListener(categoryCheckedChangeListener);
        checkBoxBoardAnime.setOnCheckedChangeListener(categoryCheckedChangeListener);
        checkBoxBoardPeople.setOnCheckedChangeListener(categoryCheckedChangeListener);
    }

    private void setupPurityCheckBoxes() {
        FilterPurity filterPurity = new FilterPurity(WallyApplication.getDataProviderInstance().getPurity(FilterPurityKeys.PARAMETER_KEY));
        checkBoxPuritySFW.setChecked(filterPurity.isSfwChecked());
        checkBoxPuritySketchy.setChecked(filterPurity.isSketchyChecked());
        checkBoxPuritySFW.setOnCheckedChangeListener(ratingCheckedChangeListener);
        checkBoxPuritySketchy.setOnCheckedChangeListener(ratingCheckedChangeListener);
    }

    private void setupAspectRatioSpinner() {
        ListFilterGroup aspectRatioFilterGroup = new ListFilterGroup(FilterAspectRatioKeys.PARAMETER_KEY, FilterAspectRatioKeys.getOrderedList());
        ArrayAdapter filterAdapter = new ArrayAdapter(getActivity(), R.layout.view_filter_list_item, aspectRatioFilterGroup.getFilters());
        spinnerAspectRatio.setAdapter(filterAdapter);
        Filter<String, String> defaultAspectRatio = WallyApplication.getDataProviderInstance().getAspectRatio(aspectRatioFilterGroup.getTag());
        int defaultAspectRationPosition = aspectRatioFilterGroup.getFilters().indexOf(defaultAspectRatio);
        spinnerAspectRatio.setSelection(defaultAspectRationPosition);
    }

    private void setupResolutionSpinner() {
        final ListFilterGroup resolutionFilterGroup = new ListFilterGroup(FilterResolutionKeys.PARAMETER_KEY, FilterResolutionKeys.getOrderedList());
        Filter<String, String> defaultResolution = WallyApplication.getDataProviderInstance().getResolution(resolutionFilterGroup.getTag());
        if (defaultResolution.isCustom()){
            defaultResolution.setKey(defaultResolution.getValue() + "…");
            FilterResolutionKeys.RES_CUSTOM.setKey(defaultResolution.getKey());
            FilterResolutionKeys.RES_CUSTOM.setValue(defaultResolution.getValue());
        }
        final ArrayAdapter filterAdapter = new ArrayAdapter(getActivity(), R.layout.view_filter_list_item, resolutionFilterGroup.getFilters());
        spinnerResolution.setAdapter(filterAdapter);
        int defaultResolutionPosition = resolutionFilterGroup.getFilters().indexOf(defaultResolution);
        spinnerResolution.setSelection(defaultResolutionPosition, false);
        spinnerResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (resolutionFilterGroup.getFilter(i).isCustom()) {
                    final CustomResolutionDialogFragment customResolutionDialogFragment = new CustomResolutionDialogFragment();
                    customResolutionDialogFragment.setPrimaryColor(getPrimaryColor());
                    customResolutionDialogFragment.setTitle(R.string.dialog_custom_resolution_title);
                    customResolutionDialogFragment.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Size size = customResolutionDialogFragment.getSize();
                            FilterResolutionKeys.RES_CUSTOM.setKey(size + "…");
                            FilterResolutionKeys.RES_CUSTOM.setValue(size.toString());
                            filterAdapter.notifyDataSetChanged();
                        }
                    });
                    customResolutionDialogFragment.setNegativeButton(R.string.cancel, null);
                    customResolutionDialogFragment.show(getFragmentManager(), CustomResolutionDialogFragment.TAG);
                } else {
                    //Clear custom filter
                    FilterResolutionKeys.RES_CUSTOM.setKey("Custom…");
                    FilterResolutionKeys.RES_CUSTOM.setValue("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected");
            }
        });
    }

    /**
     * Returns true if any filters has been changed.
     */
    public boolean saveChanges() {

        FilterBoards currentFilterBoards = new FilterBoards(checkBoxBoardGeneral.isChecked(), checkBoxBoardAnime.isChecked(), checkBoxBoardPeople.isChecked());
        FilterBoards savedFilterBoards = new FilterBoards(WallyApplication.getDataProviderInstance().getBoards(FilterBoardsKeys.PARAMETER_KEY));
        if (!currentFilterBoards.equals(savedFilterBoards)){
            WallyApplication.getDataProviderInstance().setBoards(FilterBoardsKeys.PARAMETER_KEY, currentFilterBoards.getFormattedValue());
            hasAnythingChanged = true;
        }

        FilterPurity currentFilterPurity = new FilterPurity(checkBoxPuritySFW.isChecked(), checkBoxPuritySketchy.isChecked());
        FilterPurity savedFilterPurity = new FilterPurity(WallyApplication.getDataProviderInstance().getPurity(FilterPurityKeys.PARAMETER_KEY));
        if (!currentFilterPurity.equals(savedFilterPurity)){
            WallyApplication.getDataProviderInstance().setPurity(FilterPurityKeys.PARAMETER_KEY, currentFilterPurity.getFormattedValue());
            hasAnythingChanged = true;
        }

        Filter<String, String> filterAspectRatio = (Filter<String, String>) spinnerAspectRatio.getSelectedItem();
        if (!WallyApplication.getDataProviderInstance().getAspectRatio(FilterAspectRatioKeys.PARAMETER_KEY).equals(filterAspectRatio)){
            WallyApplication.getDataProviderInstance().setAspectRatio(FilterAspectRatioKeys.PARAMETER_KEY, filterAspectRatio);
            hasAnythingChanged = true;
        }

        Filter<String, String> filterResolution = (Filter<String, String>) spinnerResolution.getSelectedItem();
        if (!WallyApplication.getDataProviderInstance().getResolution(FilterResolutionKeys.PARAMETER_KEY).equals(filterResolution)){
            WallyApplication.getDataProviderInstance().setResolution(FilterResolutionKeys.PARAMETER_KEY, filterResolution);
            hasAnythingChanged = true;
        }

        return hasAnythingChanged;
    }

    private boolean hasAtLeastOneRatingChecked() {
        return checkBoxPuritySFW.isChecked() || checkBoxPuritySketchy.isChecked();
    }

    private boolean hasAtLeastOneCategoryChecked() {
        return checkBoxBoardGeneral.isChecked() ||
                checkBoxBoardAnime.isChecked() ||
                checkBoxBoardPeople.isChecked();
    }
}
