package com.qiscus.sdk.chat.presentation.model

import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageCardViewModel
@JvmOverloads constructor(message: Message,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)
    : MessageViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val cardImage by lazy {
        message.type.payload.optString("image")
    }

    val cardTitle by lazy {
        message.type.payload.optString("title")
    }

    val cardDescription by lazy {
        message.type.payload.optString("description")
    }

    val cardUrl by lazy {
        message.type.payload.optString("url")
    }

    val buttons by lazy {
        val buttonsArray = message.type.payload.getJSONArray("buttons")
        val size = buttonsArray.length()
        val buttons = arrayListOf<ButtonViewModel>()
        (0 until size).map { buttonsArray.getJSONObject(it) }
                .mapTo(buttons) {
                    when {
                        it.optString("type") == "link" -> ButtonLinkViewModel(it.optString("label", "Button"),
                                it.optString("type"), it.optJSONObject("payload"))
                        it.optString("type") == "postback" -> ButtonPostBackViewModel(it.optString("label", "Button"),
                                it.optString("type"), it.optJSONObject("payload"))
                        else -> ButtonViewModel(it.optString("label", "Button"),
                                it.optString("type"), it.optJSONObject("payload"))
                    }
                }
        return@lazy buttons
    }

    override fun determineReadableMessage(): String {
        return message.type.payload.optString("text", message.text)
    }

    override fun determineSpannableMessage(): Spannable {
        return message.type.payload.optString("text", message.text)
                .toSpannable(account, userRepository, mentionAllColor, mentionOtherColor,
                        mentionMeColor, mentionClickListener)
    }
}