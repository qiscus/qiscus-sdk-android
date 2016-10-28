package com.qiscus.sdk.util;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by RyMey on 6/8/16.
 */
public class QiscusRecordUtil extends TimerTask {
    private Activity activity;
    public static TextView recordingTimeText;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    public QiscusRecordUtil(Activity activity) {

        this.activity = activity;
    }

    @Override
    public void run() {
        recordingTimeText = (TextView) activity.findViewById(R.id.recording_time_text);

        timeInMilliseconds = SystemClock.uptimeMillis() - QiscusBaseChatFragment.startTime;
        updatedTime = timeSwapBuff + timeInMilliseconds;
        final String hms = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                        .toHours(updatedTime)),
                TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                        .toMinutes(updatedTime)));
        long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(updatedTime));
        System.out.println(lastsec + " hms " + hms);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (recordingTimeText != null)
                        recordingTimeText.setText(hms);
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        });
    }
}
