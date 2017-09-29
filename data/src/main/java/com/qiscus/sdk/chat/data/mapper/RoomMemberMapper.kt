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

import com.qiscus.sdk.chat.data.model.MemberStateEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.domain.model.MemberState
import com.qiscus.sdk.chat.domain.model.RoomMember

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun RoomMemberEntity.toDomainModel(): RoomMember {
    return RoomMember(
            userEntity.toDomainModel(),
            MemberState(memberStateEntity.lastDeliveredCommentId, memberStateEntity.lastReadCommentId)
    )
}

fun RoomMember.toEntity(): RoomMemberEntity {
    return RoomMemberEntity(
            user.toEntity(),
            MemberStateEntity(memberState.lastDeliveredCommentId, memberState.lastReadCommentId)
    )
}