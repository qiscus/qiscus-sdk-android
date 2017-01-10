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

package com.qiscus.sdk.data.remote;

import com.schinizer.rxunfurl.RxUnfurl;
import com.schinizer.rxunfurl.model.PreviewData;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created on : December 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public enum QiscusUrlScraper {
    INSTANCE;

    private final RxUnfurl rxUnfurl;

    QiscusUrlScraper() {
        rxUnfurl = new RxUnfurl.Builder()
                .scheduler(Schedulers.io())
                .build();
    }

    public static QiscusUrlScraper getInstance() {
        return INSTANCE;
    }

    public Observable<PreviewData> generatePreviewData(String url) {
        return rxUnfurl.generatePreview(url);
    }
}
