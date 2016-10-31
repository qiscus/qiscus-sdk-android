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
    ImageButton buttonPlay;
    ImageButton buttonPause;
    ImageButton imageButtonClose;
    SeekBar seekBarSound;
    TextView textViewCurrentPos;
    TextView textViewStopPos;

    private Handler myHandler = new Handler();
    private MediaPlayer mediaPlayer;
    private double startTimeCurentPos = 0;
    private double finalTime = 0;
    private int oneTimeOnly = 0;

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
        buttonPlay= (ImageButton) view.findViewById(R.id.buttonPlay);
        buttonPause= (ImageButton) view.findViewById(R.id.buttonPause);
        imageButtonClose= (ImageButton) view.findViewById(R.id.imageButtonClose);
        seekBarSound= (SeekBar) view.findViewById(R.id.seekBarSound);
        textViewCurrentPos= (TextView)view.findViewById(R.id.textViewCurrentPos);
        textViewStopPos= (TextView) view.findViewById(R.id.textViewStopPos);

        startTimeCurentPos = 0;
        finalTime = 0;
        oneTimeOnly = 0;


        buttonPlay.setOnClickListener(v -> soundPlay());

        buttonPause.setOnClickListener(v -> soundPause());

        imageButtonClose.setOnClickListener(v -> cancel());

    }

    public void soundPlay() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(QiscusSoundDialog.file + "");
                mediaPlayer.prepare();
                seekBarSound.setMax(mediaPlayer.getDuration());
                finalTime = mediaPlayer.getDuration();
                startTimeCurentPos = mediaPlayer.getCurrentPosition();
                try {
                    textViewStopPos.setText(String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
                    );

                    textViewCurrentPos.setText(String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTimeCurentPos),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTimeCurentPos) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTimeCurentPos)))
                    );

                    seekBarSound.setProgress((int) startTimeCurentPos);
                    myHandler.postDelayed(UpdateSongTime, 100);
                    buttonPlay.setVisibility(View.GONE);
                    buttonPause.setVisibility(View.VISIBLE);
                    mediaPlayer.start();

                    seekBarSound.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            mediaPlayer.seekTo(seekBarSound.getProgress());
                            return false;
                        }
                    });
                }catch (NullPointerException x){
                    x.printStackTrace();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer playSound) {
                    try {
                        buttonPlay.setVisibility(View.VISIBLE);
                        buttonPause.setVisibility(View.GONE);
                    }catch (NullPointerException e){
                    }

                }
            });
        } else {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                        // Changing button image to play button
                        buttonPlay.setVisibility(View.VISIBLE);
                        buttonPause.setVisibility(View.GONE);
                    }
                } else {
                    // Resume song
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        // Changing button image to pause button
                        buttonPlay.setVisibility(View.GONE);
                        buttonPause.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    }

    public void soundPause() {
        if (mediaPlayer.isPlaying()) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                // Changing button image to play button
                buttonPlay.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.GONE);
            }
        } else {
            // Resume song
            if (mediaPlayer != null) {
                mediaPlayer.start();
                // Changing button image to pause button
                buttonPlay.setVisibility(View.GONE);
                buttonPause.setVisibility(View.VISIBLE);
            }
        }
    }


    public void cancel() {
        dismiss();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = null;
    }


    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (mediaPlayer != null) {
                try {
                    startTimeCurentPos = mediaPlayer.getCurrentPosition();
                    textViewCurrentPos.setText(String.format("%d min, %d sec",

                            TimeUnit.MILLISECONDS.toMinutes((long) startTimeCurentPos),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTimeCurentPos) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTimeCurentPos)))
                    );
                    seekBarSound.setProgress((int) startTimeCurentPos);
                    myHandler.postDelayed(this, 100);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (NullPointerException x){
                }
            }

        }
    };


}