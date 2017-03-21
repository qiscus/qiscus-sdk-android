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

    public final static int DEFAULT_MAX_COUNT = 9;

    public final static int MEDIA_PICKER = 0x11;
    public final static int DOC_PICKER = 0x12;

    public final static String KEY_SELECTED_MEDIA = "SELECTED_PHOTOS";
    public final static String KEY_SELECTED_DOCS = "SELECTED_DOCS";

    public final static String EXTRA_PICKER_TYPE = "EXTRA_PICKER_TYPE";
    public final static String EXTRA_SHOW_GIF = "SHOW_GIF";
    public final static String EXTRA_FILE_TYPE = "EXTRA_FILE_TYPE";
    public final static String EXTRA_BUCKET_ID = "EXTRA_BUCKET_ID";
    public final static String ALL_PHOTOS_BUCKET_ID = "ALL_PHOTOS_BUCKET_ID";

    public final static int FILE_TYPE_MEDIA = 1;
    public final static int FILE_TYPE_DOCUMENT = 2;

    public final static int MEDIA_TYPE_IMAGE = 1;
    public final static int MEDIA_TYPE_VIDEO = 3;

    public final static String PDF = "PDF";
    public final static String PPT = "PPT";
    public final static String DOC = "DOC";
    public final static String XLS = "XLS";
    public final static String TXT = "TXT";
}
