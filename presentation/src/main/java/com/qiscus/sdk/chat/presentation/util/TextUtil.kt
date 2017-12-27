package com.qiscus.sdk.chat.presentation.util

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.text.*
import android.text.style.ClickableSpan
import android.view.View
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.model.MentionClickListener

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

@JvmOverloads
fun getString(context: Context = Qiscus.instance.component.application, @StringRes resId: Int): String {
    return context.getString(resId)
}

@JvmOverloads
fun getString(context: Context = Qiscus.instance.component.application, @StringRes resId: Int, vararg formatArgs: Any): String {
    return context.getString(resId, formatArgs)
}

fun String.toReadableText(userRepository: UserRepository): String {

    if (isBlank()) {
        return ""
    }

    val stringBuilder = StringBuilder()
    var lastNotMention = 0
    var startPosition = 0
    var ongoing = false

    for (i in 0 until length) {
        if (!ongoing && i < length - 1 && this[i] == '@' && this[i + 1] == '[') {
            ongoing = true
            startPosition = i
        }

        if (ongoing && this[i] == ']') {
            val mentionedUserId = substring(startPosition + 2, i)
            val mentionedUser = userRepository.getUser(mentionedUserId).blockingGet()

            if (mentionedUser != null) {
                val mention = "@${mentionedUser.name}"


                if (lastNotMention != startPosition) {
                    stringBuilder.append(substring(lastNotMention, startPosition))
                }

                stringBuilder.append(mention)
                lastNotMention = i + 1
            }
            ongoing = false
        }
    }

    if (lastNotMention < length) {
        stringBuilder.append(substring(lastNotMention, length))
    }

    return stringBuilder.toString()
}

@JvmOverloads
fun String.toSpannable(account: Account,
                       userRepository: UserRepository,
                       @ColorInt mentionColor: Int,
                       mentionClickListener: MentionClickListener? = null): Spannable {

    return toSpannable(account, userRepository, mentionColor, mentionColor, mentionColor, mentionClickListener)
}

@JvmOverloads
fun String.toSpannable(account: Account,
                       userRepository: UserRepository,
                       @ColorInt mentionAllColor: Int,
                       @ColorInt mentionOtherColor: Int,
                       @ColorInt mentionMeColor: Int,
                       mentionClickListener: MentionClickListener? = null): Spannable {
    if (isBlank()) {
        return SpannableString("")
    }

    val spannable = SpannableStringBuilder()
    var lastNotMention = 0
    var startPosition = 0
    var ongoing = false

    for (i in 0 until length) {
        if (!ongoing && i < length - 1 && this[i] == '@' && this[i + 1] == '[') {
            ongoing = true
            startPosition = i
        }

        if (ongoing && this[i] == ']') {
            val mentionedUserId = substring(startPosition + 2, i)
            val mentionedUser = userRepository.getUser(mentionedUserId).blockingGet()

            if (mentionedUser != null) {
                val mention = SpannableString("@" + mentionedUser.name)

                mention.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        mentionClickListener?.onMentionClick(mentionedUser)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        when (mentionedUserId) {
                            "all" -> ds.color = mentionAllColor
                            account.user.id -> ds.color = mentionMeColor
                            else -> ds.color = mentionOtherColor
                        }
                    }
                }, 0, mention.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (lastNotMention != startPosition) {
                    spannable.append(substring(lastNotMention, startPosition))
                }

                spannable.append(mention)
                lastNotMention = i + 1
            }
            ongoing = false
        }
    }

    if (lastNotMention < length) {
        spannable.append(substring(lastNotMention, length))
    }

    return spannable
}