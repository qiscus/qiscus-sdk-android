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

import androidx.core.util.Pair;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rx.Subscription;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on : March 23, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusPhotoViewerPresenter extends QiscusPresenter<QiscusPhotoViewerPresenter.View> {

    private Subscription downloadSubscription;

    public QiscusPhotoViewerPresenter(View view) {
        super(view);
    }

    public void loadQiscusPhotos(long roomId) {
        view.showLoading();
        Qiscus.getDataStore()
                .getObservableComments(roomId)
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
                        view.showError(QiscusTextUtil.getString(R.string.qiscus_general_error));
                        view.closePage();
                        view.dismissLoading();
                    }
                });
    }

    public void downloadFile(QiscusComment qiscusComment) {
        if (qiscusComment.isDownloading()) {
            return;
        }
        qiscusComment.setDownloading(true);
        downloadSubscription = (Subscription) QiscusApi.getInstance()
                .downloadFile(qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(),
                        percentage -> qiscusComment.setProgress((int) percentage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.compose(bindToLifecycle())
                .doOnNext(file1 -> {
                    QiscusFileUtil.notifySystem(file1);
                    qiscusComment.setDownloading(false);
                    Qiscus.getDataStore().addOrUpdateLocalPath(qiscusComment.getRoomId(), qiscusComment.getId(),
                            file1.getAbsolutePath());
                })
                .subscribe(file1 -> view.onFileDownloaded(Pair.create(qiscusComment, file1)), throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    qiscusComment.setDownloading(false);
                    view.showError(QiscusTextUtil.getString(R.string.qiscus_failed_download_file));
                });
    }

    public void cancelDownloading() {
        if (downloadSubscription != null) {
            downloadSubscription.unsubscribe();
        }
    }

    public interface View extends QiscusPresenter.View {
        void onLoadQiscusPhotos(List<Pair<QiscusComment, File>> qiscusPhotos);

        void onFileDownloaded(Pair<QiscusComment, File> qiscusPhoto);

        void closePage();
    }
}
