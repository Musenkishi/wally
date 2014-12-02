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

package com.musenkishi.wally.dataprovider;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.musenkishi.wally.dataprovider.models.DataProviderError;
import com.musenkishi.wally.dataprovider.models.SaveImageRequest;
import com.musenkishi.wally.dataprovider.util.Parser;
import com.musenkishi.wally.models.ExceptionReporter;
import com.musenkishi.wally.models.Filter;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;
import com.musenkishi.wally.models.filters.FilterGroupsStructure;

import java.io.File;
import java.util.ArrayList;

import static com.musenkishi.wally.dataprovider.NetworkDataProvider.OnDataReceivedListener;

/**
 * <strong>No threading shall take place here.</strong>
 * Use this class to get and set data.
 * Created by Musenkishi on 2014-02-28.
 */
public class DataProvider {

    private static final String TAG = "DataProvider";
    private SharedPreferencesDataProvider sharedPreferencesDataProvider;
    private DownloadManager downloadManager;
    private Parser parser;

    public interface OnImagesReceivedListener {
        abstract void onImagesReceived(ArrayList<Image> images);

        abstract void onError(DataProviderError dataProviderError);
    }

    public interface OnPageReceivedListener {
        abstract void onPageReceived(ImagePage imagePage);

        abstract void onError(DataProviderError dataProviderError);
    }

    public DataProvider(Context context, ExceptionReporter.OnReportListener onReportListener) {
        sharedPreferencesDataProvider = new SharedPreferencesDataProvider(context);
        parser = new Parser(onReportListener);
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public SharedPreferencesDataProvider getSharedPreferencesDataProviderInstance(){
        return sharedPreferencesDataProvider;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public void getImages(String path, String query, String color, int index, FilterGroupsStructure filterGroupsStructure, final OnImagesReceivedListener onImagesReceivedListener) {
        new NetworkDataProvider().getData(path, query, color, index, filterGroupsStructure, new OnDataReceivedListener() {
            @Override
            public void onData(String data, String url) {
                ArrayList<Image> images = parser.parseImages(data);
                if (onImagesReceivedListener != null) {
                    onImagesReceivedListener.onImagesReceived(images);
                }
            }

            @Override
            public void onError(DataProviderError dataProviderError) {
                if (onImagesReceivedListener != null) {
                    onImagesReceivedListener.onError(dataProviderError);
                }
            }
        });
    }

    public ArrayList<Image> getImagesSync(String path, int index, FilterGroupsStructure filterGroupsStructure){
        String data = new NetworkDataProvider().getDataSync(path, index, filterGroupsStructure);
        if (data != null) {
            return parser.parseImages(data);
        } else {
            return null;
        }
    }

    /**
     */
    public void getImages(String path, int index, FilterGroupsStructure filterGroupsStructure, final OnImagesReceivedListener onImagesReceivedListener) {

        new NetworkDataProvider().getData(path, index, filterGroupsStructure, new OnDataReceivedListener() {
            @Override
            public void onData(String data, String url) {
                ArrayList<Image> images = parser.parseImages(data);
                if (onImagesReceivedListener != null) {
                    if (!images.isEmpty()){
                        onImagesReceivedListener.onImagesReceived(images);
                    } else {
                        DataProviderError noImagesError = new DataProviderError(DataProviderError.Type.LOCAL, 204, "No images");
                        onImagesReceivedListener.onError(noImagesError);
                    }
                }
            }

            @Override
            public void onError(DataProviderError dataProviderError) {
                if (onImagesReceivedListener != null) {
                    onImagesReceivedListener.onError(dataProviderError);
                }
            }
        });
    }

    public ImagePage getPageDataSync(String imagePageUrl){
        String data = new NetworkDataProvider().getDataSync(imagePageUrl);
        return parser.parseImagePage(data, imagePageUrl);
    }

    public void getPageData(String imagePageUrl, final OnPageReceivedListener onPageReceivedListener) {
        new NetworkDataProvider().getData(imagePageUrl, new OnDataReceivedListener() {
            @Override
            public void onData(String data, String url) {
                ImagePage imagePage = parser.parseImagePage(data, url);

                if (onPageReceivedListener != null) {
                    onPageReceivedListener.onPageReceived(imagePage);
                }
            }

            @Override
            public void onError(DataProviderError error) {
                if (onPageReceivedListener != null) {
                    onPageReceivedListener.onError(error);
                }
            }
        });
    }

    public void setTimeSpan(String tag, Filter<String, String> timespan){
        sharedPreferencesDataProvider.setTimespan(tag, timespan);
    }

    public Filter<String, String> getTimespan(String tag){
        return sharedPreferencesDataProvider.getTimespan(tag);
    }

    public void setBoards(String tag, String paramValue){
        sharedPreferencesDataProvider.setBoards(tag, paramValue);
    }

    public String getBoards(String tag) {
        return sharedPreferencesDataProvider.getBoards(tag);
    }

    public void setPurity(String tag, String paramValue){
        sharedPreferencesDataProvider.setPurity(tag, paramValue);
    }

    public String getPurity(String tag) {
        return sharedPreferencesDataProvider.getPurity(tag);
    }

    public void setAspectRatio(String tag, Filter<String, String> aspectRatio){
        sharedPreferencesDataProvider.setAspectRatio(tag, aspectRatio);
    }

    public Filter<String, String> getAspectRatio(String tag) {
        return sharedPreferencesDataProvider.getAspectRatio(tag);
    }

    public void setResolutionOption(String tag, String paramValue){
        sharedPreferencesDataProvider.setResolutionOption(tag, paramValue);
    }

    public String getResolutionOption(String tag) {
        return sharedPreferencesDataProvider.getResolutionOption(tag);
    }

    public void setResolution(String tag, Filter<String, String> resolution){
        sharedPreferencesDataProvider.setResolution(tag, resolution);
    }

    public Filter<String, String> getResolution(String tag) {
        return sharedPreferencesDataProvider.getResolution(tag);
    }

    public SaveImageRequest downloadImageIfNeeded(Uri path, String filename, String notificationTitle){
        FileManager fileManager = new FileManager();

        if (fileManager.fileExists(filename)){
            File file = fileManager.getFile(filename);
            Uri fileUri = Uri.fromFile(file);
            return new SaveImageRequest(fileUri);
        } else {

            String type = ".png"; //fallback to ".png"
            if (path.toString().lastIndexOf(".") != -1) { //-1 means there are no punctuations in the path
                type = path.toString().substring(path.toString().lastIndexOf("."));
            }

            DownloadManager.Request request = new DownloadManager.Request(path);
            request.setTitle(notificationTitle);
            request.setVisibleInDownloadsUi(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.allowScanningByMediaScanner();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "/Wally/" + filename + type);

            return new SaveImageRequest(downloadManager.enqueue(request));
        }
    }

    public Uri getFilePath(String filename) {
        FileManager fileManager = new FileManager();
        if (fileManager.fileExists(filename)) {
            File file = fileManager.getFile(filename);
            Uri fileUri = Uri.fromFile(file);
            return fileUri;
        }
        return null;
    }
}
