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
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.qiscus.sdk.filepicker.PickerManager;
import com.qiscus.sdk.filepicker.cursor.loadercallbacks.FileResultCallback;
import com.qiscus.sdk.filepicker.model.Document;
import com.qiscus.sdk.filepicker.model.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.DATA;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class DocScannerTask extends AsyncTask<Void, Void, List<Document>> {
    private static final String[] DOC_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Files.FileColumns.TITLE
    };

    private final FileResultCallback<Document> resultCallback;
    private final Context context;

    public DocScannerTask(Context context, FileResultCallback<Document> fileResultCallback) {
        this.context = context;
        this.resultCallback = fileResultCallback;
    }

    @Override
    protected List<Document> doInBackground(Void... voids) {
        ArrayList<Document> documents = new ArrayList<>();
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                DOC_PROJECTION, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        if (cursor != null) {
            documents = getDocumentFromCursor(cursor);
            cursor.close();
        }

        return documents;
    }

    @Override
    protected void onPostExecute(List<Document> documents) {
        super.onPostExecute(documents);
        if (resultCallback != null) {
            resultCallback.onResultCallback(documents);
        }
    }

    private ArrayList<Document> getDocumentFromCursor(Cursor data) {
        ArrayList<Document> documents = new ArrayList<>();
        while (data.moveToNext()) {
            int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
            String path = data.getString(data.getColumnIndexOrThrow(DATA));
            String title = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE));

            if (path != null) {
                FileType fileType = getFileType(PickerManager.getInstance().getFileTypes(), path);
                if (fileType != null && !(new File(path).isDirectory())) {
                    Document document = new Document(imageId, title, path);
                    document.setFileType(fileType);

                    String mimeType = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
                    if (mimeType != null && !TextUtils.isEmpty(mimeType)) {
                        document.setMimeType(mimeType);
                    } else {
                        document.setMimeType("");
                    }

                    document.setSize(data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)));
                    if (!documents.contains(document)) {
                        documents.add(document);
                    }
                }
            }
        }

        return documents;
    }

    private FileType getFileType(ArrayList<FileType> types, String path) {
        for (int index = 0; index < types.size(); index++) {
            for (String string : types.get(index).getExtensions()) {
                if (path.endsWith(string)) {
                    return types.get(index);
                }
            }
        }
        return null;
    }
}
