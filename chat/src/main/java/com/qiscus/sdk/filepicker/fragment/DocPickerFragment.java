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
import android.widget.ProgressBar;

import com.android.internal.util.Predicate;
import com.qiscus.sdk.R;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.adapter.SectionsPagerAdapter;
import com.qiscus.sdk.filepicker.model.Document;
import com.qiscus.sdk.filepicker.model.FileType;
import com.qiscus.sdk.filepicker.util.MediaStoreHelper;
import com.qiscus.sdk.filepicker.util.TabLayoutHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class DocPickerFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qiscus_doc_picker, container, false);
    }

    public static DocPickerFragment newInstance() {
        return new DocPickerFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpViewPager();
        setData();
    }

    private void setData() {
        MediaStoreHelper.getDocs(getActivity(), files -> {
            progressBar.setVisibility(View.GONE);
            setDataOnFragments(files);
        });
    }

    private void setDataOnFragments(List<Document> files) {
        SectionsPagerAdapter sectionsPagerAdapter = (SectionsPagerAdapter) viewPager.getAdapter();
        if (sectionsPagerAdapter != null) {
            for (int index = 0; index < sectionsPagerAdapter.getCount(); index++) {
                DocFragment docFragment = (DocFragment) getChildFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + index);
                if (docFragment != null) {
                    FileType fileType = docFragment.getFileType();
                    if (fileType != null) {
                        docFragment.updateList(filterDocuments(fileType.getExtensions(), files));
                    }
                }
            }
        }
    }

    private void setUpViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getChildFragmentManager());
        ArrayList<FileType> supportedTypes = PickerManager.getInstance().getFileTypes();
        for (int index = 0; index < supportedTypes.size(); index++) {
            adapter.addFragment(DocFragment.newInstance(supportedTypes.get(index)), supportedTypes.get(index).getTitle());
        }

        viewPager.setOffscreenPageLimit(supportedTypes.size());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        TabLayoutHelper tabLayoutHelper = new TabLayoutHelper(tabLayout, viewPager);
        tabLayoutHelper.setAutoAdjustTabModeEnabled(true);
    }

    private ArrayList<Document> filterDocuments(final String[] type, List<Document> documents) {
        Predicate<Document> docType = document -> document.isThisType(type);
        return new ArrayList<>(filter(new HashSet<>(documents), docType));
    }

    private <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<>();
        for (T element : target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }
}
