package com.qiscus.sdk.chat.presentation.model

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import org.json.JSONObject

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class ButtonViewModel(val label: String, val type: String, val payload: JSONObject)

open class ButtonLinkViewModel(label: String, type: String, payload: JSONObject) : ButtonViewModel(label, type, payload) {
    val url by lazy {
        payload.optString("url")
    }
}

open class ButtonPostBackViewModel(label: String, type: String, payload: JSONObject) : ButtonViewModel(label, type, payload)

open class ButtonAccountLinkingViewModel(label: String, type: String, payload: JSONObject) : ButtonViewModel(label, type, payload) {
    val url by lazy {
        payload.optString("url")
    }

    val finishUrl by lazy {
        payload.optString("redirect_url")
    }

    val title by lazy {
        payload.optString("view_title")
    }

    val successMessage by lazy {
        payload.optString("success_message")
    }
}

open class CommentButtonsViewModel
@JvmOverloads constructor(comment: Comment,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickHandler? = null)
    : CommentViewModel(comment, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val buttons by lazy {
        val buttonsArray = comment.type.payload.getJSONArray("buttons")
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
}