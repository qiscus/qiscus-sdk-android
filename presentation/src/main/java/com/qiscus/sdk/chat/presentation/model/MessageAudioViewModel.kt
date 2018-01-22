package com.qiscus.sdk.chat.presentation.model

import android.media.MediaPlayer
import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString
import com.qiscus.sdk.chat.presentation.util.runOnUIThread
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageAudioViewModel(message: FileAttachmentMessage, mimeType: String) : MessageFileViewModel(message, mimeType) {

    private constructor(parcel: Parcel) : this(parcel.readParcelable(Message::class.java.classLoader), parcel.readString()) {
        selected = parcel.readBoolean()
        transfer = parcel.readBoolean()
    }

    var playingAudioListener: PlayingAudioListener? = null

    private var observer: MediaObserver? = null
    private var player: MediaPlayer? = null

    override fun determineReadableMessage(): String {
        return if ((message as FileAttachmentMessage).caption.isBlank()) {
            "\uD83D\uDD0A " + getString(resId = R.string.qiscus_send_a_audio)
        } else {
            "\uD83D\uDD0A " + message.caption
        }
    }

    private fun setupPlayer() {
        if (player == null) {
            val localPath = (message as FileAttachmentMessage).file
            if (localPath != null) {
                try {
                    player = MediaPlayer()
                    player!!.setDataSource(localPath.absolutePath)
                    player!!.prepare()
                    player!!.setOnCompletionListener { _ ->
                        observer!!.stop()
                        if (playingAudioListener != null) {
                            playingAudioListener!!.onStopAudio(this)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun playAudio() {
        if (observer == null) {
            observer = MediaObserver()
        }

        setupPlayer()

        if (!player!!.isPlaying) {
            player!!.start()
            observer!!.start()
            Thread(observer).start()
        } else {
            player!!.pause()
            observer!!.stop()
            if (playingAudioListener != null) {
                playingAudioListener!!.onPauseAudio(this)
            }
        }
    }

    fun isPlayingAudio(): Boolean {
        return player != null && player!!.isPlaying
    }

    fun getAudioDuration(): Int {
        if (player == null) {
            val localPath = (message as FileAttachmentMessage).file
            if (localPath == null) {
                return 0
            } else {
                setupPlayer()
            }
        }
        return player!!.duration
    }

    fun getCurrentAudioPosition(): Int {
        if (player == null) {
            val localPath = (message as FileAttachmentMessage).file
            if (localPath == null) {
                return 0
            } else {
                setupPlayer()
            }
        }
        return player!!.currentPosition
    }

    fun destroy() {
        if (playingAudioListener != null) {
            playingAudioListener = null
        }
        if (observer != null) {
            observer!!.stop()
            observer = null
        }
        if (player != null) {
            if (player!!.isPlaying) {
                player!!.stop()
            }
            player!!.release()
            player = null
        }
    }

    private inner class MediaObserver : Runnable {
        private val stopPlay = AtomicBoolean(false)

        fun stop() {
            stopPlay.set(true)
        }

        fun start() {
            stopPlay.set(false)
        }

        override fun run() {
            while (!stopPlay.get()) {
                runOnUIThread(Runnable {
                    if (playingAudioListener != null) {
                        playingAudioListener!!.onPlayingAudio(this@MessageAudioViewModel, player!!.currentPosition)
                    }
                })
                try {
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeString(mimeType)
        parcel.writeBoolean(selected)
        parcel.writeBoolean(transfer)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageAudioViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageAudioViewModel {
            return MessageAudioViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageAudioViewModel?> {
            return arrayOfNulls(size)
        }
    }
}