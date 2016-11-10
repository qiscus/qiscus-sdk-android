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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.util.QiscusAudioRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created on : November 02, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusAudioRecorderView extends LinearLayout {
    private ImageView buttonStopRecord;
    private ImageView buttonCancelRecord;
    private TextView textViewDuration;
    private QiscusAudioRecorder recorder;
    protected long startTime;
    private Handler handler;
    private RecordListener listener;

    public QiscusAudioRecorderView(Context context) {
        super(context);
    }

    public QiscusAudioRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        recorder = new QiscusAudioRecorder();
        handler = new Handler();
        injectViews();
        applyAttrs(context, attrs);
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_qiscus_audio_recorder, this);
        buttonStopRecord = (ImageView) findViewById(R.id.button_stop_record);
        buttonCancelRecord = (ImageView) findViewById(R.id.button_cancel_record);
        textViewDuration = (TextView) findViewById(R.id.record_duration);
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.QiscusAudioRecorderView, 0, 0);
        Drawable buttonStopRecordDrawable;
        Drawable buttonCancelRecordDrawable;
        try {
            buttonStopRecordDrawable = a.getDrawable(R.styleable.QiscusAudioRecorderView_qrecord_stopSrc);
            buttonCancelRecordDrawable = a.getDrawable(R.styleable.QiscusAudioRecorderView_qrecord_cancelSrc);
        } finally {
            a.recycle();
        }

        if (buttonStopRecordDrawable != null) {
            buttonStopRecord.setImageDrawable(buttonStopRecordDrawable);
        }

        if (buttonCancelRecordDrawable != null) {
            buttonCancelRecord.setImageDrawable(buttonCancelRecordDrawable);
        }

        initLayout();
    }

    private void initLayout() {
        buttonStopRecord.setOnClickListener(v -> stopRecord());
        buttonCancelRecord.setOnClickListener(v -> cancelRecord());
        resetDuration();
    }

    public void setRecordListener(RecordListener listener) {
        this.listener = listener;
    }

    private void resetDuration() {
        textViewDuration.setText("00:00");
        startTime = System.currentTimeMillis();
    }

    public void startRecord() throws IOException {
        recorder.startRecording();
        startTime = System.currentTimeMillis();
        handler.postDelayed(timer, 1000L);
        if (listener != null) {
            listener.onStartRecord();
        }
    }

    public void stopRecord() {
        if (listener != null) {
            listener.onStopRecord(recorder.stopRecording());
        }
        resetDuration();
    }

    public void cancelRecord() {
        recorder.cancelRecording();
        resetDuration();
        if (listener != null) {
            listener.onCancelRecord();
        }
    }

    public boolean isRecording() {
        return recorder.isRecording();
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long seconds = (currentTime - startTime) / 1000;
            String formattedDuration = DateUtils.formatElapsedTime(seconds);
            textViewDuration.setText(formattedDuration);
            if (isRecording()) {
                handler.postDelayed(this, 1000L);
            }
        }
    };

    public interface RecordListener {
        void onStartRecord();

        void onCancelRecord();

        void onStopRecord(File audioFile);
    }
}
