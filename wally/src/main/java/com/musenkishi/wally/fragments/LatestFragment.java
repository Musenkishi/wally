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

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.musenkishi.wally.R;
import com.musenkishi.wally.activities.ImageDetailsActivity;
import com.musenkishi.wally.activities.MainActivity;
import com.musenkishi.wally.adapters.RecyclerImagesAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.musenkishi.wally.observers.FileReceiver.OnFileChangeListener;
import static com.musenkishi.wally.observers.FiltersChangeReceiver.OnFiltersChangeListener;

/**
 * ToplistFragment is responsible for showing the user the toplist of wallpapers.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-02-28
 */
public class LatestFragment extends GridFragment implements RecyclerImagesAdapter.OnSaveButtonClickedListener, Handler.Callback, OnFileChangeListener, OnFiltersChangeListener {

    public static final String TAG = "com.musenkishi.wally.ImagesFragment";

    private static final int MSG_GET_IMAGES = 119;
    private static final int MSG_ERROR_IMAGE_REQUEST = 121;
    private static final int MSG_IMAGES_REQUEST_CREATE = 122;
    private static final int MSG_IMAGES_REQUEST_APPEND = 123;
    private static final int MSG_SAVE_LIST_OF_SAVED_IMAGES = 128;
    private static final int MSG_ERROR_IMAGE_SAVING = 129;
    private static final int MSG_SAVE_BUTTON_CLICKED = 130;
    private static final int MSG_PAGE_RECEIVED = 131;
    private static final String STATE_IMAGES = TAG + ".Images";
    private static final String STATE_CURRENT_PAGE = TAG + ".Current.Page";

    private boolean isLoading;
    private Handler backgroundHandler;
    private Handler uiHandler;
    private HashMap<String, Boolean> savedFiles;
    private int currentPage = 1;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LatestFragment newInstance() {
        LatestFragment fragment = new LatestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LatestFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarColor(getResources().getColor(R.color.Actionbar_Latest_Background));
        setupHandlers();
        getActivity().sendBroadcast(new Intent(FileReceiver.GET_FILES));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        if (rootView != null) {
            super.onCreateView(rootView);
            setupAutoSizeGridView();
            if (savedInstanceState != null && savedInstanceState.containsKey(STATE_IMAGES)){
                Message msgObj = uiHandler.obtainMessage();
                msgObj.what = MSG_IMAGES_REQUEST_CREATE;
                msgObj.arg1 = 1;
                msgObj.obj = savedInstanceState.getParcelableArrayList(STATE_IMAGES);
                uiHandler.sendMessage(msgObj);
                currentPage = savedInstanceState.getInt(STATE_CURRENT_PAGE);
            } else {
                showLoader();
                getImages(1, null);
            }
            ((MainActivity) getActivity()).addOnFileChangedListener(this);
            ((MainActivity) getActivity()).addOnFiltersChangedListener(this);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (imagesAdapter != null) {
            outState.putParcelableArrayList(STATE_IMAGES, imagesAdapter.getImages());
            outState.putInt(STATE_CURRENT_PAGE, currentPage);
        }
        super.onSaveInstanceState(outState);
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
        setInsets(getActivity(), gridView, false, 0, view.getResources().getDimensionPixelSize(R.dimen.gridview_bottom_padding));
    }

    private void getMoreImagesIfNeeded(int position, int totalItemCount) {
        int defaultNumberOfItemsPerPage = NetworkDataProvider.THUMBS_PER_PAGE;
        boolean shouldLoadMore = position >= totalItemCount - (defaultNumberOfItemsPerPage / 2);
        if (shouldLoadMore && !isLoading && imagesAdapter != null && imagesAdapter.getItemCount() > 0) {
            getImages(++currentPage, null);
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

    private void reloadImages() {
        gridView.setAdapter(null);
        showLoader();
        getImages(1, null);
    }

    private void setupHandlers() {
        HandlerThread handlerThread = new HandlerThread("Toplist.background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), this);
        uiHandler = new Handler(getActivity().getMainLooper(), this);
    }

    @Override
    protected void getImages(int index, String query) {
        currentPage = index;
        isLoading = true;

        Message msgGetImages = backgroundHandler.obtainMessage();
        msgGetImages.what = MSG_GET_IMAGES;
        msgGetImages.arg1 = index;
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

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_GET_IMAGES:
                final int index = msg.arg1;
                WallyApplication.getDataProviderInstance().getImages(NetworkDataProvider.PATH_LATEST, index, WallyApplication.getFilterSettings(), new DataProvider.OnImagesReceivedListener() {
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

                    if (saveImageRequest.getDownloadID() != null && getActivity() instanceof MainActivity){
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
                    imagesAdapter.setOnSaveButtonClickedListener(LatestFragment.this);
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
        }
        return false;
    }

    private void setupAdapter() {
        imagesAdapter.setOnGetViewListener(new RecyclerImagesAdapter.OnGetViewListener() {
            @Override
            public void onBindView(int position) {
                getMoreImagesIfNeeded(position, imagesAdapter.getItemCount());
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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

    @Override
    public void onFiltersChange() {
        reloadImages();
    }

    @Override
    public void onFileChange(Map<String, Boolean> existingFiles) {
        Message fileListMessage = uiHandler.obtainMessage();
        fileListMessage.obj = existingFiles;
        fileListMessage.what = MSG_SAVE_LIST_OF_SAVED_IMAGES;
        uiHandler.sendMessage(fileListMessage);
    }
}
