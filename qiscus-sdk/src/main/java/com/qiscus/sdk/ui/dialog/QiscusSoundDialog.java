package com.qiscus.sdk.ui.dialog;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


import com.qiscus.sdk.R;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Created by arief on 08/08/16.
 */
public class QiscusSoundDialog extends QiscusCustomDialog {

    private Context context;
    public static String name;
    public static File file;

    public static QiscusSoundDialog newInstance(Context context, String name, File file) {
        QiscusSoundDialog dialog = new QiscusSoundDialog();
        dialog.context = context;
        dialog.name = name;
        dialog.file = file;
        return dialog;
    }

    @Override
    protected int getResourceLayout() {
        return R.layout.dialog_qiscus_sound;
    }

    @Override
    protected void onViewReady(@Nullable Bundle savedInstanceState) {
        setCancelable(false);
    }


}