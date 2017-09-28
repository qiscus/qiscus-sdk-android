package com.qiscus.sdk.chat.domain.pubsub

import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface CommentObserver {
    fun listenNewComment(): Observable<Comment>

    fun listenCommentState(roomId: String): Observable<Comment>
}
