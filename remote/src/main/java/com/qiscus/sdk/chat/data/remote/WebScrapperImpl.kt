package com.qiscus.sdk.chat.data.remote

import com.schinizer.rxunfurl.RxUnfurl
import com.schinizer.rxunfurl.model.PreviewData
import io.reactivex.Single

/**
 * Created on : October 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class WebScrapperImpl(private val rxUnfurl: RxUnfurl) : WebScrapper {
    override fun generatePreviewData(url: String): Single<PreviewData> {
        return rxUnfurl.generatePreview(url)
    }
}