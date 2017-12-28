package com.qiscus.sdk.chat.presentation.model

/**
 * Created on : December 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface PlayingAudioListener {
    fun onPlayingAudio(messageAudioViewModel: MessageAudioViewModel, currentPosition: Int)

    fun onPauseAudio(messageAudioViewModel: MessageAudioViewModel)

    fun onStopAudio(messageAudioViewModel: MessageAudioViewModel)
}