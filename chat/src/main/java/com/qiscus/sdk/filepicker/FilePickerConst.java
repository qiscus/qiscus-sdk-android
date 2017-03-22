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

import android.support.annotation.RestrictTo;

/**
 * Created on : March 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class FilePickerConst {
    public static final int REQUEST_CODE_PHOTO = 233;
    public static final int REQUEST_CODE_DOC = 234;

    public static final int REQUEST_CODE_MEDIA_DETAIL = 235;

    public static final int DEFAULT_MAX_COUNT = 9;

    public static final int MEDIA_PICKER = 0x11;
    public static final int DOC_PICKER = 0x12;

    public static final String KEY_SELECTED_MEDIA = "SELECTED_PHOTOS";
    public static final String KEY_SELECTED_DOCS = "SELECTED_DOCS";

    public static final String EXTRA_PICKER_TYPE = "EXTRA_PICKER_TYPE";
    public static final String EXTRA_SHOW_GIF = "SHOW_GIF";
    public static final String EXTRA_FILE_TYPE = "EXTRA_FILE_TYPE";
    public static final String EXTRA_BUCKET_ID = "EXTRA_BUCKET_ID";
    public static final String ALL_PHOTOS_BUCKET_ID = "ALL_PHOTOS_BUCKET_ID";

    public static final int FILE_TYPE_MEDIA = 1;
    public static final int FILE_TYPE_DOCUMENT = 2;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 3;

    public static final String PDF = "PDF";
    public static final String PPT = "PPT";
    public static final String DOC = "DOC";
    public static final String XLS = "XLS";
    public static final String TXT = "TXT";
}
