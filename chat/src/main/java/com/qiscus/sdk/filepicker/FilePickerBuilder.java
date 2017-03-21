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

package com.qiscus.sdk.filepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;

import com.qiscus.sdk.filepicker.model.FileType;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class FilePickerBuilder {
    private final Bundle pickerOptionsBundle;
    private final Context context;

    public FilePickerBuilder(Context context) {
        pickerOptionsBundle = new Bundle();
        this.context = context;
    }

    public static FilePickerBuilder getInstance(Context context) {
        return new FilePickerBuilder(context);
    }

    public FilePickerBuilder setMaxCount(int maxCount) {
        PickerManager.getInstance(context).setMaxCount(maxCount);
        return this;
    }

    public FilePickerBuilder addVideoPicker() {
        PickerManager.getInstance(context).setShowVideos(true);
        return this;
    }

    public FilePickerBuilder showGifs(boolean status) {
        PickerManager.getInstance(context).setShowGif(status);
        return this;
    }

    public FilePickerBuilder showFolderView(boolean status) {
        PickerManager.getInstance(context).setShowFolderView(status);
        return this;
    }

    public FilePickerBuilder enableDocSupport(boolean status) {
        PickerManager.getInstance(context).setDocSupport(status);
        return this;
    }

    public FilePickerBuilder enableOrientation(boolean status) {
        PickerManager.getInstance(context).setEnableOrientation(status);
        return this;
    }

    public FilePickerBuilder addFileSupport(String title, String[] extensions, @DrawableRes int drawable) {
        PickerManager.getInstance(context).addFileType(new FileType(title, extensions, drawable));
        return this;
    }

    public FilePickerBuilder addFileSupport(String title, String[] extensions) {
        PickerManager.getInstance(context).addFileType(new FileType(title, extensions, 0));
        return this;
    }

    public void pickPhoto(Activity context) {
        pickerOptionsBundle.putInt(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.MEDIA_PICKER);
        start(context, FilePickerConst.MEDIA_PICKER);
    }

    public void pickPhoto(Fragment context) {
        pickerOptionsBundle.putInt(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.MEDIA_PICKER);
        start(context, FilePickerConst.MEDIA_PICKER);
    }

    public void pickFile(Activity context) {
        pickerOptionsBundle.putInt(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.DOC_PICKER);
        start(context, FilePickerConst.DOC_PICKER);
    }

    public void pickFile(Fragment context) {
        pickerOptionsBundle.putInt(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.DOC_PICKER);
        start(context, FilePickerConst.DOC_PICKER);
    }

    private void start(Activity context, int pickerType) {
        Intent intent = new Intent(context, FilePickerActivity.class);
        intent.putExtras(pickerOptionsBundle);

        context.startActivityForResult(intent, pickerType == FilePickerConst.MEDIA_PICKER ?
                FilePickerConst.REQUEST_CODE_PHOTO : FilePickerConst.REQUEST_CODE_DOC);
    }

    private void start(Fragment fragment, int pickerType) {
        Intent intent = new Intent(fragment.getActivity(), FilePickerActivity.class);
        intent.putExtras(pickerOptionsBundle);
        fragment.startActivityForResult(intent, pickerType == FilePickerConst.MEDIA_PICKER ?
                FilePickerConst.REQUEST_CODE_PHOTO : FilePickerConst.REQUEST_CODE_DOC);
    }

}
