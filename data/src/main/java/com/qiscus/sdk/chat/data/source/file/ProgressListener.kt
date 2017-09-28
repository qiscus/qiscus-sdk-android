package com.qiscus.sdk.chat.data.source.file

interface ProgressListener {
    fun onProgress(total: Int)
}