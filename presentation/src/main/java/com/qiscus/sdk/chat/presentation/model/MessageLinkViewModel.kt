package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.extractUrls
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.schinizer.rxunfurl.model.PreviewData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created on : January 02, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageLinkViewModel(message: Message) : MessageTextViewModel(message) {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    var linkPreviewListener: LinkPreviewListener? = null
    private val webScrapper = Qiscus.instance.component.dataComponent.webScrapper

    var previewData: PreviewData? = null
        private set

    val urls by lazy {
        message.text.extractUrls()
    }

    fun loadLinkPreview() {
        if (previewData != null) {
            if (linkPreviewListener != null) {
                linkPreviewListener!!.onLinkPreviewReady(this)
            }
        } else if (urls.isNotEmpty()) {
            webScrapper.generatePreviewData(urls[0])
                    .doAfterSuccess { previewData -> previewData.url = urls[0] }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        previewData = it
                        if (linkPreviewListener != null) {
                            linkPreviewListener!!.onLinkPreviewReady(this)
                        }
                    }, {})
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeBoolean(selected)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageLinkViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageLinkViewModel {
            return MessageLinkViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageLinkViewModel?> {
            return arrayOfNulls(size)
        }
    }
}