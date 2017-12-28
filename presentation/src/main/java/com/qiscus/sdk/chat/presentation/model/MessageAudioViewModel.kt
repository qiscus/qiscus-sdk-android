package com.qiscus.sdk.chat.presentation.model

import android.media.MediaPlayer
import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.repository.UserRepository
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
open class MessageAudioViewModel
@JvmOverloads constructor(message: FileAttachmentMessage,
                          mimeType: String,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt mentionAllColor: Int,
                          @ColorInt mentionOtherColor: Int,
                          @ColorInt mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)
    : MessageFileViewModel(message, mimeType, account, userRepository, mentionAllColor, mentionOtherColor,
        mentionMeColor, mentionClickListener) {

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
}