package com.qiscus.sdk.chat.domain.model

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class FileAttachmentProgress(val fileAttachmentComment: FileAttachmentComment, val state: State, var progress: Int) {

    enum class State {
        UPLOADING,
        DOWNLOADING
    }
}