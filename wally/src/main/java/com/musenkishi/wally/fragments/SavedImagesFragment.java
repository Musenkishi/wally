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

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.musenkishi.wally.BuildConfig;
import com.musenkishi.wally.R;
import com.musenkishi.wally.activities.MainActivity;
import com.musenkishi.wally.adapters.RecyclerSavedImagesAdapter;
import com.musenkishi.wally.base.GridFragment;
import com.musenkishi.wally.observers.FileReceiver;
import com.musenkishi.wally.util.SparseBooleanArrayParcelable;

import java.util.ArrayList;

import de.psdev.licensesdialog.LicensesDialogFragment;

/**
 * SavedImagesFragment is responsible to show the user all the wallpapers that has been saved.
 *
 * Created by Musenkishi on 2014-05-11.
 */
public class SavedImagesFragment extends GridFragment implements Handler.Callback, ActionMode.Callback {

    public static final String TAG = "com.musenkishi.wally.SavedImagesFragment";
    private static final String STATE_SELECTED_ITEMS = TAG + ".state.selectedItems";
    private static final int GET_IMAGES_FROM_STORAGE = 357;
    private static final int SET_IMAGES_TO_ADAPTER = 358;
    private static final int UPDATE_ADAPTER = 359;

    private ContentObserver contentObserver;

    private Handler uiHandler;

    private Cursor cursor;

    private RecyclerSavedImagesAdapter recyclerSavedImagesAdapter;
    private ActionMode actionMode;
    private SparseBooleanArrayParcelable selectedItems;

    public static SavedImagesFragment newInstance() {
        SavedImagesFragment fragment = new SavedImagesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarColor(getResources().getColor(R.color.Actionbar_Saved_Background));
        selectedItems = new SparseBooleanArrayParcelable();
        if (savedInstanceState != null) {
            selectedItems = savedInstanceState.getParcelable(STATE_SELECTED_ITEMS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        if (rootView != null) {
            super.onCreateView(rootView);
            setupAutoSizeGridView();
            setupHandlers();
            showLoader();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView.setClipToPadding(false);
        setInsets(getActivity(), gridView, false, 0, 0);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getImages(0, null);
        } else {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.images_saved, menu);

        MenuItem menuItemFilter = menu.findItem(R.id.action_licenses);

        if (menuItemFilter != null) {
            menuItemFilter.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final LicensesDialogFragment fragment = LicensesDialogFragment.newInstance(R.raw.notices, false);
                    fragment.show(getFragmentManager(), null);
                    return false;
                }
            });
        }

        if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release")){
            menu.add("Wally " + BuildConfig.VERSION_NAME);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        getImages(0, null);
        initObserver(cursor);
    }

    @Override
    public void onPause() {
        if (cursor != null && contentObserver != null) {
            cursor.unregisterContentObserver(contentObserver);
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (recyclerSavedImagesAdapter != null) {
            outState.putParcelable(STATE_SELECTED_ITEMS, recyclerSavedImagesAdapter.getSelectedItems());
        }
        super.onSaveInstanceState(outState);
    }

    private void initObserver(Cursor cursor) {
        if (cursor != null) {
            if (contentObserver == null) {
                contentObserver = new ContentObserver(uiHandler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        getImages(0, null);
                    }
                };

            }
            try {
                cursor.registerContentObserver(contentObserver);
            } catch (IllegalStateException e) {
                //Oh goodie, it's already registered. Nevermind then.
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setupHandlers() {
        uiHandler = new Handler(getActivity().getMainLooper(), this);
    }

    @Override
    protected void getImages(int index, String query) {
        if (!uiHandler.hasMessages(GET_IMAGES_FROM_STORAGE)) {
            uiHandler.sendEmptyMessage(GET_IMAGES_FROM_STORAGE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        cursor.close();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case GET_IMAGES_FROM_STORAGE:
                if (getActivity() != null) {
                    String[] projection = {MediaStore.Images.Media._ID};
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                    cursor = contentResolver.query(
                            mImageUri,
                            projection,
                            MediaStore.Images.Media.DATA + " like ? ",
                            new String[] {"%/Wally/%"},
                            MediaStore.Audio.Media.DATE_ADDED + " DESC");

                    initObserver(cursor);

                    int what;
                    if (recyclerSavedImagesAdapter == null) {
                        what = SET_IMAGES_TO_ADAPTER;
                    } else {
                        what = UPDATE_ADAPTER;
                    }
                    uiHandler.sendEmptyMessage(what);
                }
                break;

            case SET_IMAGES_TO_ADAPTER:
                hideLoader();
                recyclerSavedImagesAdapter = new RecyclerSavedImagesAdapter(getFileUrisFromCursor(cursor), itemSize, selectedItems);
                gridView.setAdapter(recyclerSavedImagesAdapter);
                setupAdapter(recyclerSavedImagesAdapter);
                gridView.scheduleLayoutAnimation();
                break;

            case UPDATE_ADAPTER:
                ArrayList<Uri> newFilePaths = getFileUrisFromCursor(cursor);
                ArrayList<Uri> oldFilePaths = recyclerSavedImagesAdapter.getData();
                recyclerSavedImagesAdapter.setData(newFilePaths);
                checkIfAddedOrRemovedItem(oldFilePaths, newFilePaths);
                break;

            default:
                break;
        }
        return false;
    }

    private ArrayList<Uri> getFileUrisFromCursor(Cursor cursor) {
        ArrayList<Uri> filePaths = new ArrayList<Uri>();
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            while (cursor.moveToNext()) {
                int imageID = cursor.getInt(columnIndex);
                Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imageID));
                filePaths.add(uri);
            }
        }
        return filePaths;
    }

    private void checkIfAddedOrRemovedItem(ArrayList<Uri> oldList, ArrayList<Uri> newList) {
        ArrayList<Uri> oldCompList = new ArrayList<Uri>(oldList);
        ArrayList<Uri> newCompList = new ArrayList<Uri>(newList);

        oldCompList.removeAll(newList);
        if (oldCompList.size() > 0) { //Items removed
            recyclerSavedImagesAdapter.notifyItemRangeRemoved(
                    oldList.indexOf(oldCompList.get(0)),
                    oldCompList.size()
            );
        }

        newCompList.removeAll(oldList);
        if (newCompList.size() > 0) { //Items added
            recyclerSavedImagesAdapter.notifyItemRangeInserted(
                    newList.indexOf(newCompList.get(0)),
                    newCompList.size()
            );
        }
    }

    public void setupAdapter(RecyclerSavedImagesAdapter adapter) {
        adapter.setOnItemClickListener(new RecyclerSavedImagesAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (recyclerSavedImagesAdapter.getSelectedItemCount() > 0) {
                    toggleSelection(position);
                } else {
                    Uri contentUri = recyclerSavedImagesAdapter.getItem(position);
                    Uri fileUri = Uri.parse("file://" + getRealPathFromURI(view.getContext(), contentUri));

                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager != null) {
                        ImageZoomFragment imageZoomFragment = ImageZoomFragment.newInstance(fileUri, position);
                        imageZoomFragment.showFileOptions(fileUri, contentUri);
                        imageZoomFragment.show(fragmentManager, ImageZoomFragment.TAG);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                toggleSelection(position);
            }
        });
        toggleActionMode();
    }

    private void toggleSelection(int position) {
        recyclerSavedImagesAdapter.toggleSelection(position);
        toggleActionMode();
    }

    private void toggleActionMode() {
        if (recyclerSavedImagesAdapter.getSelectedItemCount() > 0) {
            if (actionMode == null) {
                actionMode = ((MainActivity) getActivity()).getSupportActionBar().startActionMode(this);
            }
            actionMode.setTitle(recyclerSavedImagesAdapter.getSelectedItemCount() + " checked");
        } else {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.saved_images_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        if (R.id.menu_delete == item.getItemId()) {

            int size = 0;

            SparseBooleanArray checkedItems = recyclerSavedImagesAdapter.getSelectedItems();
            for (int i = 0; i < checkedItems.size(); i++) {
                if (checkedItems.valueAt(i)) {
                    size++;
                }
            }

            String quantityString = getResources().getQuantityString(R.plurals.wallpapers, size);
            String title = String.format(getString(R.string.dialog_delete_title), quantityString);

            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {
                final MaterialDialogFragment materialDialogFragment = new MaterialDialogFragment();
                materialDialogFragment.setPrimaryColor(getResources().getColor(R.color.Dialog_Button_Delete));
                materialDialogFragment.setTitle(title);
                materialDialogFragment.setPositiveButton(R.string.dialog_delete_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SparseBooleanArray checkedItems = recyclerSavedImagesAdapter.getSelectedItems();
                        for (int index = 0; index < checkedItems.size(); index++) {
                            if (checkedItems.valueAt(index)) {
                                Uri contentUri = recyclerSavedImagesAdapter.getItem(checkedItems.keyAt(index));
                                getActivity().getContentResolver().delete(contentUri, null, null);
                            }
                        }
                        mode.finish();
                        getImages(0, null);
                        getActivity().sendBroadcast(new Intent(FileReceiver.GET_FILES));
                    }
                });
                materialDialogFragment.setNegativeButton(R.string.dialog_delete_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        materialDialogFragment.dismiss();
                    }
                });
                materialDialogFragment.show(fragmentManager, MaterialDialogFragment.TAG);
            }

        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        recyclerSavedImagesAdapter.clearSelections();
        actionMode = null;
    }
}
