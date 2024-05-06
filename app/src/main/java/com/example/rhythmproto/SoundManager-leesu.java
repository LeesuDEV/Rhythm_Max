package com.example.rhythmproto;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {
    public static SoundManager instance;
    final private SoundPool soundPool;
    private int soundID;

    private SoundManager() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(20)
                .setAudioAttributes(audioAttributes)
                .build();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void loadSound(Context context) {
        soundID = soundPool.load(context, R.raw.clap, 1);
    }

    public void playSound() {
        if (soundID != 0) {
            soundPool.play(soundID, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public void release() {
        soundPool.release();
    }
}