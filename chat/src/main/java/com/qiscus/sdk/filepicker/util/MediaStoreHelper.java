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

package com.qiscus.sdk.filepicker.util;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.filepicker.cursor.DocScannerTask;
import com.qiscus.sdk.filepicker.cursor.loadercallbacks.FileResultCallback;
import com.qiscus.sdk.filepicker.cursor.loadercallbacks.PhotoDirLoaderCallbacks;
import com.qiscus.sdk.filepicker.model.Document;
import com.qiscus.sdk.filepicker.model.PhotoDirectory;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class MediaStoreHelper {

    public static void getPhotoDirs(FragmentActivity activity, Bundle args, FileResultCallback<PhotoDirectory> resultCallback) {
        if (activity.getSupportLoaderManager().getLoader(FilePickerConst.MEDIA_TYPE_IMAGE) != null) {
            activity.getSupportLoaderManager().restartLoader(FilePickerConst.MEDIA_TYPE_IMAGE, args,
                    new PhotoDirLoaderCallbacks(activity, resultCallback));
        } else {
            activity.getSupportLoaderManager().initLoader(FilePickerConst.MEDIA_TYPE_IMAGE, args,
                    new PhotoDirLoaderCallbacks(activity, resultCallback));
        }
    }

    public static void getVideoDirs(FragmentActivity activity, Bundle args, FileResultCallback<PhotoDirectory> resultCallback) {
        if (activity.getSupportLoaderManager().getLoader(FilePickerConst.MEDIA_TYPE_VIDEO) != null) {
            activity.getSupportLoaderManager().restartLoader(FilePickerConst.MEDIA_TYPE_VIDEO, args,
                    new PhotoDirLoaderCallbacks(activity, resultCallback));
        } else {
            activity.getSupportLoaderManager().initLoader(FilePickerConst.MEDIA_TYPE_VIDEO, args,
                    new PhotoDirLoaderCallbacks(activity, resultCallback));
        }
    }

    public static void getDocs(FragmentActivity activity, FileResultCallback<Document> fileResultCallback) {
        new DocScannerTask(activity, fileResultCallback).execute();
    }
}