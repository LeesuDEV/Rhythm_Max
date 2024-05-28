package com.example.rhythmproto;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class SettingDialog extends Dialog {
    SeekBar previewSoundBar;
    SeekBar ingameSoundBar;
    TextView fastSync;
    TextView slowSync;
    TextView currentSyncTV;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조
    Context context;
    SwitchCompat backgroundSwitch;

    public SettingDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_dialog);

        previewSoundBar = findViewById(R.id.previewSoundBar);
        ingameSoundBar = findViewById(R.id.ingameSoundBar);
        fastSync = findViewById(R.id.syncFastBtn);
        slowSync = findViewById(R.id.syncSlowBtn);
        currentSyncTV = findViewById(R.id.currentSyncTV);
        backgroundSwitch = findViewById(R.id.backgroundSwitch);

        previewSoundBar.setProgress((int) (MainActivity.previewSoundAmountIndex * 10)); // 현재 프리뷰볼륨으로 setting progress값 설정
        previewSoundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.previewSoundAmountIndex = (float) progress / 10; // 설정값을 배경음악 프로그래스에 설정
                MainActivity.mediaPlayer.setVolume(MainActivity.previewSoundAmountIndex, MainActivity.previewSoundAmountIndex);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }); // 프리뷰볼륨 조절

        ingameSoundBar.setProgress((int) (MainActivity.ingameSoundAmountIndex * 10)); // 현재 인게임볼륨으로 setting progress값 설정
        ingameSoundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.ingameSoundAmountIndex = (float) progress / 10; // 설정값을 배경음악 프로그래스에 설정
                Log.d("check", String.valueOf(((int) MainActivity.ingameSoundAmountIndex * 10)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }); // 인게임볼륨 조절

        currentSyncTV.setText("" + MainActivity.syncValue);

        backgroundSwitch.setChecked(MainActivity.backgroundIndex); //초기 백그라운드 onoff 스위치 설정값
        backgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.backgroundIndex = true;
                } else {
                    MainActivity.backgroundIndex = false;
                }
            }
        });

        fastSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.syncValue -= 50;
                currentSyncTV.setText("" + MainActivity.syncValue);
            }
        });

        slowSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.syncValue += 50;
                currentSyncTV.setText("" + MainActivity.syncValue);
            }
        });

    }

    @Override
    public void dismiss() {
        HashMap<String, Object> setting = new HashMap<>();
        setting.put("ingame", MainActivity.ingameSoundAmountIndex);
        setting.put("preview", MainActivity.previewSoundAmountIndex); // 해쉬맵에 인게임,프리뷰 볼륨을 담아서 파이어베이스에 올릴준비를함
        setting.put("background", MainActivity.backgroundIndex);

        firestore.collection("users")
                .document(LoginActivity.userId)
                .collection("setting")
                .document("sound")
                .set(setting, SetOptions.merge()); // 유저세팅값을 업로드

        HashMap<String, Object> sync = new HashMap<>();
        sync.put("value", MainActivity.syncValue); // 싱크값도 넣을준비를 함

        firestore.collection("users")
                .document(LoginActivity.userId)
                .collection("setting")
                .document("sync")
                .set(sync, SetOptions.merge()); // 유저싱크값을 업로드
        super.dismiss();
    } // 세팅창을 닫을때 서버에 볼륨값,싱크값을 업로드함.
}
