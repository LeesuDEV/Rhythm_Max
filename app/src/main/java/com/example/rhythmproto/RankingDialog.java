package com.example.rhythmproto;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RankingDialog extends Dialog {
    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조
    Context context;
    TextView userNameTV, userScoreTV, myRankTV;
    LinearLayout rankingNumLayout, rankingNameLayout, rankingScoreLayout, rankingAccuracyLayout;
    TextView[] textViewNum, textViewName, textViewScore, textViewAccuracy;
    ImageView selectSongImageView;
    TextView songNameTV, difficultyTV;
    TextView rankingExitBtn;

    public RankingDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranking_dialog);

        userNameTV = findViewById(R.id.userNameTV);
        userScoreTV = findViewById(R.id.userScoreTV);
        myRankTV = findViewById(R.id.myRankTV);
        rankingNumLayout = findViewById(R.id.rankingNumLayout);
        rankingNameLayout = findViewById(R.id.rankingNameLayout);
        rankingScoreLayout = findViewById(R.id.rankingScoreLayout);
        rankingAccuracyLayout = findViewById(R.id.rankingAccuracyLayout);

        selectSongImageView = findViewById(R.id.selectSongImageView);
        songNameTV = findViewById(R.id.songNameTV);
        difficultyTV = findViewById(R.id.difficultyTV);

        rankingExitBtn = findViewById(R.id.rankingExitBtn);

        rankingExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        selectSongImageView.setImageResource(MainActivity.songImage);
        songNameTV.setText(MainActivity.songName);

        if (MainActivity.difficulty == 1) { //이지모드 별색깔 삽입
            MainActivity.easyModDifficultyColor(MainActivity.songDifficulty, difficultyTV);
        } else if (MainActivity.difficulty == 2) { //하드모드 별색깔 삽입
            MainActivity.hardModDifficultyColor(MainActivity.songDifficulty, difficultyTV);
        }

        userNameTV.setText(LoginActivity.userName); // 로그인정보 토대로 내 랭킹정보 닉네임 표시

        firestore.collection("ranking").document(MainActivity.songName) // 선택된곡 랭킹정보 불러오기
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) { //문서가 추출됐다면(데이터가 있음을 의미)
                            Map<String, Object> data = documentSnapshot.getData(); // Map에 데이터수령
                            List<UserScore> userScores = new ArrayList<>(); // 정렬을위한 UserScore데이터모델의 리스트 선언

                            for (Map.Entry<String, Object> entry : data.entrySet()) { // 추출데이터에서 한 객체씩 받기
                                String userName = entry.getKey(); // 유저이름
                                String value = (String) entry.getValue();
                                String[] parts = value.split(";"); // 세미콜론을 기점으로 데이터분리
                                int score = Integer.parseInt(parts[0]);
                                String accuracy = parts[1];
                                userScores.add(new UserScore(userName, score, accuracy));
                            }

                            // 점수 기준으로 내림차순 정렬
                            Collections.sort(userScores, new Comparator<UserScore>() {
                                @Override
                                public int compare(UserScore o1, UserScore o2) {
                                    return Integer.compare(o2.getScore(), o1.getScore());
                                }
                            });

                            //리팩토링 or Dart언어로 재구현시 함수로 뺄것(필수)

                            Typeface typeFace_m = ResourcesCompat.getFont(context, R.font.netmarble_m); //텍스트 타입
                            Typeface typeFace_b = ResourcesCompat.getFont(context, R.font.netmarble_b); //텍스트 타입
                            textViewNum = new TextView[userScores.size()];
                            textViewName = new TextView[userScores.size()];  // 순위,이름,점수,정확도 텍스트뷰 선언
                            textViewScore = new TextView[userScores.size()];
                            textViewAccuracy = new TextView[userScores.size()];
                            int height = Math.round(25 * getContext().getResources().getDisplayMetrics().density); // 25dp값을 화면비율에서 픽셀값추출

                            for (int j = 0; j < userScores.size(); j++) {
                                if (userScores.get(j).getUserName().equals(LoginActivity.userName)) { //유저의 이름과 일치하는 정보가 있는지 확인
                                    switch (j) {
                                        case 0:
                                            myRankTV.setTextColor(Color.parseColor("#F6D764")); //금색
                                            break;
                                        case 1:
                                            myRankTV.setTextColor(Color.parseColor("#A3A3A3")); //은색
                                            break;
                                        case 2:
                                            myRankTV.setTextColor(Color.parseColor("#CD7F32")); //동색
                                            break;
                                        default:
                                            myRankTV.setTextColor(Color.WHITE); // 아닐시 화이트
                                            break;
                                    }
                                    myRankTV.setText("" + (j + 1)); //일치하는 정보의 순위를 입력(인자값을 가지고 추측가능)
                                    userScoreTV.setText("" + userScores.get(j).getScore()); // 해당하는 정보의 점수값을 추출
                                }
                            }

                            for (int i = 0; i < userScores.size(); i++) {

                                textViewNum[i] = new TextView(context);
                                textViewNum[i].setText("" + (i + 1)); //랭킹을 표시
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                layoutParams.height = height;
                                layoutParams.setMargins(0, 10, 0, 10);
                                textViewNum[i].setLayoutParams(layoutParams);
                                switch (i) {
                                    case 0:
                                        textViewNum[i].setTextColor(Color.parseColor("#F6D764")); //금색
                                        textViewNum[i].setTextSize(19);
                                        textViewNum[i].setTypeface(typeFace_b); // 볼드체
                                        break;
                                    case 1:
                                        textViewNum[i].setTextColor(Color.parseColor("#A3A3A3")); //은색
                                        textViewNum[i].setTextSize(19);
                                        textViewNum[i].setTypeface(typeFace_b); // 볼드체
                                        break;
                                    case 2:
                                        textViewNum[i].setTextColor(Color.parseColor("#CD7F32")); //동색
                                        textViewNum[i].setTextSize(19);
                                        textViewNum[i].setTypeface(typeFace_b); // 볼드체
                                        break;
                                    default:
                                        textViewNum[i].setTextColor(Color.WHITE); // 아닐시 화이트
                                        textViewNum[i].setTypeface(typeFace_m); // 아닐시 netmarble_m
                                        textViewNum[i].setTextSize(17);
                                        break;
                                }
                                rankingNumLayout.addView(textViewNum[i]);

                                textViewName[i] = new TextView(context);
                                textViewName[i].setText(userScores.get(i).getUserName()); //해당랭킹의 유저이름
                                textViewName[i].setGravity(View.TEXT_ALIGNMENT_CENTER);
                                textViewName[i].setLayoutParams(layoutParams);
                                textViewName[i].setTextColor(Color.WHITE);
                                switch (i) {
                                    case 0:
                                        textViewName[i].setTypeface(typeFace_b); // 볼드체
                                        textViewName[i].setTextSize(17);
                                        break;
                                    case 1:
                                        textViewName[i].setTypeface(typeFace_b); // 볼드체
                                        textViewName[i].setTextSize(17);
                                        break;
                                    case 2:
                                        textViewName[i].setTypeface(typeFace_b); // 볼드체
                                        textViewName[i].setTextSize(17);
                                        break;
                                    default:
                                        textViewName[i].setTypeface(typeFace_m); // 아닐시 netmarble_m
                                        textViewName[i].setTextSize(15);
                                        break;
                                }
                                rankingNameLayout.addView(textViewName[i]);

                                textViewScore[i] = new TextView(context);
                                textViewScore[i].setText("" + userScores.get(i).getScore()); //해당랭킹의 유저스코어

                                switch (i) {
                                    case 0:
                                        textViewScore[i].setTypeface(typeFace_b); // 볼드체
                                        textViewScore[i].setTextSize(17);
                                        break;
                                    case 1:
                                        textViewScore[i].setTypeface(typeFace_b); // 볼드체
                                        textViewScore[i].setTextSize(17);
                                        break;
                                    case 2:
                                        textViewScore[i].setTypeface(typeFace_b); // 볼드체
                                        textViewScore[i].setTextSize(17);
                                        break;
                                    default:
                                        textViewScore[i].setTypeface(typeFace_m); // 아닐시 netmarble_m
                                        textViewScore[i].setTextSize(15);
                                        break;
                                }
                                textViewScore[i].setLayoutParams(layoutParams);
                                textViewScore[i].setTextColor(Color.WHITE);
                                rankingScoreLayout.addView(textViewScore[i]);

                                textViewAccuracy[i] = new TextView(context);
                                textViewAccuracy[i].setText("" + userScores.get(i).getAccuracy()); //해당랭킹의 정확도
                                switch (i) {
                                    case 0:
                                        textViewAccuracy[i].setTypeface(typeFace_b); // 볼드체
                                        textViewAccuracy[i].setTextSize(17);
                                        break;
                                    case 1:
                                        textViewAccuracy[i].setTypeface(typeFace_b); // 볼드체
                                        textViewAccuracy[i].setTextSize(17);
                                        break;
                                    case 2:
                                        textViewAccuracy[i].setTypeface(typeFace_b); // 볼드체
                                        textViewAccuracy[i].setTextSize(17);
                                        break;
                                    default:
                                        textViewAccuracy[i].setTypeface(typeFace_m); // 아닐시 netmarble_m
                                        textViewAccuracy[i].setTextSize(15);
                                        break;
                                }
                                textViewAccuracy[i].setLayoutParams(layoutParams);
                                textViewAccuracy[i].setTextColor(Color.WHITE);
                                rankingAccuracyLayout.addView(textViewAccuracy[i]);
                            }


                        }
                    }
                });

    }
}
