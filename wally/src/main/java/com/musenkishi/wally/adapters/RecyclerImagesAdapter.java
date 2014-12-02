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

package com.musenkishi.wally.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.musenkishi.wally.R;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.util.PaletteLoader;
import com.musenkishi.wally.util.PaletteRequest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter that handles images coming from backend.
 *
 * Created by Musenkishi on 2014-02-28.
 */
public class RecyclerImagesAdapter extends RecyclerView.Adapter<RecyclerImagesAdapter.ViewHolder> {

    protected int itemSize;

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }

    private OnSaveButtonClickedListener onSaveButtonClickedListener;
    private OnItemClickListener onItemClickListener;
    private OnGetViewListener onGetViewListener;

    private ArrayList<Image> images;
    private int barHeight;

    private HashMap<String, Boolean> existingFiles = new HashMap<String, Boolean>();
    private SparseArray<ValueAnimator> valueAnimators = new SparseArray<ValueAnimator>();

    /**
     * Don't use this constructor.
     */
    public RecyclerImagesAdapter() {
        throw new NoDataException("No data set. Did you use the correct constructor?");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.view_cell_thumb_tile, viewGroup, false);
        if (barHeight == 0){
            barHeight = view.getResources().getDimensionPixelSize(R.dimen.default_height);
        }
        view.getLayoutParams().height = itemSize + barHeight;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        if (onGetViewListener != null) {
            onGetViewListener.onBindView(position);
        }
        final Image image = getItem(position);

        if (image != null){

            viewHolder.bottomBar.setBackgroundColor(viewHolder.bottomBar.getContext().getResources().getColor(R.color.Transparent));
            viewHolder.textViewResolution.setTextColor(viewHolder.bottomBar.getContext().getResources().getColor(R.color.Thumb_Text));

            RequestListener<String, GlideDrawable> glideDrawableRequestListener = new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }
                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    Bitmap bitmap = ((GlideBitmapDrawable) resource).getBitmap();
                    if (bitmap != null) {
                        Context context = viewHolder.bottomBar.getContext();
                        PaletteLoader.with(context, model)
                                .load(bitmap)
                                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.REGULAR_VIBRANT, PaletteRequest.SwatchColor.BACKGROUND))
                                .into(viewHolder.bottomBar);
                        PaletteLoader.with(context, model)
                                .load(bitmap)
                                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.REGULAR_VIBRANT, PaletteRequest.SwatchColor.TEXT_TITLE))
                                .into(viewHolder.textViewResolution);
                        PaletteLoader.with(context, model)
                                .load(bitmap)
                                .fallbackColor(viewHolder.textViewResolution.getCurrentTextColor())
                                .setPaletteRequest(new PaletteRequest(PaletteRequest.SwatchType.REGULAR_VIBRANT, PaletteRequest.SwatchColor.TEXT_TITLE))
                                .mask()
                                .into(viewHolder.imageButton);
                    }
                    return false;
                }
            };

            Glide.with(viewHolder.bottomBar.getContext())
                    .load(image.thumbURL())
                    .fitCenter()
                    .placeholder(R.color.Transparent)
                    .listener(glideDrawableRequestListener)
                    .into(viewHolder.imageView);

            viewHolder.textViewResolution.setText(image.resolution());

            if (existingFiles != null && existingFiles.containsKey(image.imageId())) {
                if (valueAnimators.get(position) != null) {
                    valueAnimators.get(position).cancel();
                }
                viewHolder.imageButton.setImageResource(R.drawable.ic_action_heart_full);
                viewHolder.imageButton.getDrawable().mutate().setColorFilter(viewHolder.textViewResolution.getCurrentTextColor(), PorterDuff.Mode.MULTIPLY);
                viewHolder.imageButton.getDrawable().mutate().setAlpha(255);
                viewHolder.imageButton.setOnClickListener(null);
            } else if (WallyApplication.getDownloadIDs().containsValue(image.imageId())) {
                viewHolder.imageButton.setOnClickListener(null);
                animateDownload(viewHolder, position);
            } else {
                viewHolder.imageButton.setImageResource(R.drawable.ic_action_heart_empty);
                viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onSaveButtonClickedListener != null) {
                            v.setOnClickListener(null);
                            onSaveButtonClickedListener.onSaveButtonClicked(image);
                            animateDownload(viewHolder, position);
                        }
                    }
                });
                viewHolder.imageButton.getDrawable().mutate().setColorFilter(viewHolder.textViewResolution.getCurrentTextColor(), PorterDuff.Mode.MULTIPLY);
            }

        }

        viewHolder.itemView.getLayoutParams().height = itemSize + barHeight;
        viewHolder.itemView.getLayoutParams().width = itemSize;
    }

    private void animateDownload(final ViewHolder viewHolder, int position) {
        viewHolder.imageButton.setImageResource(R.drawable.ic_heart_download_animation);
        if (valueAnimators.get(position) != null) {
            valueAnimators.get(position).cancel();
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 10000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (viewHolder.imageButton.getDrawable() instanceof LayerDrawable) {
                    LayerDrawable layerDrawable = (LayerDrawable) viewHolder.imageButton.getDrawable();
                    layerDrawable.getDrawable(0).mutate().setColorFilter(viewHolder.textViewResolution.getCurrentTextColor(), PorterDuff.Mode.MULTIPLY);
                    if (layerDrawable.getDrawable(layerDrawable.getNumberOfLayers()-1) instanceof ClipDrawable) {
                        ClipDrawable clipDrawable = (ClipDrawable) layerDrawable.getDrawable(layerDrawable.getNumberOfLayers()-1);
                        clipDrawable.mutate().setColorFilter(viewHolder.textViewResolution.getCurrentTextColor(), PorterDuff.Mode.MULTIPLY);
                        clipDrawable.mutate().setLevel((Integer) animation.getAnimatedValue());
                        float reversedValue = 10000f - ((Integer) animation.getAnimatedValue());
                        float alphaValue = reversedValue / 10000f;
                        if (alphaValue > 0.5f) {
                            clipDrawable.mutate().setAlpha(255);
                        } else {
                            float f = alphaValue * 2;
                            clipDrawable.mutate().setAlpha(f == 1.0f ? 255 : (int) (f * 256.0f));
                        }
                    }
                }
            }
        });
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setDuration(500);
        valueAnimator.start();
        valueAnimators.append(position, valueAnimator);
    }

    public RecyclerImagesAdapter(ArrayList<Image> images, int itemSize) {
        this.images = images;
        this.itemSize = itemSize;
    }

    public Image getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setOnSaveButtonClickedListener(OnSaveButtonClickedListener onSaveButtonClickedListener) {
        this.onSaveButtonClickedListener = onSaveButtonClickedListener;
    }

    public void updateSavedFilesList(HashMap<String, Boolean> savedFilesList) {
        existingFiles = savedFilesList;
    }

    /**
     * Will loop through all items in the adapter and check if any are included in the existing files map.
     * For each match, a {@code notifyItemChanged()} will be called.
     */
    public void notifySavedItemsChanged() {
        for (int i = 0; i < getItemCount(); i++) {
            Image image = getItem(i);
            if (existingFiles.containsKey(image.imageId())){
                notifyItemChanged(i);
            }
        }
    }

    public interface OnSaveButtonClickedListener {
        abstract void onSaveButtonClicked(Image image);
    }

    private class NoDataException extends NullPointerException {
        private NoDataException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView imageView;
        ViewGroup bottomBar;
        ImageButton imageButton;
        TextView textViewResolution;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.thumb_image_view);
            imageButton = (ImageButton) itemView.findViewById(R.id.thumb_button_heart);
            textViewResolution = (TextView) itemView.findViewById(R.id.thumb_text_resolution);
            bottomBar = (ViewGroup) itemView.findViewById(R.id.thumb_bottom_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getPosition());
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        abstract void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnGetViewListener {
        abstract void onBindView(int position);
    }

    public void setOnGetViewListener(OnGetViewListener onGetViewListener) {
        this.onGetViewListener = onGetViewListener;
    }
}
