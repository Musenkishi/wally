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

package com.musenkishi.wally.dataprovider.util;

import android.net.Uri;
import android.text.TextUtils;

import com.musenkishi.wally.models.Author;
import com.musenkishi.wally.models.ExceptionReporter;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;
import com.musenkishi.wally.models.Tag;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Iterator;

import static com.musenkishi.wally.dataprovider.NetworkDataProvider.WALLHAVEN_BASE_URL;

/**
 * A class for parsing data from Wallhaven to usable objects.
 * Uses SharedPreferencesProvider for knowing whether to send Crashlytics logs or do nothing.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-10-16.
 */
public class Parser {

    private ExceptionReporter.OnReportListener onReportListener;

    public Parser(ExceptionReporter.OnReportListener onReportListener) {
        this.onReportListener = onReportListener;
    }

    public ArrayList<Image> parseImages(String data) {
        ArrayList<Image> images = new ArrayList<Image>();
        Document document = Jsoup.parse(data, WALLHAVEN_BASE_URL);
        Iterator thumbIterator = document.select("section.thumb-listing-page figure").iterator();

        while (thumbIterator.hasNext()) {
            Element wrapperElement = (Element) thumbIterator.next();

            //Get the resolution
            Element resElement = wrapperElement.getElementsByClass("wall-info").first();
            String resolution = resElement.select("span.wall-res").text();

            //Get the image with link
            Element linkElement = wrapperElement.select("a.preview").first();
            String imagePageURL = linkElement.absUrl("href");
            Element localElement2 = wrapperElement.getElementsByTag("img").first();
            if (localElement2 != null) {
                String thumbURL = localElement2.attr("data-src");
                if ((!TextUtils.isEmpty(imagePageURL)) && (!TextUtils.isEmpty(thumbURL))) {
                    Uri pageUri = Uri.parse(imagePageURL);
                    String imageId = pageUri.getLastPathSegment();
                    Image localThumbnail = Image.create(imageId, thumbURL, imagePageURL, resolution);
                    images.add(localThumbnail);
                }
            }
        }
        return images;
    }

    public ImagePage parseImagePage(String data, String url) {
        Document document = Jsoup.parse(data, WALLHAVEN_BASE_URL);

        String title = document.head().select("title").text();

        //All titles from wallhaven ends with 'wallpaper (#123456) / Wallhaven.cc
        // which takes up unnecessary space.
        String[] strings = title.split("Wallpaper");
        title = strings[0].trim();

        String idUri = Uri.parse(url).getLastPathSegment();

        String categoryMatch = "div.sidebar-section dl dd";
        String category = "Unknown";
        try {
            Element categoryElement = document.select(categoryMatch).get(2);
            category = categoryElement.text();
        } catch (IndexOutOfBoundsException e) {
            reportCrash(categoryMatch, e);
        }

        Element sfwElement = document.select("fieldset.framed input").first();
        String rating = "Unknown";
        if (sfwElement != null) {
            if (sfwElement.toString().contains("checked")){
                rating = "SFW";
            } else {
                rating = "Sketchy";
            }
        }

        String uploaderMatch = "div.sidebar-section dl dd";
        String uploader = "Unknown";
        try {
            Element uploaderElement = document.select(uploaderMatch).get(5);
            uploader = uploaderElement.text();
            if (uploader != null) {
                uploader = uploader.trim();
            }
        } catch (IndexOutOfBoundsException e) {
            reportCrash(uploaderMatch, e);
        }

        String uploadDateMatch = "div.sidebar-section dl dd";
        String uploadDate = "Unknown";
        try {
            Element uploadDateElement = document.select(uploadDateMatch).get(6);
            if (uploadDateElement.text() != null) {
                uploadDate = uploadDateElement.text();
            }
        } catch (IndexOutOfBoundsException e) {
            reportCrash(uploadDateMatch, e);
        }

        String sourceMatch = "div.sidebar-section dl dd";
        Author author = Author.create("Unknown", Uri.EMPTY);
        try {
            Element sourceElement = document.select(sourceMatch).get(7);

            String linkText = sourceElement.text();
            String linkHref = sourceElement.select("a").attr("href");
            if (!TextUtils.isEmpty(linkText)) {
                Uri uri = Uri.EMPTY;
                if (!TextUtils.isEmpty(linkHref)){
                    uri = Uri.parse(linkHref);
                }
                author = Author.create(linkText, uri);
            }
        } catch (IndexOutOfBoundsException e) {
            reportCrash(sourceMatch, e);
        }

        Element imageUrlElement = document.select("section#showcase img#wallpaper").first();
        String resolution = "0 x 0";
        Uri imagePath = Uri.EMPTY;
        if (imageUrlElement != null) {
            String width = imageUrlElement.attr("data-wallpaper-width");
            String height = imageUrlElement.attr("data-wallpaper-height");
            resolution = width + " x " + height;
            String imageUrl = imageUrlElement.attr("src");
            imagePath = Uri.parse(imageUrl);

            if (imagePath.getScheme() == null || !imagePath.getScheme().equalsIgnoreCase("http")
                    || !imagePath.getScheme().equalsIgnoreCase("https")) {
                imagePath = imagePath.buildUpon().scheme("http").build();
            }
        }

        ArrayList<Tag> tags = getTags(document);

        return ImagePage.create(title, idUri, imagePath, resolution, category, rating, uploader, uploadDate, author, tags);
    }

    private ArrayList<Tag> getTags(Document document) {

        ArrayList<Tag> tags = new ArrayList<>();

        Iterator thumbIterator = document.select("ul#tags li").iterator();

        while (thumbIterator.hasNext()) {
            Element wrapperElement = (Element) thumbIterator.next();
            Element tagNameElement = wrapperElement.select("a.tagname").first();
            String tagname = tagNameElement.text().trim();
            Tag tag = Tag.create(tagname);
            tags.add(tag);
        }

        return tags;
    }

    private void reportCrash(String match, Exception e) {
        onReportListener.report(getClass(), match, e.getMessage());
    }

}
