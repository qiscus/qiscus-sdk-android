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

package com.qiscus.sdk.ui.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.qiscus.manggil.mention.MentionSpanConfig;
import com.qiscus.manggil.mention.Mentionable;
import com.qiscus.manggil.suggestions.SuggestionsAdapter;
import com.qiscus.manggil.suggestions.SuggestionsResult;
import com.qiscus.manggil.suggestions.interfaces.SuggestionsListBuilder;
import com.qiscus.manggil.suggestions.interfaces.SuggestionsVisibilityManager;
import com.qiscus.manggil.tokenization.QueryToken;
import com.qiscus.manggil.tokenization.impl.WordTokenizer;
import com.qiscus.manggil.tokenization.interfaces.QueryTokenReceiver;
import com.qiscus.manggil.ui.MentionsEditText;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.data.model.QiscusMentionConfig;
import com.qiscus.sdk.ui.adapter.QiscusMentionSuggestionBuilder;
import com.qiscus.sdk.util.QiscusConverterUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on : September 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusMentionSuggestionView extends FrameLayout implements QueryTokenReceiver, SuggestionsVisibilityManager {
    private static final String BUCKET = "member-memory";

    private MentionsEditText editText;
    private ListView listView;

    private List<QParticipant> members;
    private SuggestionsAdapter adapter;

    private QAccount qAccount = Qiscus.getQiscusAccount();

    public QiscusMentionSuggestionView(@NonNull Context context) {
        super(context);
    }

    public QiscusMentionSuggestionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_qiscus_mention_suggestion, this);
        listView = findViewById(R.id.list_suggestion);
    }

    public void bind(MentionsEditText editText) {
        this.editText = editText;
        setupEditText();
    }

    public void setRoomMembers(List<QParticipant> members) {
        this.members = members;
    }

    private void setupEditText() {
        editText.setQueryTokenReceiver(this);
        editText.setTokenizer(new WordTokenizer());
        editText.setAvoidPrefixOnTap(true);
        editText.setSuggestionsVisibilityManager(this);

        QiscusMentionConfig mentionConfig = Qiscus.getChatConfig().getMentionConfig();

        MentionSpanConfig.Builder builder = new MentionSpanConfig.Builder();
        builder.setMentionTextColor(mentionConfig.getEditTextMentionOtherColor());
        builder.setSelectedMentionTextBackgroundColor(mentionConfig.getEditTextMentionOtherColor());
        editText.setMentionSpanConfig(builder.build());

        // Set the suggestions adapter
        SuggestionsListBuilder listBuilder = new QiscusMentionSuggestionBuilder();
        adapter = new SuggestionsAdapter(getContext(), this, listBuilder);
        listView.setAdapter(adapter);

        // Set the item click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Mentionable mention = (Mentionable) adapter.getItem(position);
            if (editText != null) {
                editText.insertMention(mention);
                adapter.clear();
            }
        });
    }

    @Override
    public List<String> onQueryReceived(@NonNull QueryToken queryToken) {
        List<String> buckets = Collections.singletonList(BUCKET);
        List<QParticipant> suggestions = getSuggestions(queryToken);
        final SuggestionsResult result = new SuggestionsResult(queryToken, suggestions);
        listView.post(() -> {
            if (adapter != null) {
                adapter.addSuggestions(result, BUCKET, editText);
            }
        });
        return buckets;
    }

    private List<QParticipant> getSuggestions(QueryToken queryToken) {
        ArrayList<QParticipant> suggestions = new ArrayList<>();
        String namePrefix = queryToken.getKeywords().toLowerCase();
        if (members != null) {
            for (QParticipant suggestion : members) {
                if (qAccount.getId().equals(suggestion.getId())) {
                    continue;
                }

                if (suggestion.getName().toLowerCase().startsWith(namePrefix)) {
                    suggestions.add(suggestion);
                }
            }
        }
        return suggestions;
    }

    @Override
    public void displaySuggestions(boolean display) {
        // If nothing to change, return early
        if (display == isDisplayingSuggestions() || editText == null) {
            return;
        }

        listView.setVisibility(display ? VISIBLE : GONE);

        requestLayout();
        invalidate();
    }

    @Override
    public boolean isDisplayingSuggestions() {
        return listView.getVisibility() == VISIBLE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) QiscusConverterUtil.dp2px(getResources(), 160),
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
