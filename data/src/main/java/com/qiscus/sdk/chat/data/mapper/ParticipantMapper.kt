/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.ParticipantStateEntity
import com.qiscus.sdk.chat.data.model.ParticipantEntity
import com.qiscus.sdk.chat.domain.model.ParticipantState
import com.qiscus.sdk.chat.domain.model.Participant

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun ParticipantEntity.toDomainModel(): Participant {
    return Participant(
            userEntity.toDomainModel(),
            ParticipantState(stateEntity.lastDeliveredMessageId, stateEntity.lastReadMessageId)
    )
}

fun Participant.toEntity(): ParticipantEntity {
    return ParticipantEntity(
            user.toEntity(),
            ParticipantStateEntity(state.lastDeliveredMessageId, state.lastReadMessageId)
    )
}