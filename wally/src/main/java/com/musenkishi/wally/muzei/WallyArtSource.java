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

package com.musenkishi.wally.muzei;

import android.content.Intent;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.dataprovider.NetworkDataProvider;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;

import java.util.ArrayList;
import java.util.Random;

/**
 * A very basic Muzei extension, providing the top number of wallpapers from the toplist.
 * It's based on the user's filter preferences.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-08-23.
 */
public class WallyArtSource extends RemoteMuzeiArtSource {

    public static final String TAG = "WallyArtSource";

    private static final String SOURCE_NAME = "WallyArtSource";
    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours

    public WallyArtSource(){
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        final ArrayList<Image> images = WallyApplication.getDataProviderInstance().getImagesSync(NetworkDataProvider.PATH_TOPLIST, 1, WallyApplication.getFilterSettings());
        if (images != null) {
            if (images.size() == 0) {
                scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                return;
            }
            pickImage(images);
        } else {
            Log.e(TAG, "images was null");
            throw new RetryException();
        }
    }

    private void pickImage(ArrayList<Image> images) throws RetryException {
        final Random random = new Random();
        final Image image = images.get(random.nextInt(images.size()));
        final String newToken = image.imageId();
        getImageAndPublish(image, newToken);
    }

    private void getImageAndPublish(final Image image, final String newToken) throws RetryException {
        ImagePage imagePage = WallyApplication.getDataProviderInstance().getPageDataSync(image.imagePageURL());

        if (imagePage != null) {
            publishArtwork(new Artwork.Builder()
                    .title(imagePage.title())
                    .byline(image.imagePageURL())
                    .imageUri(imagePage.imagePath())
                    .token(newToken)
                    .viewIntent(new Intent(Intent.ACTION_VIEW,
                            image.generateWallyUri()))
                    .build());

            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
        } else {
            throw new RetryException();
        }
    }

}
