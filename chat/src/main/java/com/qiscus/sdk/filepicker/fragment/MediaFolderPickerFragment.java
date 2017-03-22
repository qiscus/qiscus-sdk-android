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

package com.qiscus.sdk.filepicker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.MediaDetailsActivity;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.adapter.FolderGridAdapter;
import com.qiscus.sdk.filepicker.model.PhotoDirectory;
import com.qiscus.sdk.filepicker.util.GridSpacingItemDecoration;
import com.qiscus.sdk.filepicker.util.MediaStoreHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class MediaFolderPickerFragment extends Fragment implements FolderGridAdapter.FolderGridAdapterListener {
    private static final String FILE_TYPE = "FILE_TYPE";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FolderGridAdapter photoGridAdapter;
    private int fileType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qiscus_media_folder_picker, container, false);
    }

    public static MediaFolderPickerFragment newInstance(int fileType) {
        MediaFolderPickerFragment mediaFolderPickerFragment = new MediaFolderPickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(FILE_TYPE, fileType);
        mediaFolderPickerFragment.setArguments(bundle);
        return mediaFolderPickerFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        emptyView = (TextView) view.findViewById(R.id.empty_view);
        fileType = getArguments().getInt(FILE_TYPE);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

        int spanCount = 2; // 2 columns
        int spacing = 5; // 5px
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getDataFromMedia();
    }

    private void getDataFromMedia() {
        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(FilePickerConst.EXTRA_SHOW_GIF, PickerManager.getInstance().isShowGif());
        mediaStoreArgs.putInt(FilePickerConst.EXTRA_FILE_TYPE, fileType);

        if (fileType == FilePickerConst.MEDIA_TYPE_IMAGE) {
            MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs, this::updateList);
        } else if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
            MediaStoreHelper.getVideoDirs(getActivity(), mediaStoreArgs, this::updateList);
        }
    }

    private void updateList(List<PhotoDirectory> dirs) {
        if (dirs.size() > 0) {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        PhotoDirectory photoDirectory = new PhotoDirectory();
        photoDirectory.setBucketId(FilePickerConst.ALL_PHOTOS_BUCKET_ID);

        if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
            photoDirectory.setName(getString(R.string.qiscus_all_videos));
        } else if (fileType == FilePickerConst.MEDIA_TYPE_IMAGE) {
            photoDirectory.setName(getString(R.string.qiscus_all_photos));
        } else {
            photoDirectory.setName(getString(R.string.qiscus_all_files));
        }

        if (dirs.size() > 0 && dirs.get(0).getMedias().size() > 0) {
            photoDirectory.setDateAdded(dirs.get(0).getDateAdded());
            photoDirectory.setCoverPath(dirs.get(0).getMedias().get(0).getPath());
        }

        for (int i = 0; i < dirs.size(); i++) {
            photoDirectory.addPhotos(dirs.get(i).getMedias());
        }

        dirs.add(0, photoDirectory);

        if (photoGridAdapter != null) {
            photoGridAdapter.setData(dirs);
            photoGridAdapter.notifyDataSetChanged();
        } else {
            photoGridAdapter = new FolderGridAdapter(getActivity(), (ArrayList<PhotoDirectory>) dirs, null);
            recyclerView.setAdapter(photoGridAdapter);
            photoGridAdapter.setFolderGridAdapterListener(this);
        }
    }

    @Override
    public void onFolderClicked(PhotoDirectory photoDirectory) {
        Intent intent = new Intent(getActivity(), MediaDetailsActivity.class);
        intent.putExtra(PhotoDirectory.class.getSimpleName(), photoDirectory);
        intent.putExtra(FilePickerConst.EXTRA_FILE_TYPE, fileType);
        getActivity().startActivityForResult(intent, FilePickerConst.REQUEST_CODE_MEDIA_DETAIL);
    }
}
