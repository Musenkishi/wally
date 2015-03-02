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

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.musenkishi.wally.R;
import com.musenkishi.wally.activities.ImageDetailsActivity;
import com.musenkishi.wally.activities.MainActivity;
import com.musenkishi.wally.adapters.RecyclerImagesAdapter;
import com.musenkishi.wally.anim.interpolator.EaseInOutBezierInterpolator;
import com.musenkishi.wally.base.BaseActivity;
import com.musenkishi.wally.base.GridFragment;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.dataprovider.DataProvider;
import com.musenkishi.wally.dataprovider.NetworkDataProvider;
import com.musenkishi.wally.dataprovider.models.DataProviderError;
import com.musenkishi.wally.dataprovider.models.SaveImageRequest;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;
import com.musenkishi.wally.notification.NotificationProvider;
import com.musenkishi.wally.observers.FileReceiver;
import com.musenkishi.wally.observers.FiltersChangeReceiver;
import com.musenkishi.wally.util.PaletteRequest;

import net.margaritov.preference.colorpicker.dialog.ColorPickerDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.musenkishi.wally.observers.FileReceiver.OnFileChangeListener;
import static com.musenkishi.wally.observers.FiltersChangeReceiver.OnFiltersChangeListener;

/**
 * The fragment used for searching wallpapers.
 * <p/>
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-05-11.
 */
public class SearchFragment extends GridFragment implements
        RecyclerImagesAdapter.OnSaveButtonClickedListener,
        Handler.Callback,
        OnFileChangeListener,
        OnFiltersChangeListener {

    public static final String TAG = "Wally.SearchFragment";
    public static final String EXTRA_MESSAGE_TAG = TAG + ".Extra.Tag.Name";

    private static final int MSG_GET_IMAGES = 119;
    private static final int MSG_ERROR_IMAGE_REQUEST = 121;
    private static final int MSG_IMAGES_REQUEST_CREATE = 122;
    private static final int MSG_IMAGES_REQUEST_APPEND = 123;
    private static final int MSG_SAVE_LIST_OF_SAVED_IMAGES = 128;
    private static final int MSG_ERROR_IMAGE_SAVING = 129;
    private static final int MSG_NEW_COLOR_FETCHED = 130;
    private static final int MSG_RENDER_NEW_COLOR = 131;
    private static final int MSG_SAVE_BUTTON_CLICKED = 132;
    private static final int MSG_PAGE_RECEIVED = 133;
    private static final String STATE_IMAGES = TAG + ".Images";
    private static final String STATE_QUERY = TAG + ".Query";
    private static final String STATE_COLOR = TAG + ".CurrentColor";
    private static final String STATE_COLOR_TEXT = TAG + ".CurrentColor.Text";
    private static final String STATE_CURRENT_PAGE = TAG + ".Current.Page";

    private boolean isLoading;
    private Handler backgroundHandler;
    private Handler uiHandler;
    private HashMap<String, Boolean> savedFiles;

    private View quickReturnView;
    private View quickReturnBackground;
    private int quickReturnHeight;

    private EditText quickReturnEditText;
    private ImageButton quickReturnEditTextClearButton;

    private View colorPickerButton;
    private View colorTagCard;
    private TextView colorTagTextView;
    private ImageButton colorTagClearButton;
    private String currentColor = null;
    private int currentPage = 1;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarColor(getResources().getColor(R.color.Actionbar_Search_Background));
        setupHandlers();
        getActivity().sendBroadcast(new Intent(FileReceiver.GET_FILES));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        if (rootView != null) {
            super.onCreateView(rootView);
            quickReturnBackground = rootView.findViewById(R.id.quick_return_protective_background);
            quickReturnView = rootView.findViewById(R.id.quick_return_view);
            quickReturnEditTextClearButton = (ImageButton) rootView.findViewById(R.id.quick_return_edittext_clear);
            quickReturnEditTextClearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quickReturnEditText != null) {
                        query = "";
                        quickReturnEditText.setText("");
                        quickReturnEditText.performClick();
                        showKeyboard(quickReturnEditText);
                    }
                }
            });
            quickReturnEditText = (EditText) rootView.findViewById(R.id.quick_return_edittext);
            quickReturnEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quickReturnEditText.setCursorVisible(true);
                    restoreQuickReturnView();
                }
            });
            quickReturnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        return search();
                    }
                    return false;
                }

            });
            quickReturnEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(s)) {
                        quickReturnEditTextClearButton.setVisibility(View.VISIBLE);
                    } else {
                        quickReturnEditTextClearButton.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            gridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    float currentTranslationY = quickReturnView.getTranslationY();
                    float maxTranslationY = quickReturnHeight;
                    float newTranslationY = currentTranslationY + -dy;

                    if (newTranslationY > 0) {
                        newTranslationY = 0;
                    } else if (newTranslationY < -maxTranslationY) {
                        newTranslationY = -maxTranslationY;
                    }
                    quickReturnView.setTranslationY(newTranslationY);

                    float percent = (-maxTranslationY) / 100.0f;
                    float currentPercent = 100 - (newTranslationY / percent);

                    quickReturnBackground.setAlpha(currentPercent / 100);
                    quickReturnBackground.setTranslationY(newTranslationY);

                }
            });

            colorPickerButton = rootView.findViewById(R.id.quick_return_color_picker);
            colorPickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showColorPickerDialog();

                }
            });

            colorTagCard = rootView.findViewById(R.id.search_color_card);
            colorTagTextView = (TextView) rootView.findViewById(R.id.search_color_textview);
            colorTagTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showColorPickerDialog();
                }
            });
            colorTagClearButton = (ImageButton) rootView.findViewById(R.id.search_color_button_clear);
            colorTagClearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    colorTagCard.setVisibility(View.GONE);
                    colorPickerButton.setVisibility(View.VISIBLE);
                    currentColor = null;
                    query = quickReturnEditText.getText().toString();
                    gridView.setAdapter(null);
                    showLoader();
                    getImages(1, query);
                }
            });

            setupAutoSizeGridView();
            if (savedInstanceState != null && savedInstanceState.containsKey(STATE_IMAGES)) {
                query = savedInstanceState.getString(STATE_QUERY, "");
                Message msgObj = uiHandler.obtainMessage();
                msgObj.what = MSG_IMAGES_REQUEST_CREATE;
                msgObj.arg1 = 1;
                msgObj.obj = savedInstanceState.getParcelableArrayList(STATE_IMAGES);
                uiHandler.sendMessage(msgObj);
                currentColor = savedInstanceState.getString(STATE_COLOR);
                if (currentColor != null) {
                    int backgroundColor = Color.parseColor("#" + currentColor);
                    int textColor = savedInstanceState.getInt(STATE_COLOR_TEXT);
                    colorizeColorTag(backgroundColor, textColor, textColor, currentColor);
                    colorTagCard.setVisibility(View.VISIBLE);
                    colorPickerButton.setVisibility(View.GONE);
                }
                currentPage = savedInstanceState.getInt(STATE_CURRENT_PAGE);
            }
            ((MainActivity) getActivity()).addOnFileChangedListener(this);
            ((MainActivity) getActivity()).addOnFiltersChangedListener(this);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (imagesAdapter != null) {
            outState.putParcelableArrayList(STATE_IMAGES, imagesAdapter.getImages());
            outState.putString(STATE_QUERY, query);
            outState.putString(STATE_COLOR, currentColor);
            outState.putInt(STATE_COLOR_TEXT, colorTagTextView.getCurrentTextColor());
            outState.putInt(STATE_CURRENT_PAGE, currentPage);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.findFragmentByTag(ColorPickerDialogFragment.TAG) != null) {
            ColorPickerDialogFragment colorPickerDialogFragment = (ColorPickerDialogFragment) fragmentManager.findFragmentByTag(ColorPickerDialogFragment.TAG);
            colorPickerDialogFragment.setOnDialogButtonClickedListener(getColorPickerOnDialogButtonClickedListener());
            /*
            If the color picker dialog is open when a user rotates their device, the listener for the buttons
            would still be attached to the old fragment, hence not updating the content in the new one.
            This solution solves the problem by reattaching the listener to the new fragment.
             */
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacksAndMessages(null);
        backgroundHandler.getLooper().quit();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView.setClipToPadding(false);
        quickReturnView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setQuickReturnViewPadding();
                int extraPadding;

                if (quickReturnView != null) {
                    extraPadding = quickReturnView.getHeight();
                } else {
                    extraPadding = (int) getResources().getDimension(R.dimen.quick_return_view_height);
                }

                setInsets(getActivity(), gridView, false, extraPadding, getResources().getDimensionPixelSize(R.dimen.gridview_bottom_padding));
                quickReturnHeight = extraPadding;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    quickReturnView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    quickReturnView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        HashMap<String, Object> messages = WallyApplication.readMessages(TAG);
        if (!messages.isEmpty()){
            String tagName = (String) messages.get(EXTRA_MESSAGE_TAG);
            if (tagName != null) {
                searchTag(tagName);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            dismissKeyboard();
        }
    }

    private boolean search() {
        quickReturnEditText.setCursorVisible(false);
        dismissKeyboard();
        query = quickReturnEditText.getText().toString();
        gridView.setAdapter(null);
        showLoader();
        getImages(1, query);
        return true;
    }

    private void setQuickReturnViewPadding() {
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.quick_return_view_horizontal_padding);
            int verticalPadding = getResources().getDimensionPixelSize(R.dimen.quick_return_view_vertical_padding);
            quickReturnView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        }
    }

    private void getMoreImagesIfNeeded(int position, int totalItemCount) {
        int defaultNumberOfItemsPerPage = NetworkDataProvider.THUMBS_PER_PAGE;
        boolean shouldLoadMore = position >= totalItemCount - (defaultNumberOfItemsPerPage / 2);
        if (shouldLoadMore && !isLoading && imagesAdapter != null && imagesAdapter.getItemCount() > 0) {
            getImages(++currentPage, query);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.images_list, menu);

        MenuItem menuItemFilter = menu.findItem(R.id.action_filter);

        if (menuItemFilter != null) {
            menuItemFilter.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    FragmentManager fragmentManager = getFragmentManager();

                    if (fragmentManager != null) {
                        final FilterDialogFragment filterDialogFragment = new FilterDialogFragment();
                        filterDialogFragment.setPrimaryColor(getAppBarColor());
                        filterDialogFragment.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (filterDialogFragment.saveChanges()) {
                                    WallyApplication.getContext().sendBroadcast(new Intent(FiltersChangeReceiver.FILTERS_CHANGED));
                                }
                            }
                        });
                        filterDialogFragment.setNegativeButton(R.string.cancel, null);
                        filterDialogFragment.show(fragmentManager, FilterDialogFragment.TAG);
                    }

                    return false;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupHandlers() {
        HandlerThread handlerThread = new HandlerThread("Search.background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), this);
        uiHandler = new Handler(getActivity().getMainLooper(), this);
    }

    @Override
    protected void getImages(int index, final String query) {
        currentPage = index;
        isLoading = true;

        Message msgGetImages = backgroundHandler.obtainMessage();
        msgGetImages.what = MSG_GET_IMAGES;
        msgGetImages.arg1 = index;
        msgGetImages.obj = query;
        if (!backgroundHandler.hasMessages(msgGetImages.what)) {
            backgroundHandler.sendMessage(msgGetImages);
        }
    }

    private void showError(DataProviderError dataProviderError, int index) {
        Message msgObj = uiHandler.obtainMessage();
        msgObj.what = MSG_ERROR_IMAGE_REQUEST;
        msgObj.obj = dataProviderError;
        msgObj.arg1 = index;
        uiHandler.sendMessageDelayed(msgObj, 1000);
    }

    @Override
    public void onSaveButtonClicked(final Image image) {
        Message msgSaveButton = backgroundHandler.obtainMessage();
        msgSaveButton.what = MSG_SAVE_BUTTON_CLICKED;
        msgSaveButton.obj = image;
        backgroundHandler.sendMessage(msgSaveButton);
    }

    public void dismissKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public void showKeyboard(View viewThatWantsKeyboard) {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(viewThatWantsKeyboard, 0);
//            inputMethodManager.toggleSoftInputFromWindow(quickReturnEditText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void showColorPickerDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            int color = Color.BLACK;
            try {
                color = Color.parseColor(currentColor != null ? "#" + currentColor : "#000000");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            ColorPickerDialogFragment colorPickerDialogFragment = new ColorPickerDialogFragment(color, null, getColorPickerOnDialogButtonClickedListener());
            colorPickerDialogFragment.show(fragmentManager, ColorPickerDialogFragment.TAG);
        }
    }

    private ColorPickerDialogFragment.OnDialogButtonClickedListener getColorPickerOnDialogButtonClickedListener() {
        return new ColorPickerDialogFragment.OnDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(DialogFragment dialogFragment) {
                int color = ((ColorPickerDialogFragment) dialogFragment).getColor();
                Message msgObj = backgroundHandler.obtainMessage();
                msgObj.what = MSG_NEW_COLOR_FETCHED;
                msgObj.arg1 = color;
                uiHandler.sendMessage(msgObj);
            }

            @Override
            public void onNegativeButtonClicked(DialogFragment dialogFragment) {
                dialogFragment.dismiss();
            }
        };
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_GET_IMAGES:
                final int index = msg.arg1;
                String query = (String) msg.obj;
                WallyApplication.getDataProviderInstance().getImages(NetworkDataProvider.PATH_SEARCH, query,
                        currentColor, index, WallyApplication.getFilterSettings(), new DataProvider.OnImagesReceivedListener() {
                            @Override
                            public void onImagesReceived(ArrayList<Image> images) {
                                Message msgObj = uiHandler.obtainMessage();
                                msgObj.what = index == 1 ? MSG_IMAGES_REQUEST_CREATE : MSG_IMAGES_REQUEST_APPEND;
                                msgObj.obj = images;
                                uiHandler.sendMessage(msgObj);
                            }

                            @Override
                            public void onError(DataProviderError dataProviderError) {
                                showError(dataProviderError, index);
                            }
                        });
                break;

            case MSG_SAVE_BUTTON_CLICKED:
                Image image = (Image) msg.obj;
                WallyApplication.getDataProviderInstance().getPageData(image.imagePageURL(), new DataProvider.OnPageReceivedListener() {
                    @Override
                    public void onPageReceived(ImagePage imagePage) {
                        Message msgImagePage = uiHandler.obtainMessage();
                        msgImagePage.what = MSG_PAGE_RECEIVED;
                        msgImagePage.obj = imagePage;
                        uiHandler.sendMessage(msgImagePage);
                    }

                    @Override
                    public void onError(DataProviderError dataProviderError) {
                        Message msgObj = uiHandler.obtainMessage();
                        msgObj.what = MSG_ERROR_IMAGE_SAVING;
                        msgObj.obj = dataProviderError;
                        uiHandler.sendMessage(msgObj);
                    }
                });
                break;

            case MSG_PAGE_RECEIVED:
                ImagePage imagePage = (ImagePage) msg.obj;
                if (imagePage != null) {
                    SaveImageRequest saveImageRequest = WallyApplication.getDataProviderInstance().downloadImageIfNeeded(
                            imagePage.imagePath(),
                            imagePage.imageId(),
                            getResources().getString(R.string.notification_title_image_saving));

                    if (saveImageRequest.getDownloadID() != null && getActivity() instanceof MainActivity) {
                        WallyApplication.getDownloadIDs().put(saveImageRequest.getDownloadID(), imagePage.imageId());
                    } else {
                        getActivity().sendBroadcast(new Intent(FileReceiver.GET_FILES));
                    }
                }
                break;

            case MSG_ERROR_IMAGE_REQUEST:
                if (getActivity() != null) {
                    DataProviderError dataProviderError = (DataProviderError) msg.obj;
                    int imagesIndex = msg.arg1;
                    showErrorMessage(dataProviderError, imagesIndex);
                }
                break;

            case MSG_ERROR_IMAGE_SAVING:
                if (getActivity() != null) {
                    NotificationProvider notificationProvider = new NotificationProvider();
                    notificationProvider.cancelAll(getActivity());
                    Toast.makeText(getActivity(), "Couldn't save image", Toast.LENGTH_SHORT).show();
                }
                break;

            case MSG_IMAGES_REQUEST_CREATE:
                ArrayList<Image> images = (ArrayList<Image>) msg.obj;
                boolean shouldScheduleLayoutAnimation = msg.arg1 == 0;
                isLoading = false;
                if (images != null) {
                    hideLoader();
                    imagesAdapter = new RecyclerImagesAdapter(images, itemSize);
                    imagesAdapter.setOnSaveButtonClickedListener(SearchFragment.this);
                    imagesAdapter.updateSavedFilesList(savedFiles);
                    gridView.setAdapter(imagesAdapter);
                    setupAdapter();
                    if (shouldScheduleLayoutAnimation) {
                        gridView.scheduleLayoutAnimation();
                    }
                }
                break;

            case MSG_IMAGES_REQUEST_APPEND:
                ArrayList<Image> extraImages = (ArrayList<Image>) msg.obj;
                isLoading = false;
                if (extraImages != null) {
                    hideLoader();
                    int endPosition = imagesAdapter.getItemCount();
                    ArrayList<Image> currentList = imagesAdapter.getImages();
                    currentList.addAll(extraImages);
                    imagesAdapter.notifyItemRangeInserted(endPosition, extraImages.size());
                }
                break;

            case MSG_SAVE_LIST_OF_SAVED_IMAGES:
                savedFiles = (HashMap<String, Boolean>) msg.obj;
                if (imagesAdapter != null) {
                    imagesAdapter.updateSavedFilesList(savedFiles);
                    imagesAdapter.notifySavedItemsChanged();
                }
                break;

            case MSG_NEW_COLOR_FETCHED:
                int color = msg.arg1;
                String colorHex = Integer.toHexString(color).substring(2);
                int[] colors = new int[1];
                colors[0] = color;
                Bitmap bitmapColor = Bitmap.createBitmap(colors, 1, 1, Bitmap.Config.ARGB_8888); //Use this to create a Palette.
                Palette palette = Palette.generate(bitmapColor);

                Message newColorMessage = uiHandler.obtainMessage();
                newColorMessage.what = MSG_RENDER_NEW_COLOR;
                newColorMessage.obj = new Pair<String, Palette>(colorHex, palette);
                uiHandler.sendMessage(newColorMessage);
                break;

            case MSG_RENDER_NEW_COLOR:
                Pair<String, Palette> pair = (Pair<String, Palette>) msg.obj;
                String colorAsHex = pair.first;
                Palette palette1 = pair.second;

                showColorTag(colorAsHex, palette1);

                break;
        }
        return false;
    }

    private void showColorTag(String colorAsHex, Palette palette) {

        currentColor = colorAsHex;

        Palette.Swatch swatch = PaletteRequest.getBestSwatch(palette, palette.getVibrantSwatch());
        if (swatch != null) {
            colorizeColorTag(swatch.getRgb(), swatch.getBodyTextColor(), swatch.getBodyTextColor(), colorAsHex);
        }

        colorTagCard.setVisibility(View.VISIBLE);

        colorPickerButton.setVisibility(View.GONE);

        query = quickReturnEditText.getText().toString();
        gridView.setAdapter(null);
        showLoader();
        getImages(1, query);

    }

    private void colorizeColorTag(int backgroundColor, int primaryColor, int secondaryColor, String hexColor) {
        Drawable colorTagCardBackground = getResources().getDrawable(R.drawable.chip_background);
        colorTagCardBackground.setColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY);
        colorTagCard.setBackgroundDrawable(colorTagCardBackground);
        colorTagClearButton.getDrawable().setColorFilter(secondaryColor, PorterDuff.Mode.MULTIPLY);
        colorTagTextView.setTextColor(primaryColor);
        colorTagTextView.setText("#" + hexColor);
    }

    @Override
    public void onFileChange(Map<String, Boolean> existingFiles) {
        Message fileListMessage = uiHandler.obtainMessage();
        fileListMessage.obj = existingFiles;
        fileListMessage.what = MSG_SAVE_LIST_OF_SAVED_IMAGES;
        uiHandler.sendMessage(fileListMessage);
    }

    @Override
    public void onFiltersChange() {
        if (this.query != null && !this.query.isEmpty()) {
            restoreQuickReturnView();
            search();
        }
    }

    private void setupAdapter() {

        imagesAdapter.setOnGetViewListener(new RecyclerImagesAdapter.OnGetViewListener() {
            @Override
            public void onBindView(int position) {
                if (gridView.getAdapter() != null) {
                    getMoreImagesIfNeeded(position, imagesAdapter.getItemCount());
                }
            }
        });

        imagesAdapter.setOnItemClickListener(new RecyclerImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Image image = (Image) imagesAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(image.imagePageURL()),
                        view.getContext(),
                        ImageDetailsActivity.class);

                ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.thumb_image_view);

                Bitmap thumb = null;

                intent.putExtra(ImageDetailsActivity.INTENT_EXTRA_IMAGE, image);

                if (thumbnailImageView != null && thumbnailImageView.getDrawable() != null
                        && thumbnailImageView.getDrawable() instanceof GlideBitmapDrawable) {
                    GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) thumbnailImageView.getDrawable();
                    thumb = glideBitmapDrawable.getBitmap();
                } else if (thumbnailImageView != null && thumbnailImageView.getDrawable() != null
                        && thumbnailImageView.getDrawable() instanceof TransitionDrawable) {
                    GlideBitmapDrawable squaringDrawable = (GlideBitmapDrawable) ((TransitionDrawable) thumbnailImageView.getDrawable()).getDrawable(1);
                    thumb = squaringDrawable.getBitmap();
                }
                WallyApplication.setBitmapThumb(thumb);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    String transitionNameImage = getString(R.string.transition_image_details);
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                    android.support.v4.util.Pair.create(view.findViewById(R.id.thumb_image_view), transitionNameImage)
                            );
                    ActivityCompat.startActivityForResult(getActivity(), intent, ImageDetailsActivity.REQUEST_EXTRA_TAG, options.toBundle());

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.buildDrawingCache(true);
                    Bitmap drawingCache = view.getDrawingCache(true);
                    Bundle bundle = ActivityOptions.makeThumbnailScaleUpAnimation(view, drawingCache, 0, 0).toBundle();
                    getActivity().startActivityForResult(intent, REQUEST_CODE, bundle);
                } else {
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }

    public void searchTag(String tag) {
        if (!tag.equalsIgnoreCase(this.query)
                && quickReturnEditText != null
                && quickReturnView != null
                && quickReturnBackground != null) {

            if (!tag.startsWith("#")){
                tag = "#" + tag;
            }

            quickReturnEditText.setText(tag);
            restoreQuickReturnView();
            search();
        }
    }

    private void restoreQuickReturnView() {
        quickReturnView.animate()
                .translationY(0.0f)
                .setDuration(300)
                .setInterpolator(new EaseInOutBezierInterpolator())
                .start()
        ;
        quickReturnBackground.animate()
                .translationY(0.0f)
                .alpha(1.0f)
                .setDuration(300)
                .setInterpolator(new EaseInOutBezierInterpolator())
                .start()
        ;
    }
}
