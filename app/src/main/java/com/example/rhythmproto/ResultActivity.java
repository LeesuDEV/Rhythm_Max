package com.example.rhythmproto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    ImageView songImageView;  // 노래 이미지 텍스트뷰
    TextView songNameTV,difficultyTV;  // 노래이름,난이도 텍스트뷰
    TextView rankTV; // 랭크 텍스트뷰
    TextView perfectTV,greatTV,goodTV,badTV,missTV,totalTV;  // 퍼펙~배드 + 미스 텍스트뷰
    TextView scoreTV,maxComboTV,accuracyTV;  // 점수,맥스콤보,정확도 텍스트뷰
    Button gotoMainBtn; //로비로 가는 버튼

    int score; // 총 점수
    int perfect; // 퍼펙트 수
    int great;
    int good;  // ~
    int bad;
    int miss; // 미스 수

    int stackCombo;  //누적 콤보
    int maxCombo; // 최고 콤보
    String accuracy; // 정확도

    String songName;
    String songDifficulty;
    String songBPM;
    int songImage; //곡 이미지

    Intent intent;
    Double accuracy_double; //정확도를 double로 변환해서 담을 변수
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);

        if (getIntent() != null){
            intent = getIntent(); //인턴트 받아오기

            songImage = intent.getIntExtra("songImage",1); //GameActivity 에서 게임끝난 결고+곡정보를 인턴트로 보낸것을 받아서 변수에 저장
            songName = intent.getStringExtra("songName");
            songDifficulty = intent.getStringExtra("songDifficulty");
            songBPM  = intent.getStringExtra("songBpm");

            perfect = intent.getIntExtra("perfect",1);
            great =intent.getIntExtra("great",1);
            good = intent.getIntExtra("good",1);
            bad = intent.getIntExtra("bad",1);
            miss = intent.getIntExtra("miss",1);

            stackCombo = intent.getIntExtra("stackCombo",1);
            maxCombo = intent.getIntExtra("maxCombo",1);
            score = intent.getIntExtra("score",1);
            accuracy = intent.getStringExtra("accuracy");
        }

        songImageView = findViewById(R.id.sondImage);
        songNameTV = findViewById(R.id.songNameTV);
        difficultyTV = findViewById(R.id.difficultyTV);

        rankTV = findViewById(R.id.rankTV);

        perfectTV = findViewById(R.id.perfectTV);
        greatTV = findViewById(R.id.greatTV);
        goodTV = findViewById(R.id.goodTV);
        badTV = findViewById(R.id.badTV);
        missTV = findViewById(R.id.missTV);
        totalTV = findViewById(R.id.totalTV);

        scoreTV = findViewById(R.id.scoreTV);
        maxComboTV = findViewById(R.id.maxcomboTV);
        accuracyTV = findViewById(R.id.accuracyTV);

        gotoMainBtn = findViewById(R.id.gotoMainBtn);

        /*------------ 결과창에 게임정보 입력 시작------------*/

        songImageView.setImageResource(songImage);
        songNameTV.setText(""+songName);
        difficultyTV.setText(""+songDifficulty);

        perfectTV.setText(""+perfect);
        greatTV.setText(""+great);
        goodTV.setText(""+good);
        badTV.setText(""+bad);
        missTV.setText(""+miss);
        totalTV.setText(""+stackCombo);

        scoreTV.setText(""+score);
        maxComboTV.setText(""+maxCombo);
        accuracyTV.setText(""+accuracy);

        accuracy_double = Double.parseDouble(accuracy.replace("%","")); // String 정확도 값에서 %를 제외한 숫자를 가져옴 -> double에 저장하여 Rank계산

        if (accuracy_double >= 99.5){
            rankTV.setText("SS");
            rankTV.setTextColor(Color.parseColor("#FF8300")); //99.5점 이상일시 SS
        }
        else if (accuracy_double >= 97){
            rankTV.setText("S");
            rankTV.setTextColor(Color.parseColor("#F8D730")); //97점 이상일시 S
        }
        else if (accuracy_double >= 90){
            rankTV.setText("A");
            rankTV.setTextColor(Color.parseColor("#FF0000")); //90점 이상일시 A
        }
        else if (accuracy_double >= 80){
            rankTV.setText("B");
            rankTV.setTextColor(Color.parseColor("#1354FB")); //80점 이상일시 B
        }
        else if (accuracy_double >= 70){
            rankTV.setText("C");
            rankTV.setTextColor(Color.parseColor("#08FF00")); //70점 이상일시 C
        }
        else {
            rankTV.setText("D");
            rankTV.setTextColor(Color.parseColor("#A1A1A1")); //70점 미만일시 D
        }


        /*------------ 결과창에 게임정보 입력 끝------------*/

        gotoMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this,MainActivity.class);
                startActivity(intent); //메인으로 가기
                finish();
            }
        }); //버튼클릭시 메인으로 가기


    }
}
