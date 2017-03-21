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
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.model.Document;
import com.qiscus.sdk.filepicker.view.SmoothCheckBox;

import java.util.List;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class FileListAdapter extends SelectableAdapter<FileListAdapter.FileViewHolder, Document> {
    private Context context;

    public FileListAdapter(Context context, List<Document> items, List<String> selectedPaths) {
        super(items, selectedPaths);
        this.context = context;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_qiscus_doc_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final FileViewHolder holder, int position) {
        Document document = getItems().get(position);

        holder.imageView.setImageResource(document.getFileType().getDrawable());
        holder.fileNameTextView.setText(document.getTitle());
        holder.fileSizeTextView.setText(Formatter.formatShortFileSize(context, Long.parseLong(document.getSize())));

        holder.itemView.setOnClickListener(v -> {
            if (PickerManager.getInstance(context).getMaxCount() == 1) {
                PickerManager.getInstance(context).add(document.getPath(), FilePickerConst.FILE_TYPE_DOCUMENT);
            } else {
                onItemClicked(document, holder);
            }
        });

        //in some cases, it will prevent unwanted situations
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setOnClickListener(view -> onItemClicked(document, holder));

        //if true, your checkbox will be selected, else unselected
        holder.checkBox.setChecked(isSelected(document));

        holder.itemView.setBackgroundResource(isSelected(document) ? R.color.qiscus_bg_gray : android.R.color.white);
        holder.checkBox.setVisibility(isSelected(document) ? View.VISIBLE : View.GONE);

        holder.checkBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            toggleSelection(document);
            holder.itemView.setBackgroundResource(isChecked ? R.color.qiscus_bg_gray : android.R.color.white);

        });
    }

    private void onItemClicked(Document document, FileViewHolder holder) {
        if (holder.checkBox.isChecked() || PickerManager.getInstance(context).shouldAdd()) {
            holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
        }

        if (holder.checkBox.isChecked()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            PickerManager.getInstance(context).add(document.getPath(), FilePickerConst.FILE_TYPE_DOCUMENT);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            PickerManager.getInstance(context).remove(document.getPath(), FilePickerConst.FILE_TYPE_DOCUMENT);
        }
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        private SmoothCheckBox checkBox;
        private ImageView imageView;
        private TextView fileNameTextView;
        private TextView fileSizeTextView;

        public FileViewHolder(View itemView) {
            super(itemView);
            checkBox = (SmoothCheckBox) itemView.findViewById(R.id.checkbox);
            imageView = (ImageView) itemView.findViewById(R.id.file_iv);
            fileNameTextView = (TextView) itemView.findViewById(R.id.file_name_tv);
            fileSizeTextView = (TextView) itemView.findViewById(R.id.file_size_tv);
        }
    }
}
