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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.adapter.PhotoGridAdapter;
import com.qiscus.sdk.filepicker.model.Media;
import com.qiscus.sdk.filepicker.model.PhotoDirectory;
import com.qiscus.sdk.filepicker.util.MediaStoreHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class MediaDetailPickerFragment extends Fragment {
    private static final String FILE_TYPE = "FILE_TYPE";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private PhotoGridAdapter photoGridAdapter;
    private int fileType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qiscus_photo_picker, container, false);
    }

    public static MediaDetailPickerFragment newInstance(int fileType) {
        MediaDetailPickerFragment mediaDetailPickerFragment = new MediaDetailPickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(FILE_TYPE, fileType);
        mediaDetailPickerFragment.setArguments(bundle);
        return mediaDetailPickerFragment;
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
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getDataFromMedia();
    }

    private void getDataFromMedia() {
        Bundle mediaStoreArgs = new Bundle();

        mediaStoreArgs.putInt(FilePickerConst.EXTRA_FILE_TYPE, fileType);

        if (fileType == FilePickerConst.MEDIA_TYPE_IMAGE) {
            MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs, this::updateList);
        } else if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
            MediaStoreHelper.getVideoDirs(getActivity(), mediaStoreArgs, this::updateList);
        }
    }

    private void updateList(List<PhotoDirectory> dirs) {
        ArrayList<Media> medias = new ArrayList<>();
        for (int i = 0; i < dirs.size(); i++) {
            medias.addAll(dirs.get(i).getMedias());
        }

        Collections.sort(medias, (a, b) -> b.getId() - a.getId());

        emptyView.setVisibility(medias.size() > 0 ? View.GONE : View.VISIBLE);

        if (photoGridAdapter != null) {
            photoGridAdapter.setData(medias);
            photoGridAdapter.notifyDataSetChanged();
        } else {
            photoGridAdapter = new PhotoGridAdapter(getActivity(), medias,
                    PickerManager.getInstance().getSelectedPhotos());
            recyclerView.setAdapter(photoGridAdapter);
        }

    }
}
