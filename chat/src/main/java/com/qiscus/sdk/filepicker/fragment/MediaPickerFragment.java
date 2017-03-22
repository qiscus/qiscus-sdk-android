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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.sdk.R;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.adapter.SectionsPagerAdapter;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class MediaPickerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qiscus_media_picker, container, false);
    }

    public static MediaPickerFragment newInstance() {
        return new MediaPickerFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getChildFragmentManager());

        if (PickerManager.getInstance().isShowFolderView()) {
            adapter.addFragment(MediaFolderPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_IMAGE),
                    getString(R.string.qiscus_images));
        } else {
            adapter.addFragment(MediaDetailPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_IMAGE),
                    getString(R.string.qiscus_images));
        }

        if (PickerManager.getInstance().showVideo()) {
            if (PickerManager.getInstance().isShowFolderView()) {
                adapter.addFragment(MediaFolderPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_VIDEO),
                        getString(R.string.qiscus_videos));
            } else {
                adapter.addFragment(MediaDetailPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_VIDEO),
                        getString(R.string.qiscus_videos));
            }
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
