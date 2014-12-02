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

package com.musenkishi.wally.base;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.musenkishi.wally.R;
import com.musenkishi.wally.adapters.RecyclerImagesAdapter;
import com.musenkishi.wally.dataprovider.models.DataProviderError;
import com.musenkishi.wally.util.TextLinkBuilder;
import com.musenkishi.wally.views.AutoGridView;

/**
 * A base class where common logic for gridview based fragments is placed.
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-10-07.
 */
public abstract class GridFragment extends BaseFragment {

    public static final short REQUEST_CODE = 25380;

    private ViewGroup errorLayout;
    protected AutoGridView gridView;
    protected View progressBar;
    protected RecyclerImagesAdapter imagesAdapter;

    protected int itemSize;
    protected String query;
    private GridLayoutManager gridLayoutManager;

    /**
     * Call this to setup basic views like the gridview.
     * @param rootView
     */
    protected void onCreateView(@NonNull View rootView) {
        errorLayout = (ViewGroup) rootView.findViewById(R.id.error_layout);
        gridView = (AutoGridView) rootView.findViewById(R.id.listview);

        gridLayoutManager = new GridLayoutManager(rootView.getContext(), 2);
        gridView.setLayoutManager(gridLayoutManager);
        gridView.getItemAnimator().setSupportsChangeAnimations(true);

        progressBar = rootView.findViewById(R.id.loader);
        if (progressBar != null) {
            progressBar.setAlpha(0.0f);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && imagesAdapter != null) {
            gridView.post(new Runnable() {
                @Override
                public void run() {
                    imagesAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    protected void showLoader() {
        progressBar.animate().alpha(1.0f).setDuration(300).start();
    }

    protected void hideLoader() {
        progressBar.animate().alpha(0.0f).setDuration(300).start();
    }

    protected void setupAutoSizeGridView() {
        final ViewTreeObserver vto = gridView.getViewTreeObserver();
        if (vto != null) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                int lastWidth = -1;

                @Override
                public void onGlobalLayout() {
                    int width = gridView.getWidth() - gridView.getPaddingLeft() - gridView.getPaddingRight();
                    if (width == lastWidth || width <= 0) {
                        return;
                    }

                    // Compute number of columns
                    int maxItemWidth = gridView.getDefaultCellWidth();
                    int numColumns = 1;
                    while (true) {
                        if (width / numColumns > maxItemWidth) {
                            ++numColumns;
                        } else {
                            break;
                        }
                    }

                    itemSize = width / numColumns;
                    if (imagesAdapter != null) {
                        imagesAdapter.setItemSize(itemSize);
                    }
                    gridLayoutManager.setSpanCount(numColumns);

                }
            });
        }
    }

    protected abstract void getImages(int index, String query);

    protected void showErrorMessage(DataProviderError dataProviderError, int index) {
        if (dataProviderError.getType().equals(DataProviderError.Type.LOCAL) && dataProviderError.getHttpStatusCode() == 204){
            //TODO Even if we don't show an error, it's still requesting images... find a way to stop it.
            if (index == 1){
                //No images was found with current filter and search settings

            }
        } else if (errorLayout != null) {

            int numberOfRetries = (errorLayout.getTag() instanceof Integer) ?
                    (Integer) errorLayout.getTag()
                    : 1;

            hideLoader();
            gridView.setVisibility(View.GONE);

            TextView message = (TextView) errorLayout.findViewById(R.id.error_backend_textview_message);
            TextView status = (TextView) errorLayout.findViewById(R.id.error_backend_textview_status);
            TextView checkBackend = (TextView) errorLayout.findViewById(R.id.error_backend_textview_check_backend);

            if (numberOfRetries >= 3) {
                TextLinkBuilder backendTextLinkBuilder = new TextLinkBuilder(
                        errorLayout.getContext(),
                        R.string.error_backend_check_backend,
                        R.string.error_backend_check_backend
                );
                backendTextLinkBuilder.color(getResources().getColor(R.color.Material_Blue_500));
                backendTextLinkBuilder.onClick(new TextLinkBuilder.OnTextClickedListener() {
                    @Override
                    public void onClick(View textView) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://alpha.wallhaven.cc"));
                        startActivity(browserIntent);
                    }
                });
                checkBackend.setMovementMethod(LinkMovementMethod.getInstance());
                checkBackend.setText(backendTextLinkBuilder.build());
                checkBackend.setVisibility(View.VISIBLE);
            } else {
                checkBackend.setVisibility(View.GONE);
            }

            TextLinkBuilder textLinkBuilder = new TextLinkBuilder(errorLayout.getContext(), R.string.error_backend_message_text, R.string.error_backend_message_text_link);
            textLinkBuilder.color(getResources().getColor(R.color.Material_Blue_500));
            message.setTag(index);
            textLinkBuilder.onClick(new TextLinkBuilder.OnTextClickedListener() {
                @Override
                public void onClick(View textView) {
                    int index = (Integer) textView.getTag();
                    errorLayout.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                    showLoader();
                    getImages(index, query);
                    int numberOfRetries = (errorLayout.getTag() instanceof Integer) ?
                            (Integer) errorLayout.getTag()
                            : 1;
                    numberOfRetries++;
                    errorLayout.setTag(numberOfRetries);
                }
            });
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setText(textLinkBuilder.build());

            if (dataProviderError != null) {
                status.setText(dataProviderError.getHttpStatusCode() + " " + dataProviderError.getMessage());
            } else {
                status.setVisibility(View.GONE);
            }
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

}
