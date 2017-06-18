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
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.data.remote.QiscusGlide;
import com.qiscus.sdk.filepicker.model.PhotoDirectory;

import java.io.File;
import java.util.ArrayList;

public class FolderGridAdapter extends SelectableAdapter<FolderGridAdapter.PhotoViewHolder, PhotoDirectory> {
    private Context context;
    private int imageSize;
    private FolderGridAdapterListener folderGridAdapterListener;

    public interface FolderGridAdapterListener {
        void onFolderClicked(PhotoDirectory photoDirectory);
    }

    public FolderGridAdapter(Context context, ArrayList<PhotoDirectory> photos, ArrayList<String> selectedPaths) {
        super(photos, selectedPaths);
        this.context = context;
        setColumnNumber(context, 3);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_qiscus_folder_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {
        PhotoDirectory photoDirectory = getItems().get(position);

        QiscusGlide.getInstance().get()
                .load(new File(photoDirectory.getCoverPath()))
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
                .override(imageSize, imageSize)
                .placeholder(R.drawable.qiscus_image_placeholder)
                .into(holder.imageView);

        holder.folderTitle.setText(photoDirectory.getName());
        holder.folderCount.setText(String.valueOf(photoDirectory.getMedias().size()));

        holder.itemView.setOnClickListener(view -> {
            if (folderGridAdapterListener != null) {
                folderGridAdapterListener.onFolderClicked(photoDirectory);
            }
        });
        holder.bottomOverlay.setVisibility(View.VISIBLE);
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

    public void setFolderGridAdapterListener(FolderGridAdapterListener onClickListener) {
        this.folderGridAdapterListener = onClickListener;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView folderTitle;
        private TextView folderCount;
        private View bottomOverlay;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv_photo);
            folderTitle = (TextView) itemView.findViewById(R.id.folder_title);
            folderCount = (TextView) itemView.findViewById(R.id.folder_count);
            bottomOverlay = itemView.findViewById(R.id.bottomOverlay);
        }
    }
}
