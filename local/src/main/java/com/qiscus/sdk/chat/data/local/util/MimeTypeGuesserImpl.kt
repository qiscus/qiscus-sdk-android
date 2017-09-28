package com.qiscus.sdk.chat.data.local.util

import android.webkit.MimeTypeMap
import com.qiscus.sdk.chat.data.util.MimeTypeGuesser
import com.qiscus.sdk.chat.domain.common.getAttachmentName
import com.qiscus.sdk.chat.domain.common.getExtension
import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MimeTypeGuesserImpl: MimeTypeGuesser {
    override fun getMimeTypeFromFile(file: File): String? {
        return getMimeTypeFromFileName(file.name)
    }

    override fun getMimeTypeFromFileUrl(fileUrl: String): String? {
        return getMimeTypeFromFileName(fileUrl.getAttachmentName())
    }

    override fun getMimeTypeFromFileName(fileName: String): String? {
        return getMimeTypeFromExtension(fileName.getExtension())
    }

    override fun getMimeTypeFromExtension(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}