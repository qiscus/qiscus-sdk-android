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

package com.qiscus.sdk.filepicker.cursor;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.CursorLoader;

import com.qiscus.sdk.filepicker.FilePickerConst;

public class PhotoDirectoryLoader extends CursorLoader {

    private final String[] IMAGE_PROJECTION = {
            Media._ID,
            Media.DATA,
            Media.BUCKET_ID,
            Media.BUCKET_DISPLAY_NAME,
            Media.DATE_ADDED,
            Media.TITLE
    };

    public PhotoDirectoryLoader(Context context, Bundle args) {
        super(context);
        String bucketId = args.getString(FilePickerConst.EXTRA_BUCKET_ID, null);
        int mediaType = args.getInt(FilePickerConst.EXTRA_FILE_TYPE, FilePickerConst.MEDIA_TYPE_IMAGE);

        setProjection(null);
        setUri(MediaStore.Files.getContentUri("external"));
        setSortOrder(Media._ID + " DESC");

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        if (mediaType == FilePickerConst.MEDIA_TYPE_VIDEO) {
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        }

        if (bucketId != null) {
            selection += " AND " + Media.BUCKET_ID + "='" + bucketId + "'";
        }

        setSelection(selection);
    }
}