package com.example.rhythmproto;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class SelectSongDialog extends Dialog {
    TextView songNameTV;
    TextView difficultyTV;
    TextView bpmTV;
    TextView myBestScoreTV;
    TextView myBestRateTV;
    ImageView myBestRankImg;
    String bestRank; //최고랭크

    ImageView startBtn;

    ImageView setSpeedBtn;
    ImageView gameModBtn;
    ImageView autoModBtn;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조
    Activity activity;
    Context context;

    public SelectSongDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_setting_dialog);
        //songImageView = findViewById(R.id.selectSongImageView);
        songNameTV = findViewById(R.id.songNameTV);
        difficultyTV = findViewById(R.id.difficultyTV);
        bpmTV = findViewById(R.id.bpmTV);

        startBtn = findViewById(R.id.startBtn);

        setSpeedBtn = findViewById(R.id.setSpeedBtn);
        gameModBtn = findViewById(R.id.gameModBtn);
        autoModBtn = findViewById(R.id.autoModBtn);

        myBestScoreTV = findViewById(R.id.myBestScoreTV);
        myBestRateTV = findViewById(R.id.myBestRateTV);
        myBestRankImg = findViewById(R.id.myBestRankImg);

        activity = (Activity) context;

       // songImageView.setImageResource(MainActivity.songImage);  // 선택된곡의 이미지 세팅
        songNameTV.setText(MainActivity.songName);  // 선택된곡의 이름 세팅

        MovingText.moveText(songNameTV); //옆으로 슬라이드 되는 텍스트뷰

        bpmTV.setText("bpm : " + MainActivity.songBPM);  // 선택된곡의 bpm 세팅
        if (MainActivity.difficulty == 1) {
            MainActivity.easyModDifficultyColor(MainActivity.songDifficulty, difficultyTV);
        } else {
            MainActivity.hardModDifficultyColor(MainActivity.songDifficulty, difficultyTV);
        } //이지모드는 이지모드색깔로 텍스트뷰 설정, 하드모드는 하드모드 색깔로 텍스트뷰 설정

        MainActivity.setDefaultSpeed(MainActivity.speedIndex, setSpeedBtn); //다이어로그 구성시 초기 인덱스값(혹은 db에서 받아온 유저의 배속값)을 버튼+배속에 설정
        MainActivity.setDefaultAutoMode(MainActivity.autoModIndex, autoModBtn); //화면 구성시 오토모드 인덱스값을 텍스트에 부여
        MainActivity.setDefaultGameMode(MainActivity.gameModIndex, gameModBtn);
        loadBestScore(); //화면구성시 최고기록을 가져오는 메소드

        setSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (MainActivity.speedIndex) {
                    case 1:
                        MainActivity.speedIndex = 2; // 배속 식별값을 2로변경 (1.5배 식별값)
                        MainActivity.setSpeed = 2000; // 1.5배속 값을 세팅
                        MainActivity.setSpeedJudgment = 1.5f; // 1.5배율을 세팅
                        setSpeedBtn.setImageResource(R.drawable.speedbtn_15x_img);//버튼 이미지를 1.5배속 이미지로 변경
                        break;
                    case 2:
                        MainActivity.speedIndex = 3;
                        MainActivity.setSpeed = 1500;
                        MainActivity.setSpeedJudgment = 2f;
                        setSpeedBtn.setImageResource(R.drawable.speedbtn_2x_img);
                        break;
                    case 3:
                        MainActivity.speedIndex = 4;
                        MainActivity.setSpeed = 1200;
                        MainActivity.setSpeedJudgment = 2.5f;
                        setSpeedBtn.setImageResource(R.drawable.speedbtn_25x_img);
                        break;
                    case 4:
                        MainActivity.speedIndex = 5;
                        MainActivity.setSpeed = 1000;
                        MainActivity.setSpeedJudgment = 3;
                        setSpeedBtn.setImageResource(R.drawable.speedbtn_3x_img);
                        break;
                    case 5:
                        MainActivity.speedIndex = 6;
                        MainActivity.setSpeed = 857;
                        MainActivity.setSpeedJudgment = 3.5f;
                        setSpeedBtn.setImageResource(R.drawable.speedbtn_35x_img);
                        break;
                    case 6:
                        MainActivity.speedIndex = 1;
                        MainActivity.setSpeed = 3000;
                        MainActivity.setSpeedJudgment = 1;
                        setSpeedBtn.setImageResource(R.drawable.speedbtn_1x_img);// 배속으로 다시 변경
                        break;
                }
            }
        }); // 버튼클릭시 배속설정을 바꾸는 리스너
        gameModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (MainActivity.gameModIndex) {
                    case 0:
                        gameModBtn.setImageResource(R.drawable.mirrorbtn_img); //게임모드 버튼을 미러모드 이미지로 변경
                        MainActivity.gameModIndex = 1; //게임모드 인덱스를 1로변경
                        break;
                    case 1:
                        gameModBtn.setImageResource(R.drawable.randombtn_img);
                        MainActivity.gameModIndex = 2;
                        break;
                    case 2:
                        gameModBtn.setImageResource(R.drawable.normalbtn_img);
                        MainActivity.gameModIndex = 0;
                        break;
                }
            }
        }); // 버튼클릭시 모드를 바꾸는 리스너
        autoModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.autoModIndex) {
                    autoModBtn.setImageResource(R.drawable.nonebtn_img); // 일반모드로 변경
                    MainActivity.autoModIndex = false;
                } else {
                    autoModBtn.setImageResource(R.drawable.autobtn_img);
                    MainActivity.autoModIndex = true;
                }
            }
        }); //버튼 클릭시 오토모드를 바꿔주는 리스너
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.notes = OsuFileParser.parseOsuFile(context, MainActivity.noteData);  // 선택된곡 노트데이터를 리스트에 삽입
                Intent intent = new Intent(context, LoadingActivity.class); // 인턴트생성

                activity.finish();  // 현재 액티비티 종료

                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃

                context.startActivity(intent);

                dismiss();
            }
        }); // 게임정보를 넘기고 게임스타트를 해주는 스타트버튼 리스너
    }

    @Override
    public void dismiss() {
        HashMap<String, Object> setting = new HashMap<>();
        setting.put("automode", MainActivity.autoModIndex);
        setting.put("gamemode", MainActivity.gameModIndex);
        setting.put("speed", MainActivity.speedIndex); // 해쉬맵에 배속,게임모드,오토모드 인덱스를 담아서 파이어베이스에 올릴준비를함

        firestore.collection("users")
                .document(LoginActivity.userId)
                .collection("setting")
                .document("mode")
                .set(setting, SetOptions.merge()); // 유저 곡세팅을 업로드

        MainActivity.song_difficulty_Main.setVisibility(View.VISIBLE);
        MainActivity.song_name_Main.setVisibility(View.VISIBLE);

        Animation fadeIn = AnimationUtils.loadAnimation(context,R.anim.fade_in_text);
        fadeIn.setDuration(500);
        MainActivity.song_name_Main.startAnimation(fadeIn);
        MainActivity.song_difficulty_Main.startAnimation(fadeIn);

        super.dismiss();

    }

    public void loadBestScore() {
        firestore.collection("users")
                .document(LoginActivity.userId)
                .collection("bestRecord")
                .document(MainActivity.songName)
                .get().addOnSuccessListener(documentSnapshot -> { //불러오는데 성공했다면(최고기록이 있다면)
                    if (documentSnapshot.exists()) {
                        String bestAccuracy = String.valueOf(documentSnapshot.getDouble("accuracy").doubleValue());
                        String bestScore = String.valueOf(documentSnapshot.getLong("score").intValue());

                        myBestRateTV.setText(bestAccuracy); //최고정확도를 가져옴
                        myBestScoreTV.setText(bestScore); //최고점수를 가져옴

                        ResultActivity.setRank(Double.parseDouble(bestAccuracy),bestRank,myBestRankImg); // 정확도를 기반으로 resultRank에 랭크를 입력해주고 텍스트뷰에 표시 해주는 메소드
                    } else { // 최고기록이 없다면 (데이터 null)
                        myBestRateTV.setText("No Data"); //최고정확도를 비움
                        myBestScoreTV.setText("No Data"); //최고점수를 비움
                        myBestRankImg.setVisibility(View.INVISIBLE); //최고랭크를 비움
                    }
                }).addOnFailureListener(e -> {
                });
    } // 최고점수를 가져오는 메소드
}
