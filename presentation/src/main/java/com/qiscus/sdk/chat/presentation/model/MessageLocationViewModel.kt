package com.qiscus.sdk.chat.presentation.model

import android.text.Spannable
import android.text.SpannableString
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageLocationViewModel
@JvmOverloads constructor(message: Message,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)
    : MessageViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val locationName by lazy {
        message.type.payload.optString("name")
    }

    val locationAddress by lazy {
        message.type.payload.optString("address")
    }

    val locationLatitude by lazy {
        message.type.payload.optDouble("latitude")
    }

    val locationLongitude by lazy {
        message.type.payload.optString("longitude")
    }

    val mapUrl by lazy {
        "http://maps.google.com/?q=$locationLatitude,$locationLongitude"
    }

    val thumbnailUrl by lazy {
        "http://maps.google.com/maps/api/staticmap?center=$locationLatitude,$locationLongitude" +
                "&zoom=17&size=512x300&sensor=false"
    }

    override fun determineReadableMessage(): String {
        return "\uD83D\uDCCD $locationName\n$locationAddress"
    }

    override fun determineSpannableMessage(): Spannable {
        return SpannableString(readableMessage)
    }
}