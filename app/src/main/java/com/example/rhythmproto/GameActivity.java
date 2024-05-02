package com.example.rhythmproto;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    Button exitbutton;
    Button button1, button2, button3, button4, button5;
    private NoteManager noteManager;
    private Handler gameHandler = new Handler(); // 게임 핸들러, 실시간 처리를위해 사용

    ArrayList<NoteData> notes;
    private float judgmentLineY;   //판정선 y축 좌표
    private JudgmentLineView judgmentLineView;

    int score; // 총 점수

    int combo; // 콤보 수

    TextView scoreTV;
    TextView judgmentTV;
    TextView comboTV;
    AnimationController animationController;
    private List<NoteView>[] lanes = new List[5];  // 5개의 레인을 위한 배열
    private Runnable gameUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateAllNotes();
            gameHandler.postDelayed(this, 16); // 예를 들어, 16ms는 (약 60FPS)
        }
    }; //게임 핸들러 스레드
    MediaPlayer mediaPlayer; // 노래 플레이어 객체
    int dalay_StartTime = 950; // 노래 시작 시간 +시간은 더 빠르게, -시간은 더 느리게 (통상적으로 배속 * 판정선배율 - 170하면 맞음)
    String color; // 판정 색깔코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        Intent intent = getIntent();
        notes = intent.getParcelableArrayListExtra("notes");

        exitbutton = findViewById(R.id.quitGameBtn);
        exitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);

        score = 0;
        judgmentTV = findViewById(R.id.judgmentTV); // 판정텍스트뷰
        comboTV = findViewById(R.id.comboTV); // 콤보텍스트뷰

        judgmentLineView = findViewById(R.id.judgmentLineView);
        setupJudgmentLine();    // 판정선 메소드

        laneButtonListener(button1, 1);  // 판정처리 리스너 레인1
        laneButtonListener(button2, 2);  // 판정처리 리스너 레인2
        laneButtonListener(button3, 3);  // 판정처리 리스너 레인3
        laneButtonListener(button4, 4);  // 판정처리 리스너 레인4
        laneButtonListener(button5, 5);  // 판정처리 리스너 레인5

        mediaPlayer = new MediaPlayer();

        startGame(); // 노래를 준비하고, 노래가 준비가 되면 노트를 생성하는 메소드. OnPreParedListener이 사용됨.

        animationController = new AnimationController();  // 애니메이션 컨트롤러 (판정텍스트가 연속실행시 버벅여서 컨트롤러로 실행중이면 끄고 초기화하게 처리)

        gameHandler.post(gameUpdateRunnable); // 게임핸들러에 쓰레드를 입혀서 동작
    }

    public void startGame() {
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.xeon);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    new Handler().postDelayed(() -> {
                        mp.start();
                    }, dalay_StartTime); // 2초후에 노래를 시작하게 하는 메소드 - delay_StartTime은 int 형식의 시작시간 MS 밀리세컨드 단위
                    // 노트 스케줄링 로직
                    ViewGroup layout = findViewById(R.id.noteView); // 노트를 포함할 레이아웃
                    layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (layout.getViewTreeObserver().isAlive()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                } else {
                                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                }
                            }
                            noteManager = new NoteManager(GameActivity.this, layout);
                            preparedNotesInBackGround(); // 파싱데이터로 노트데이터를 실행하는 메소드 &UI쓰레드로 처리하여 부담줄이기& - 5/2 오전3시 이 작업으로 렉이 70%이상 줄어든듯^^!
                            //noteManager.startGame(); // 게임 시작
                        }
                    });
                    //노트 매니저를 실행하는 레이아웃 리스너, 파싱된 노트데이터로 게임판을 만듬
                    try {
                        afd.close();  // close 호출을 try 블록 안으로 이동
                    } catch (IOException e) {
                        e.printStackTrace();  // 예외 처리
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // 노래를 준비하고, 노래가 준비가 되면 노트를 생성하는 메소드. OnPreParedListener이 사용됨.


    private void preparedNotesInBackGround() {
        new Thread(() -> {
            runOnUiThread(() -> {
                noteManager.createNotesFromData(notes);
            });
        }).start();
    }  // 파싱데이터로 노트데이터를 실행하는 메소드 &UI쓰레드로 처리하여 부담줄이기& - 5/2 오전3시 이 작업으로 렉이 70%이상 줄어든듯^^!

    public GameActivity() {
        for (int i = 0; i < lanes.length; i++) {
            lanes[i] = new ArrayList<>();
        }
    }

    public void updateAllNotes() {
        for (List<NoteView> lane : lanes) {
            Iterator<NoteView> iterator = lane.iterator();
            while (iterator.hasNext()) {
                NoteView note = iterator.next();
                if (note.isOffScreen() || note.isJudged()) {
                    iterator.remove();
                }
            }
        }
    } //처리됐거나 화면을 벗어난 노트는 리스트에서 제거하는 메소드

    public void checkJudgment(NoteView note, float judgmentLineY) {
        float checkY = (note.getY()) + (note.getHeight()/2.0f); // 노트의 y위치 - Height / 2 (노트블럭의 중간) 을 더해줌

        if (!note.isJudged()) {  // 판정되지 않은 노트만 처리
            float distance = Math.abs(checkY - judgmentLineY); //노트의 y위치와 판정y위치의 거리차이
            String judgment;
            if (distance < JudgmentWindow.BAD) {
                if (distance <= JudgmentWindow.PERFECT) {
                    comboIncrease(); //콤보수 증가 메소드
                    note.setJudgment("Perfect"); //판정처리
                    judgment = "PERFECT";
                    color = "#FFEB3B";  // 노란색 코드
                    updateScore(judgment); //판정에따라 점수를 추가
                    animationController.startAnimation(judgmentTV, judgment, color);  // 화면중앙에 노트판정 텍스트를 애니메이션 효과로 출력하는 메소드(매개변수는 String 형태)
                } else if (distance <= JudgmentWindow.GREAT) {
                    comboIncrease();
                    note.setJudgment("Great");
                    judgment = "GREAT";
                    color = "#8BC34A";  // 초록색 코드
                    updateScore(judgment);
                    animationController.startAnimation(judgmentTV, judgment, color);
                } else if (distance <= JudgmentWindow.GOOD) {
                    comboIncrease();
                    note.setJudgment("Good");
                    judgment = "GOOD";
                    color = "#FF9800";  // 주황색 코드
                    updateScore(judgment);
                    animationController.startAnimation(judgmentTV, judgment, color);
                } else if (distance <= JudgmentWindow.BAD) {
                    comboReset();
                    note.setJudgment("BAD");
                    judgment = "BAD";
                    color = "#606060";  // 회색 코드
                    updateScore(judgment);
                    animationController.startAnimation(judgmentTV, judgment, color);
                }
                note.setJudged(true);
            }
        }
    } // 판정 처리 메소드

    private void comboIncrease() {
        combo += 1;
        comboTV.setText("" + combo);
    } // 콤보에 +1후 콤보텍스트뷰에 1을 더하는 메소드

    private void comboReset() {
        combo = 0;
        comboTV.setText("" + combo);
    } // 콤보 리셋후 콤보텍스트뷰에 0을 세팅하는 메소드

    public void updateScore(String judgment) {
        if (judgment == null) return;

        switch (judgment) {
            case "PERFECT":
                score += 1000;
                break;
            case "GREAT":
                score += 700;
                break;
            case "GOOD":
                score += 300;
                break;
            case "BAD":
                score -= 500;
                break;
        }
        runOnUiThread(() -> displayScore(score)); // 점수를 화면에 표시
    } // 노트에 판정따른 스코어 +- 메소드

    //       스코어 관련 메소드묶음     +++
    private void displayScore(int score) {
        scoreTV = findViewById(R.id.scoreTV);
        scoreTV.setText("" + score);
    } // 점수를 ScoreTv에 표시해주는 메소드

    private void setupJudgmentLine() {
        judgmentLineView.post(new Runnable() {
            @Override
            public void run() { // 화면이 생성된후 JudgmentLineView(게임판크기)의 높이를 토대로 생성할것이기 떄문에. post() 메소드를 사용하면 레이아웃이 그려진후 높이를 가져올 수 있게됨.
                judgmentLineY = judgmentLineView.getHeight() * 0.74f;
                judgmentLineView.setLinePosition(judgmentLineY);
            }
        });
    }  // 화면높이의 0.9비율에 판정선 위치 설정하는 메소드

    public void laneButtonListener(Button btn, int index) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SoundManager.getInstance().playSound(); // 판정 종소리 효과음
                List<NoteView> laneNotes = noteManager.getLaneNotes(index); //NoteManager의 lane1Notes에서 노트1 데이터 받아오기
                NoteView closetNote = findClosetNote(laneNotes);
                if (closetNote != null) {
                    checkJudgment(closetNote, judgmentLineY); // 원래코드 - 리스트에서 데이터가 삭제 안되는 관계로 일단은 판정을 최대아랫범위보다 줄이고, 최대 아랫범위 내의 값만으로 판정을 처리하게 만듬. 05/02 새벽1시3분
                    //checkJudgment(laneNotes, judgmentLineY); // 판정체크
                    //updateScore(closetNote.getJudgment()); //점수 업데이트 - 이것또한 판정이후에 처리하는게 훨씬 깔끔해서 GameActivity - checkJudgment() 메소드 안에서 판정에따라 처리하게 변경.
                }
            }
        });
    }     // 버튼객체와, 인덱스로 판정리스너를 삽입해주는 메소드

    private NoteView findClosetNote(List<NoteView> notes) {
        NoteView closet = null;
        float minDistance = Float.MAX_VALUE;

        for (NoteView note : notes) {
            float distance = Math.abs(note.getY() - judgmentLineY);
            if (distance < minDistance) {    //노트가 존재하는지 확인하는 메소드
                minDistance = distance;
                closet = note;
            }
        }
        return closet;
    }  //가장 가까운 노트를 찾아주는 NoteView메소드

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        gameHandler.removeCallbacksAndMessages(null);
    } // 뷰가 꺼질때 노래,종소리도 같이 null로 초기화

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A:
                button1.performClick();
                return true;
            case KeyEvent.KEYCODE_S:
                button2.performClick();
                return true;
            case KeyEvent.KEYCODE_SEMICOLON:
                button3.performClick();
                return true;
            case KeyEvent.KEYCODE_APOSTROPHE:
                button4.performClick();
                return true;
            /*case KeyEvent.KEYCODE_M:
                button5.performClick();
                return true;*/
        }
        return super.onKeyDown(keyCode, event);
    } //키보드 지원을 위한 키보드 A,S,;,' 키를 1,2,3,4번 버튼을 눌러주게하는 메소드
}