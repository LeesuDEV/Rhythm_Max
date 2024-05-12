package com.example.rhythmproto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {

    ImageView songImageView;  // 노래 이미지 텍스트뷰
    TextView songNameTV, difficultyTV;  // 노래이름,난이도 텍스트뷰
    TextView rankTV; // 랭크 텍스트뷰
    TextView perfectTV, greatTV, goodTV, badTV, missTV, totalTV;  // 퍼펙~배드 + 미스 텍스트뷰
    TextView scoreTV, maxComboTV, accuracyTV;  // 점수,맥스콤보,정확도 텍스트뷰
    TextView gotoMainBtn; //로비로 가는 버튼
    TextView automodeTV; //오토모드 표시 텍스트뷰

    long score; // 총 점수
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

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String resultRank; //결과랭크

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);

        if (getIntent() != null) {
            intent = getIntent(); //인턴트 받아오기

            songImage = intent.getIntExtra("songImage", 1); //GameActivity 에서 게임끝난 결고+곡정보를 인턴트로 보낸것을 받아서 변수에 저장
            songName = intent.getStringExtra("songName");
            songDifficulty = intent.getStringExtra("songDifficulty");
            songBPM = intent.getStringExtra("songBpm");

            perfect = intent.getIntExtra("perfect", 1);
            great = intent.getIntExtra("great", 1);
            good = intent.getIntExtra("good", 1);
            bad = intent.getIntExtra("bad", 1);
            miss = intent.getIntExtra("miss", 1);

            stackCombo = intent.getIntExtra("stackCombo", 1);
            maxCombo = intent.getIntExtra("maxCombo", 1);
            score = intent.getLongExtra("score", 1);
            accuracy = intent.getStringExtra("accuracy");
        }

        songImageView = findViewById(R.id.sondImage);
        songNameTV = findViewById(R.id.songNameTV);
        difficultyTV = findViewById(R.id.difficultyTV);

        rankTV = findViewById(R.id.resultRankTV);

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
        automodeTV = findViewById(R.id.automodeTV);

        /*------------ 결과창에 게임정보 입력 시작------------*/

        songImageView.setImageResource(songImage);
        songNameTV.setText("" + songName);
        difficultyTV.setText("" + songDifficulty);

        perfectTV.setText("" + perfect);
        greatTV.setText("" + great);
        goodTV.setText("" + good);
        badTV.setText("" + bad);
        missTV.setText("" + miss);
        totalTV.setText("" + stackCombo);

        scoreTV.setText("" + score);
        maxComboTV.setText("" + maxCombo);
        accuracyTV.setText("" + accuracy);

        accuracy_double = Double.parseDouble(accuracy.replace("%", "")); // String 정확도 값에서 %를 제외한 숫자를 가져옴 -> double에 저장하여 Rank계산

        setRank(accuracy_double, resultRank, rankTV); // 정확도를 기반으로 resultRank에 랭크를 입력해주고 텍스트뷰에 표시 해주는 메소드


        updateBestScore(); //최고기록 DB등록 메소드

        /*------------ 결과창에 게임정보 입력 끝------------*/

        gotoMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent); //메인으로 가기
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃
            }
        }); //버튼클릭시 메인으로 가기
    }

    public void updateBestScore() {

        if (!MainActivity.autoModIndex) {  // 오토모드 비활성화시에만 기록 갱신 {

            HashMap<String, Object> record = new HashMap<>();
            record.put("score", score);
            record.put("accuracy", accuracy_double);
            record.put("maxcombo", maxCombo);
            record.put("recordtime", new Date());

            firestore.collection("users")
                    .document(LoginActivity.userId)
                    .collection("bestRecord")
                    .document(MainActivity.songName)
                    .get().addOnSuccessListener(documentSnapshot -> { //데이터베이스에서 기록이 있는지 체크
                        if (documentSnapshot.exists()) { //추출이 됐다면(기록이 있다면)
                            if (documentSnapshot.getLong("score").longValue() < score) { //최고점수가 현재점수보다 낮을때(최고기록 갱신)
                                firestore.collection("users")
                                        .document(LoginActivity.userId)
                                        .collection("bestRecord")
                                        .document(MainActivity.songName)
                                        .set(record, SetOptions.merge()); // 최초 기록을 업로드
                            } else {
                                // 최고기록보다 점수가 낮으면 아무것도 하지않음.
                            }
                        } else { //추출이 안됐다면(곡에대한 기록이 없다면)
                            firestore.collection("users")
                                    .document(LoginActivity.userId)
                                    .collection("bestRecord")
                                    .document(MainActivity.songName)
                                    .set(record, SetOptions.merge()); // 최초 기록을 업로드
                        }
                    }).addOnFailureListener(e -> {
                                Toast.makeText(ResultActivity.this, "error loading DB", Toast.LENGTH_SHORT).show();
                            }
                    );
        } else {
            automodeTV.setVisibility(View.VISIBLE);
        }
    }  // db의 최고기록과 비교하여 점수를 업데이트해주는 메소드 ( 기록이 없다면 결과기록등록 )

    public static void setRank(double rate, String setRank, TextView rankTV) {
        if (rate >= 99.5) {
            setRank = "SS"; // 결과 랭크를 문자열로 저장
            rankTV.setTextColor(Color.parseColor("#FFE7A3")); //99.5점 이상일시 SS
        } else if (rate >= 97) {
            setRank = "S";
            rankTV.setTextColor(Color.parseColor("#FFE7A3")); //97점 이상일시 S
        } else if (rate >= 90) {
            setRank = "A";
            rankTV.setTextColor(Color.parseColor("#AEFF001F")); //90점 이상일시 A
        } else if (rate >= 80) {
            setRank = "B";
            rankTV.setTextColor(Color.parseColor("#0088FF")); //80점 이상일시 B
        } else if (rate >= 70) {
            setRank = "C";
            rankTV.setTextColor(Color.parseColor("#4DAC25")); //70점 이상일시 C
        } else {
            setRank = "D";
            rankTV.setTextColor(Color.parseColor("#919191")); //70점 미만일시 D
        }

        rankTV.setText(setRank);
    } //정확도,텍스트뷰를 받아 랭크를 반환해주는 메소드
}
