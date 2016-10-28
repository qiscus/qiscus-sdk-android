package com.qiscus.sdk.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.trello.rxlifecycle.components.support.RxDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created on : June 07, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusCustomDialog extends RxDialogFragment {
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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dialog_qiscus_sound,container,false);

        buttonPlay= (ImageButton) view.findViewById(R.id.buttonPlay);
        buttonPause= (ImageButton) view.findViewById(R.id.buttonPause);
        imageButtonClose= (ImageButton) view.findViewById(R.id.imageButtonClose);
        seekBarSound= (SeekBar) view.findViewById(R.id.seekBarSound);
        textViewCurrentPos= (TextView)view.findViewById(R.id.textViewCurrentPos);
        textViewStopPos= (TextView) view.findViewById(R.id.textViewStopPos);

        startTimeCurentPos = 0;
        finalTime = 0;
        oneTimeOnly = 0;

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPlay();
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPause();
            }
        });

        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onViewReady(savedInstanceState);
    }

    protected abstract int getResourceLayout();

    protected abstract void onViewReady(@Nullable Bundle savedInstanceState);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
