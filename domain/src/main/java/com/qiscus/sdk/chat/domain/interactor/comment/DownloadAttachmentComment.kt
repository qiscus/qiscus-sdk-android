package com.qiscus.sdk.chat.domain.interactor.comment

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import io.reactivex.Single

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DownloadAttachmentComment(private val commentRepository: CommentRepository,
                                threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        SingleUseCase<FileAttachmentComment, DownloadAttachmentComment.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Single<FileAttachmentComment> {
        return commentRepository.downloadAttachmentComment(params!!.attachmentComment)
    }

    data class Params(val attachmentComment: FileAttachmentComment)
}