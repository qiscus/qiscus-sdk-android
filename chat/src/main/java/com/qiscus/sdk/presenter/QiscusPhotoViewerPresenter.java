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

package com.qiscus.sdk.presenter;

import android.support.v4.util.Pair;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : March 23, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusPhotoViewerPresenter extends QiscusPresenter<QiscusPhotoViewerPresenter.View> {

    public QiscusPhotoViewerPresenter(View view) {
        super(view);
    }

    public void loadQiscusPhotos(int topicId) {
        view.showLoading();
        Qiscus.getDataStore()
                .getObservableComments(topicId)
                .map(qiscusComments -> {
                    List<Pair<QiscusComment, File>> qiscusPhotos = new ArrayList<>();
                    for (QiscusComment qiscusComment : qiscusComments) {
                        if (qiscusComment.isImage()) {
                            File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
                            if (localPath != null) {
                                qiscusPhotos.add(Pair.create(qiscusComment, localPath));
                            }
                        }
                    }
                    Collections.reverse(qiscusPhotos);
                    return qiscusPhotos;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusPhotos -> {
                    if (view != null) {
                        view.onLoadQiscusPhotos(qiscusPhotos);
                        view.dismissLoading();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    if (view != null) {
                        view.showError("Something went wrong!");
                        view.dismissLoading();
                    }
                });
    }

    public interface View extends QiscusPresenter.View {
        void onLoadQiscusPhotos(List<Pair<QiscusComment, File>> qiscusPhotos);
    }
}
