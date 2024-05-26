package com.example.rhythmproto;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

                            Typeface typeFace = ResourcesCompat.getFont(context, R.font.netmarble_b);
                            textViewNum = new TextView[userScores.size()];
                            textViewName = new TextView[userScores.size()];
                            textViewScore = new TextView[userScores.size()];
                            textViewAccuracy = new TextView[userScores.size()];
                            int height = Math.round(25 * getContext().getResources().getDisplayMetrics().density); // 25dp값을 화면비율에서 픽셀값추출

                            for (int j = 0; j < userScores.size(); j++) {
                                if (userScores.get(j).getUserName().equals(LoginActivity.userName)) { //유저의 이름과 일치하는 정보가 있는지 확인
                                    myRankTV.setText("" + (j + 1)); //일치하는 정보의 순위를 입력(인자값을 가지고 추측가능)
                                    userScoreTV.setText("" + userScores.get(j).getScore()); // 해당하는 정보의 점수값을 추출
                                }
                            }

                            for (int i = 0; i < userScores.size(); i++) {

                                textViewNum[i] = new TextView(context);
                                textViewNum[i].setText("" + (i + 1)); //랭킹을 표시
                                textViewNum[i].setTextSize(20);
                                textViewNum[i].setTypeface(typeFace);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                layoutParams.height = height;
                                layoutParams.setMargins(0, 10, 0, 10);
                                textViewNum[i].setLayoutParams(layoutParams);
                                textViewNum[i].setTextColor(Color.WHITE);
                                rankingNumLayout.addView(textViewNum[i]);

                                textViewName[i] = new TextView(context);
                                textViewName[i].setText(userScores.get(i).getUserName()); //해당랭킹의 유저이름
                                textViewName[i].setTextSize(20);
                                textViewName[i].setTypeface(typeFace);
                                textViewName[i].setLayoutParams(layoutParams);
                                textViewName[i].setTextColor(Color.WHITE);
                                rankingNameLayout.addView(textViewName[i]);

                                textViewScore[i] = new TextView(context);
                                textViewScore[i].setText("" + userScores.get(i).getScore()); //해당랭킹의 유저스코어
                                textViewScore[i].setTextSize(20);
                                textViewScore[i].setTypeface(typeFace);
                                textViewScore[i].setLayoutParams(layoutParams);
                                textViewScore[i].setTextColor(Color.WHITE);
                                rankingScoreLayout.addView(textViewScore[i]);

                                textViewAccuracy[i] = new TextView(context);
                                textViewAccuracy[i].setText("" + userScores.get(i).getAccuracy()); //해당랭킹의 정확도
                                textViewAccuracy[i].setTextSize(20);
                                textViewAccuracy[i].setTypeface(typeFace);
                                textViewAccuracy[i].setLayoutParams(layoutParams);
                                textViewAccuracy[i].setTextColor(Color.WHITE);
                                rankingAccuracyLayout.addView(textViewAccuracy[i]);
                            }


                        }
                    }
                });

    }
}
