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

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.musenkishi.wally.R;
import com.musenkishi.wally.util.SparseBooleanArrayParcelable;

import java.util.ArrayList;

/**
 * Adapter that handles images stored on the device.
 *
 * Created by Musenkishi on 2014-02-28.
 */
public class RecyclerSavedImagesAdapter extends RecyclerView.Adapter<RecyclerSavedImagesAdapter.ViewHolder> {

    protected int itemSize;
    public void setItemSize(int itemSize) { this.itemSize = itemSize; }

    private ArrayList<Uri> filePaths;

    private OnItemClickListener onItemClickListener;

    private SparseBooleanArrayParcelable selectedItems;

    /**
     * Don't use this constructor.
     */
    public RecyclerSavedImagesAdapter() {
        throw new NoDataException("No data set. Did you use the correct constructor?");
    }

    public RecyclerSavedImagesAdapter(ArrayList<Uri> filePaths, int itemSize, SparseBooleanArrayParcelable selectedItems) {
        this.filePaths = filePaths;
        this.itemSize = itemSize;
        this.selectedItems = selectedItems;
    }

    public void setData(ArrayList<Uri> filePaths) {
        this.filePaths = filePaths;
    }

    public ArrayList<Uri> getData() {
        return filePaths;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = inflater.inflate(R.layout.view_cell_thumb_tile_saved, viewGroup, false);

        convertView.getLayoutParams().height = itemSize;

        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.thumb_image_view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Glide.with(viewHolder.itemView.getContext())
                .load(filePaths.get(position))
                .placeholder(R.color.Transparent)
                .into(viewHolder.imageView);

        viewHolder.itemView.setSelected(getSelectedItems().get(position));

        viewHolder.itemView.getLayoutParams().height = itemSize;
        viewHolder.itemView.getLayoutParams().width = itemSize;
    }

    public Uri getItem(int position) {
        return filePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.thumb_image_view);
            if (onItemClickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(v, getPosition());
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onItemClickListener.onLongClick(v, getPosition());
                        return true;
                    }
                });
            }
        }
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        if (selectedItems != null) {
            return selectedItems.size();
        } else {
            return 0;
        }
    }

    public SparseBooleanArrayParcelable getSelectedItems() {
        return selectedItems;
    }

    public interface OnItemClickListener {
        abstract void onClick(View view, int position);
        abstract void onLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private class NoDataException extends NullPointerException {
        private NoDataException(String detailMessage) {
            super(detailMessage);
        }
    }

}
