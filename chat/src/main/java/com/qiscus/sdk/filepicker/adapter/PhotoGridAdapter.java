/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.filepicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.data.remote.QiscusGlide;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.model.Media;
import com.qiscus.sdk.filepicker.view.SmoothCheckBox;

import java.io.File;
import java.util.ArrayList;

public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder, Media> {
    private Context context;
    private int imageSize;

    public PhotoGridAdapter(Context context, ArrayList<Media> medias, ArrayList<String> selectedPaths) {
        super(medias, selectedPaths);
        this.context = context;
        setColumnNumber(context, 3);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_qiscus_photo_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {
        Media media = getItems().get(position);

        QiscusGlide.getInstance().get()
                .load(new File(media.getPath()))
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
                .override(imageSize, imageSize)
                .placeholder(R.drawable.qiscus_image_placeholder)
                .into(holder.imageView);

        holder.videoIcon.setVisibility(media.getMediaType() == FilePickerConst.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (PickerManager.getInstance().getMaxCount() == 1) {
                PickerManager.getInstance().add(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
            } else if (holder.checkBox.isChecked() || PickerManager.getInstance().shouldAdd()) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
            }
        });

        //in some cases, it will prevent unwanted situations
        holder.checkBox.setVisibility(View.GONE);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setOnClickListener(view -> {
            if (holder.checkBox.isChecked() || PickerManager.getInstance().shouldAdd()) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
            }
        });

        //if true, your checkbox will be selected, else unselected
        holder.checkBox.setChecked(isSelected(media));

        holder.selectBg.setVisibility(isSelected(media) ? View.VISIBLE : View.GONE);
        holder.checkBox.setVisibility(isSelected(media) ? View.VISIBLE : View.GONE);

        holder.checkBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            toggleSelection(media);
            holder.selectBg.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (isChecked) {
                holder.checkBox.setVisibility(View.VISIBLE);
                PickerManager.getInstance().add(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
            } else {
                holder.checkBox.setVisibility(View.GONE);
                PickerManager.getInstance().remove(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
            }
        });
    }

    private void setColumnNumber(Context context, int columnNum) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNum;
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private SmoothCheckBox checkBox;
        private ImageView imageView;
        private ImageView videoIcon;
        private View selectBg;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            checkBox = (SmoothCheckBox) itemView.findViewById(R.id.checkbox);
            imageView = (ImageView) itemView.findViewById(R.id.iv_photo);
            videoIcon = (ImageView) itemView.findViewById(R.id.video_icon);
            selectBg = itemView.findViewById(R.id.transparent_bg);
        }
    }
}
