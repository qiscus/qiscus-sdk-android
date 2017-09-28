package com.qiscus.sdk.chat.core.component

import com.qiscus.sdk.chat.core.UiThread
import com.qiscus.sdk.chat.data.executor.JobExecutor
import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor

/**
 * Created on : August 30, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class ExecutorComponent
(
        var threadExecutor: ThreadExecutor = JobExecutor(),
        var postExecutionThread: PostExecutionThread = UiThread()
)