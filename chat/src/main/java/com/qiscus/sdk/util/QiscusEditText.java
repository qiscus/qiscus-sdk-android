package com.qiscus.sdk.util;

import android.content.Context;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.vanniktech.emoji.EmojiEditText;

/**
 * Created by rajapulau on 8/8/17.
 */

public class QiscusEditText extends EmojiEditText {
    private CommitListener commitListener;

    public QiscusEditText(Context context) {
        super(context);
    }

    public QiscusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public InputConnection onCreateInputConnection(final EditorInfo info) {
        final InputConnection ic = super.onCreateInputConnection(info);
        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/gif"});
        final InputConnectionCompat.OnCommitContentListener callback = (info1, flags, opts) -> {
            if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    info1.requestPermission();
                } catch (Exception e) {
                    return false;
                }
            }
            if (commitListener != null) {
                commitListener.onCommitContent(info1);
            }
            return true;
        };
        return InputConnectionCompat.createWrapper(ic, info, callback);
    }

    public void setCommitListener(CommitListener listener) {
        this.commitListener = listener;
    }

    public interface CommitListener {
        void onCommitContent(InputContentInfoCompat infoCompat);
    }
}
