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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.qiscus.sdk.R;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.adapter.SectionsPagerAdapter;
import com.qiscus.sdk.filepicker.model.Document;
import com.qiscus.sdk.filepicker.model.FileType;
import com.qiscus.sdk.filepicker.util.MediaStoreHelper;
import com.qiscus.sdk.filepicker.util.TabLayoutHelper;
import com.qiscus.sdk.util.QiscusFileUtil;

import java.io.IOException;
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
    protected static final int PICK_FILE_REQUEST = 363;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setHasOptionsMenu(true);
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            inflater.inflate(R.menu.file_picker, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && i == R.id.action_choose_manually) {
            addFile();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void addFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"text/plain", "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint"};
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_FILE_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getActivity(), "Can not open file", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                PickerManager.getInstance()
                        .add(QiscusFileUtil.from(data.getData()).getAbsolutePath(), FilePickerConst.FILE_TYPE_DOCUMENT);
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Can not read file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
