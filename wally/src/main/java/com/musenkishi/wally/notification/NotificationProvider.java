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

package com.musenkishi.wally.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import com.musenkishi.wally.R;
import com.musenkishi.wally.models.SavedImageData;

/**
 * A class for managing all notifications in the app.
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-03-04.
 */
public class NotificationProvider {

    private static final int NOTIFICATION_ID_DEFAULT = 98172421;

    public NotificationProvider() {
    }

    public Notification buildProgressNotification(Context context, String ticker, String title){
        Notification notification;
        notification = new Notification.Builder(context)
                .setTicker(ticker)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification_launcher)
                .setOngoing(true)
                .setProgress(100, 0, true)
                .getNotification();
        return notification;
    }

    public Notification buildBigPictureNotification(Context context, SavedImageData imageData, String ticker, String title){
        Notification notification;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(imageData.path(), "image/*");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Bitmap largeIconCroppedBitmap = cropSquaredBitmap(imageData.bitmap());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.BigPictureStyle(
                    new Notification.Builder(context)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setTicker(ticker)
                            .setContentTitle(title)
                            .setSmallIcon(R.drawable.ic_notification_launcher)
                            .setLargeIcon(Bitmap.createScaledBitmap(largeIconCroppedBitmap,
                                    (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width),
                                    (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height), false))
                            .setContentIntent(pendingIntent))
                    .bigPicture(imageData.bitmap())
                    .build();
        } else {
            notification = new Notification.Builder(context)
                    .setTicker(context.getResources().getString(R.string.notification_ticker_image_saved))
                    .setContentTitle(context.getResources().getString(R.string.notification_title_image_saved))
                    .setSmallIcon(R.drawable.ic_notification_launcher)
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(Bitmap.createScaledBitmap(largeIconCroppedBitmap,
                            (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width),
                            (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height), false))
                    .getNotification();
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    private Bitmap cropSquaredBitmap(Bitmap bitmap) {
        Bitmap largeIconCroppedBitmap;
        if (bitmap.getWidth() >= bitmap.getHeight()){
            largeIconCroppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth()/2 - bitmap.getHeight()/2,
                    0,
                    bitmap.getHeight(),
                    bitmap.getHeight()
            );
        }else{
            largeIconCroppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight()/2 - bitmap.getWidth()/2,
                    bitmap.getWidth(),
                    bitmap.getWidth()
            );
        }
        return largeIconCroppedBitmap;
    }

    public void notify(Context context, Notification notification){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_DEFAULT, notification);
    }

    public void cancelAll(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

}
