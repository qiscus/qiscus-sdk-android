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

package com.qiscus.sdk.filepicker.cursor.loadercallbacks;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.cursor.PhotoDirectoryLoader;
import com.qiscus.sdk.filepicker.model.PhotoDirectory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private WeakReference<Context> context;
    private FileResultCallback<PhotoDirectory> resultCallback;

    public PhotoDirLoaderCallbacks(Context context, FileResultCallback<PhotoDirectory> resultCallback) {
        this.context = new WeakReference<>(context);
        this.resultCallback = resultCallback;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new PhotoDirectoryLoader(context.get(), args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }

        List<PhotoDirectory> directories = new ArrayList<>();
        while (data.moveToNext()) {
            int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
            String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
            String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
            String path = data.getString(data.getColumnIndexOrThrow(DATA));
            String fileName = data.getString(data.getColumnIndexOrThrow(TITLE));
            int mediaType = data.getInt(data.getColumnIndexOrThrow(MEDIA_TYPE));

            PhotoDirectory photoDirectory = new PhotoDirectory();
            photoDirectory.setBucketId(bucketId);
            photoDirectory.setName(name);

            if (!directories.contains(photoDirectory)) {
                photoDirectory.setCoverPath(path);
                if (PickerManager.getInstance().isShowGif() && path.toLowerCase().endsWith("gif")) {
                    photoDirectory.addPhoto(imageId, fileName, path, mediaType);
                } else {
                    photoDirectory.addPhoto(imageId, fileName, path, mediaType);
                }

                photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                directories.add(photoDirectory);
            } else {
                directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, fileName, path, mediaType);
            }

        }

        if (resultCallback != null) {
            resultCallback.onResultCallback(directories);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}