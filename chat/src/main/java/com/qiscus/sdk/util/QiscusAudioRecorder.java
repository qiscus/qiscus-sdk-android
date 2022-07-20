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

package com.qiscus.sdk.util;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created on : November 02, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusAudioRecorder {
    private MediaRecorder recorder;
    private boolean recording;
    private String fileName;

    public void startRecording() throws IOException {
        if (Build.VERSION.SDK_INT >= 29) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String audioFileName = "AUDIO_" + timeStamp + "_";
            File storageDir =
                    QiscusCore.getApps().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            storageDir.mkdirs();
            String file = storageDir.getAbsolutePath();
            file += File.separator + audioFileName + ".m4a";
            startRecording(file);
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String audioFileName = "AUDIO_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            storageDir.mkdirs();
            String file = storageDir.getAbsolutePath();
            file += File.separator + audioFileName + ".m4a";
            startRecording(file);
        }
    }

    private void startRecording(String fileName) throws IOException {
        if (!recording) {
            this.fileName = fileName;
            recording = true;
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(fileName);
            recorder.prepare();
            recorder.start();
        }
    }

    public File stopRecording() {
        cancelRecording();
        return new File(fileName);
    }

    public void cancelRecording() {
        if (recording) {
            recording = false;
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
            } catch (Exception e) {
                QiscusErrorLogger.print(e);
            }
        }
    }

    public boolean isRecording() {
        return recording;
    }
}
