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

package com.qiscus.sdk.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.qiscus.manggil.suggestions.SuggestionsResult;
import com.qiscus.manggil.suggestions.interfaces.Suggestible;
import com.qiscus.manggil.suggestions.interfaces.SuggestionsListBuilder;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on : September 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusMentionSuggestionBuilder implements SuggestionsListBuilder {
    @NonNull
    @Override
    public List<Suggestible> buildSuggestions(@NonNull Map<String, SuggestionsResult> latestResults,
                                              @NonNull String currentTokenString) {

        List<Suggestible> results = new ArrayList<>();
        for (Map.Entry<String, SuggestionsResult> entry : latestResults.entrySet()) {
            SuggestionsResult result = entry.getValue();
            if (currentTokenString.equalsIgnoreCase(result.getQueryToken().getTokenString())) {
                results.addAll(result.getSuggestions());
            }
        }
        return results;
    }

    @NonNull
    @Override
    public View getView(@NonNull Suggestible suggestion, @Nullable View convertView, ViewGroup parent,
                        @NonNull Context context, @NonNull LayoutInflater inflater, @NonNull Resources resources) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_qiscus_mention_suggestion, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = view.findViewById(R.id.name);
        textView.setText(suggestion.getSuggestiblePrimaryText());

        if (suggestion instanceof QiscusRoomMember) {
            ImageView imageView = view.findViewById(R.id.avatar);
            QiscusRoomMember member = (QiscusRoomMember) suggestion;
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions()
                            .error(com.qiscus.sdk.R.drawable.ic_qiscus_avatar)
                            .placeholder(com.qiscus.sdk.R.drawable.ic_qiscus_avatar)
                            .dontAnimate())
                    .load(member.getAvatar())
                    .into(imageView);
        }

        return view;
    }
}
