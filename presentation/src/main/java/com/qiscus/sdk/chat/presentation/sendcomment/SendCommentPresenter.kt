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

package com.qiscus.sdk.chat.presentation.sendcomment

import com.qiscus.sdk.chat.domain.common.CommentFactory
import com.qiscus.sdk.chat.domain.interactor.comment.PostComment
import java.io.File

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SendCommentPresenter(val view: SendCommentContract.View,
                           private val useCase: PostComment,
                           private val commentFactory: CommentFactory) : SendCommentContract.Presenter {

    override fun start() {}

    override fun stop() {
        useCase.dispose()
    }

    override fun sendComment(roomId: String, message: String) {
        useCase.execute(PostComment.Params(commentFactory.createTextComment(roomId, message)))
        view.clearTextField()
    }

    override fun sendFileComment(roomId: String, file: File, caption: String) {
        useCase.execute(PostComment.Params(commentFactory.createFileAttachmentComment(roomId, file, caption)))
    }
}