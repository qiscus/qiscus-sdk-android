package com.qiscus.sdk.ui.view;

import android.content.Context;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.qiscus.manggil.ui.MentionsEditText;
import com.qiscus.sdk.chat.core.util.BuildVersionUtil;

/**
 * Created on : August 08, 2017
 * Author     : rajapulau
 * Name       : Ganjar Widiatmansyah
 * GitHub     : https://github.com/rajapulau
 */
public class QiscusEditText extends MentionsEditText {
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
            if (BuildVersionUtil.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
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
