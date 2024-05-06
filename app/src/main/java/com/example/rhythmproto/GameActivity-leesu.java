package com.example.rhythmproto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    String songName;
    String songDifficulty;
    String songBPM;
    int songImage; //곡 이미지
    int song_mp3; //노래 파일


    Button exitbutton;
    Button[] button; // 레인 버튼배열
    public NoteManager noteManager;
    private Thread noteThread; // 노트매니저 Ui Thread를 담을 쓰레드

    ArrayList<NoteData> notes;
    private float judgmentLineY;   //판정선 y축 좌표
    private float judgmentLineY_Rate = 0.80f;  //판정선의 (전체 게임판 * 0.x배) 설정 높이값
    int maxScore = 1000; //퍼펙트 스코어 (기준점)
    private JudgmentLineView judgmentLineView;
    int[] buttonPositions = new int[4]; //각 버튼의 위치
    int[] buttonWidths = new int[4]; //각 버튼의 크기정보
    int[] currentLane = new int[4]; //초기 레인 인덱스  -- 5월6일 10시30분 슬라이딩 구현으로 동시터치시 currentLane의 값 공유로인해 쿼드터치 현상이 발생함, CurrentLane을 배열로 만들어 각 레인별로 CurrentLane을 따로 할당하여 독립적인 currentLane을 사용하도록함.(문제해결완료)
    boolean[] touched = new boolean[4]; // 터치상태를 인식하는 변수

    int score; // 총 점수
    int combo; // 콤보 수
    int perfect; // 퍼펙트 수
    int great;
    int good;  // ~
    int bad;
    int miss; // 미스 수

    TextView scoreTV;  // 점수 텍스트뷰
    TextView judgmentTV;  // 판정 텍스트뷰
    TextView comboTV;  //콤보 텍스트뷰
    TextView accuracyTV; // 정확도 텍스트뷰
    static int stackCombo;  //누적 콤보
    int maxCombo; // 최고 콤보
    double accuracy; // 정확도
    String accuracy_s; // 정확도 텍스트포맷 %.1f% 까지 포맷

    ImageView[] laneLights = new ImageView[4];  // 라인 불빛 이미지뷰

    AnimationController animationController;

    MediaPlayer mediaPlayer; // 노래 플레이어 객체
    float dalay_StartTime; // 노래 시작 시간 +시간은 더 빠르게, -시간은 더 느리게 (2배속 - 1500ms기준 1140)
    static float setSpeed; //받아올 배속세팅값
    float setSpeedJudgment; // 받아올 배율 값
    String color; // 판정 색깔코드

    LottieAnimationView[] animationViews = new LottieAnimationView[4];  // 판정시 폭죽 이펙트효과

    ProgressBar healthBar; // 체력 바

    public List<ValueAnimator> animators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        for (int i = 0; i < NoteManager.lanes.length; i++) {
            NoteManager.lanes[i] = new ArrayList<>();
        }  // 노트매니저의 lanes를 초기화해줌

        for (int i = 0 ; i < currentLane.length; i++) {
            currentLane[i] = -1;
        } // 각 currentLane을 -1로 초기화

        /*---------------------인턴트로 받아올 자료 시작----------------------*/
        Intent intent = getIntent();
        notes = intent.getParcelableArrayListExtra("notes"); // 인턴트로 받아온 노트정보 어레이리스트 notes에 담기
        songName = intent.getStringExtra("songName");
        songDifficulty = intent.getStringExtra("songDifficulty");
        songBPM = intent.getStringExtra("songBPM");
        songImage = intent.getIntExtra("songImage", 1); //곡 정보들을 게임액티비티에 전달.
        song_mp3 = intent.getIntExtra("song_mp3", 1); // 곡 노래mp3를 게임액티비티에 전달.
        setSpeed = intent.getFloatExtra("setSpeed",1.0f); // 배속세팅값을 받아옴
        setSpeedJudgment = intent.getFloatExtra("setSpeedJudgment",1.0f); // 배율값을 받아옴

        /*---------------------인턴트로 받아올 자료 끝----------------------*/

        setdelayStartTime(); // 받아온 배속값 * 판정선비율 -60값을 곡 시작시간으로 설정하는 메소드

        exitbutton = findViewById(R.id.quitGameBtn);
        exitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        button = new Button[]{
                findViewById(R.id.button1),
                findViewById(R.id.button2),
                findViewById(R.id.button3),
                findViewById(R.id.button4)
        };  //버튼배열 초기화

        ViewTreeObserver vto = button[3].getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    button[3].getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    button[3].getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                buttonPositions[0] = (int) button[0].getX();
                buttonWidths[0] = button[0].getWidth();
                buttonPositions[1] = (int) button[1].getX();
                buttonWidths[1] = button[1].getWidth();
                buttonPositions[2] = (int) button[2].getX();
                buttonWidths[2] = button[2].getWidth();
                buttonPositions[3] = (int) button[3].getX();
                buttonWidths[3] = button[3].getWidth();
            }
        }); // 뷰 트리 옵저버를 통해 레이아웃이 구성 된 후에 값을 가지고옴.

        laneLights[0] = findViewById(R.id.lane1_light);
        laneLights[1] = findViewById(R.id.lane2_light);
        laneLights[2] = findViewById(R.id.lane3_light);
        laneLights[3] = findViewById(R.id.lane4_light);

        score = 0;
        judgmentTV = findViewById(R.id.judgmentTV); // 판정 텍스트뷰
        comboTV = findViewById(R.id.comboTV); // 콤보 텍스트뷰
        accuracyTV = findViewById(R.id.accuracyTV); //정확도 테스트뷰
        scoreTV = findViewById(R.id.scoreTV); // 점수 텍스트뷰

        judgmentLineView = findViewById(R.id.judgmentLineView);
        setupJudgmentLine();    // 판정선 메소드
        healthBar = findViewById(R.id.health_Bar);

        animationViews[0] = findViewById(R.id.animation_view1);
        animationViews[0].setAnimation(R.raw.hit);
        animationViews[0].setSpeed(3.0f);

        animationViews[1] = findViewById(R.id.animation_view2);
        animationViews[1].setAnimation(R.raw.hit);
        animationViews[1].setSpeed(3.0f);

        animationViews[2] = findViewById(R.id.animation_view3);
        animationViews[2].setAnimation(R.raw.hit);
        animationViews[2].setSpeed(3.0f);

        animationViews[3] = findViewById(R.id.animation_view4);
        animationViews[3].setAnimation(R.raw.hit);
        animationViews[3].setSpeed(3.0f);

        for (int i = 0; i < button.length; i++) {
            final int index = i;  // Java에서는 lambda expression 내부에서 사용할 변수는 final 이어야 함
            laneButtonTouchListener(button[i], index, laneLights[index], animationViews[index]);
        } // 판정 터치 리스너 레인

        /*laneButtonListener(button1, 1, laneLight1,animationView1);  // 판정처리 리스너 레인1
        laneButtonListener(button2, 2, laneLight2, animationView2);  // 판정처리 리스너 레인2
        laneButtonListener(button3, 3, laneLight3, animationView3);  // 판정처리 리스너 레인3
        laneButtonListener(button4, 4, laneLight4, animationView4);  // 판정처리 리스너 레인4*/

        mediaPlayer = new MediaPlayer();

        startGame(); // 노래를 준비하고, 노래가 준비가 되면 노트를 생성하는 메소드. OnPreParedListener이 사용됨.

        animationController = new AnimationController();  // 애니메이션 컨트롤러 (판정텍스트가 연속실행시 버벅여서 컨트롤러로 실행중이면 끄고 초기화하게 처리)

        // gameHandler.post(gameUpdateRunnable); // 게임핸들러에 쓰레드를 입혀서 동작 - NoteView 의 Animator에서 판정시 리스트에서 삭제하게 바꿈 05/04 23:15분


    }

    private void setdelayStartTime(){
        dalay_StartTime = (setSpeed * judgmentLineY_Rate) - 60;
    }

    public void startGame() {
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(song_mp3);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {  //노래준비 리스너 - 노래가 불러와지길 기다림
                @Override
                public void onPrepared(MediaPlayer mp) {
                    new Handler().postDelayed(() -> {
                        mp.start();
                    }, (int) dalay_StartTime); // 2초후에 노래를 시작하게 하는 메소드 - delay_StartTime은 int 형식의 시작시간 MS 밀리세컨드 단위
                    // 노트 스케줄링 로직
                    ViewGroup layout = findViewById(R.id.noteView); // 노트를 포함할 레이아웃

                    noteManager = new NoteManager(GameActivity.this, layout);
                    preparedNotesInBackGround(); // 파싱데이터로 노트데이터를 실행하는 메소드 &UI쓰레드로 처리하여 부담줄이기& - 5/2 오전3시 이 작업으로 렉이 70%이상 줄어든듯^^!
                    //noteManager.startGame(); // 게임 시작


                    //노트 매니저를 실행하는 레이아웃 리스너, 파싱된 노트데이터로 게임판을 만듬
                    try {
                        afd.close();  // close 호출을 try 블록 안으로 이동
                    } catch (IOException e) {
                        e.printStackTrace();  // 예외 처리
                    }
                }
            }); //노래준비 리스너 - 노래가 불러와지길 기다림
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //노래재생이 끝날때 수행할 코드
                    /*---------------------결과 창 엑티비티로 인턴트 전송 시작---------------------*/
                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                    intent.putExtra("songImage", songImage);  // 곡이미지 인턴트전송
                    intent.putExtra("songName", songName);  // 곡이름 인턴트전송
                    intent.putExtra("songDifficulty", songDifficulty);  // 곡 난이도 인턴트전송
                    intent.putExtra("songBpm", songBPM);  // 곡 bpm 인턴트전송

                    intent.putExtra("perfect", perfect); //퍼펙~ 미스까지 데이터를 인턴트로 전송
                    intent.putExtra("great", great);
                    intent.putExtra("good", good);
                    intent.putExtra("bad", bad);
                    intent.putExtra("miss", miss);

                    intent.putExtra("stackCombo", stackCombo); //스택콤보 인턴트전송
                    intent.putExtra("maxCombo", maxCombo); //맥스콤보 인턴트 전송
                    intent.putExtra("score", score); //점수 인턴트전송
                    intent.putExtra("accuracy", accuracy_s); //정확도 인턴트전송


                    startActivity(intent); //결과 화면으로 이동
                    finish();
                    /*---------------------결과 창 엑티비티로 인턴트 전송 끝---------------------*/
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    } // 노래를 준비하고, 노래가 준비가 되면 노트를 생성하는 메소드. OnPreParedListener이 사용됨.

    private void preparedNotesInBackGround() {
        noteThread = new Thread(() -> {
            runOnUiThread(() -> {
                noteManager.createNotesFromData(notes);
                //Log.d("String", "string");
                //여기서 필요에따라 Thread.sleep(밀리세콘드);
            });
        });
        noteThread.start();
    }  // 파싱데이터로 노트데이터를 실행하는 메소드 &UI쓰레드로 처리하여 부담줄이기& - 5/2 오전3시 이 작업으로 렉이 70%이상 줄어든듯^^!

    public GameActivity() {
    }

    public void checkJudgment(NoteView note, float judgmentLineY, LottieAnimationView animationView) {
        float checkY = (note.getY()) + (note.getHeight() / 2.0f); // 노트의 y위치 - Height / 2 (노트블럭의 중간) 을 더해줌

        if (!note.isJudged()) {  // 판정되지 않은 노트만 처리
            float distance = Math.abs(checkY - judgmentLineY); //노트의 y위치와 판정y위치의 거리차이
            String judgment;  //판정 문자
            int damage;  //데미지 값
            int heal;  //회복 값
            if (distance < JudgmentWindow.BAD * setSpeedJudgment) {
                if (distance <= JudgmentWindow.PERFECT) {
                    heal = 5;
                    judgment = "PERFECT";
                    color = "#FFEB3B";  // 노란색 코드

                    animationHit(animationView); //판정처리시 폭죽 애니메이션 활성화
                    increaseHealth(heal); // 체력증가 메소드
                    comboIncrease(); //콤보수 증가 메소드
                    note.setJudgment("Perfect"); //판정처리
                    updateScore(judgment);  //판정에따라 점수를 추가
                    increaseMaxCombo(combo); // 맥스콤보 로직
                    animationController.startAnimation(judgmentTV, judgment, color);  // 화면중앙에 노트판정 텍스트를 애니메이션 효과로 출력하는 메소드(매개변수는 String 형태)
                    noteManager.removeNoteFromLane(note); // lanes 배열에서 이 객체의 노트뷰를 삭제.
                } else if (distance <= JudgmentWindow.GREAT * setSpeedJudgment) {
                    heal = 3;
                    judgment = "GREAT";
                    color = "#8BC34A";  // 초록색 코드

                    animationHit(animationView);
                    increaseHealth(heal);
                    comboIncrease();
                    note.setJudgment("Great");
                    updateScore(judgment);
                    increaseMaxCombo(combo);
                    animationController.startAnimation(judgmentTV, judgment, color);
                    noteManager.removeNoteFromLane(note); // lanes 배열에서 이 객체의 노트뷰를 삭제.
                } else if (distance <= JudgmentWindow.GOOD * setSpeedJudgment) {
                    heal = 2;
                    judgment = "GOOD";
                    color = "#FF9800";  // 주황색 코드

                    animationHit(animationView);
                    increaseHealth(heal);
                    comboIncrease();
                    note.setJudgment("Good");
                    updateScore(judgment);
                    increaseMaxCombo(combo);
                    animationController.startAnimation(judgmentTV, judgment, color);
                    noteManager.removeNoteFromLane(note); // lanes 배열에서 이 객체의 노트뷰를 삭제.
                } else if (distance <= JudgmentWindow.BAD * setSpeedJudgment) {
                    damage = 5;
                    judgment = "BAD";
                    color = "#606060";  // 회색 코드

                    animationHit(animationView);
                    reduceHealth(damage); //체력감소 메소드
                    comboReset();
                    note.setJudgment("BAD");
                    updateScore(judgment);
                    increaseMaxCombo(combo);
                    animationController.startAnimation(judgmentTV, judgment, color);
                    noteManager.removeNoteFromLane(note); // lanes 배열에서 이 객체의 노트뷰를 삭제.
                }
                note.setJudged(true);
            }
        }
    } // 판정 처리 메소드

    public void animationHit(LottieAnimationView animationViewIndex) {
        animationViewIndex.setVisibility(View.VISIBLE); // 애니메이션 뷰 활성화
        animationViewIndex.playAnimation(); //애니메이션 재생
        animationViewIndex.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationViewIndex.setVisibility(View.GONE);
            }
        });
    } // 애니메이션 뷰 (판정 폭죽효과) 활성 + 비활성 메소드

    private void comboIncrease() {
        combo += 1;
        comboTV.setText("" + combo);
    } // 콤보에 +1후 콤보텍스트뷰에 1을 더하는 메소드

    public void comboReset() {
        combo = 0;
        comboTV.setText("" + combo);
    } // 콤보 리셋후 콤보텍스트뷰에 0을 세팅하는 메소드

    public void updateScore(String judgment) {
        if (judgment == null) return;

        switch (judgment) {
            case "PERFECT":
                score += maxScore; //점수 증가
                stackCombo++; //누적노트 증가
                perfect++; //퍼펙트 수 증가

                break;
            case "GREAT":
                score += maxScore * 0.75;
                stackCombo++;
                great++;
                break;
            case "GOOD":
                score += maxScore * 0.5;
                stackCombo++;
                good++;
                break;
            case "BAD":
                score -= maxScore * 0.25;
                stackCombo++;
                bad++;
                break;
            case "MISS":
                break;
        }
        runOnUiThread(() -> {
            displayScore(score); // 점수를 화면에 표시
            displayAccuracy(score); // 정확도를 화면에 표시
        });

    } // 노트에 판정따른 스코어 +- 메소드

    public void increaseMaxCombo(int combo) {
        if (combo >= maxScore) {
            maxScore = combo;
        }
    }  // 현재콤보가 맥스콤보보다 크거나 같을때 max콤보가 증가하는 메소드

    //       스코어 관련 메소드묶음     +++
    private void displayScore(int score) {
        scoreTV.setText("" + score);
    } // 점수를 ScoreTv에 표시해주는 메소드

    private void displayAccuracy(int score) {
        if (stackCombo != 0) {
            accuracy = (double) score / (stackCombo * maxScore);
            accuracy_s = String.format("%.1f%%", accuracy * 100);
            accuracyTV.setText(accuracy_s);
        }
    }  // 현재 정확도를 실시간으로 화면에 표시해주는 메소드 ( 계산포함 )

    private void setupJudgmentLine() {
        judgmentLineView.post(new Runnable() {
            @Override
            public void run() { // 화면이 생성된후 JudgmentLineView(게임판크기)의 높이를 토대로 생성할것이기 떄문에. post() 메소드를 사용하면 레이아웃이 그려진후 높이를 가져올 수 있게됨.
                judgmentLineY = judgmentLineView.getHeight() * judgmentLineY_Rate;
                judgmentLineView.setLinePosition(judgmentLineY);
            }
        });
    }  // 화면높이의 judgmentLineY_Rate 비율에 판정선 위치를 설정하는 메소드

    public void touchEvent(int index) {
        SoundManager.getInstance().playSound(); // 판정 종소리 효과음
        laneLights[index].setVisibility(View.VISIBLE); // 해당라인 불빛기둥 생성
        new Handler().postDelayed(() -> laneLights[index].setVisibility(View.INVISIBLE), 100); // 해당라인 불빛기둥 0.1초후 끔
        List<NoteView> laneNotes = noteManager.getLaneNotes(index); //NoteManager의 lane1Notes에서 노트1 데이터 받아오기
        NoteView closetNote = findClosetNote(laneNotes);
        if (closetNote != null) {
            checkJudgment(closetNote, judgmentLineY, animationViews[index]); // 원래코드 - 리스트에서 데이터가 삭제 안되는 관계로 일단은 판정을 최대아랫범위보다 줄이고, 최대 아랫범위 내의 값만으로 판정을 처리하게 만듬.
        }
    }   // 버튼객체와, 인덱스로 판정리스너를 삽입해주는 메소드

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

    public void reduceHealth(int damage) {
        int currentHealth = healthBar.getProgress();  // 현재체력 가져오기
        currentHealth -= damage;  // 현재체력에 데미지만큼 깎음

        if (currentHealth < 0) {
            currentHealth = 0; //체력이 0 이하로 내려가지않도록 처리

            Intent intent = new Intent(GameActivity.this, MissionFail.class);
            startActivity(intent);  // 체력이 0이됐으니, 인턴트를 통해 게임오버 화면으로 이동
            finish();
        }

        healthBar.setProgress(currentHealth);
    }  // 체력이 줄어드는 메소드  ++ 체력이 0이하가 되면 게임오버 화면으로 이동

    public void increaseHealth(int heal) {
        int currentHealth = healthBar.getProgress();

        if (currentHealth != healthBar.getMax()) {
            currentHealth += heal;
            if (currentHealth > healthBar.getMax()) currentHealth = healthBar.getMax();
        }

        healthBar.setProgress(currentHealth);
    }  // 체력이 증가하는 메소드

    public void laneButtonTouchListener(Button btn, int index, ImageView laneLightNum, LottieAnimationView animationView) {
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    currentLane[index] = index;
                    touchEvent(index); //인덱스값으로 터치 로직 시작(모든 계산을 실행)
                    return true; // 이벤트를 여기서 종료
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!touched[index]) {   // 최초 터치 1회만 인덱스를 바꾸기위해 touched boolean 변수를 추가.
                        currentLane[index] = index; // 현재라인을 인덱스값으로 변경
                        touched[index] = true;  // 터치상태를 유지
                        Log.d("CHECK","CHECK");
                    }
                    handleTouchMove(index,v, event);
                    return true;
                }  // 터치후 움직였을때

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    currentLane[index] = -1; //레인 인덱스 초기화
                    touched[index] = false;  // 터치를 뗏으니 터치상태를 초기화
                    return true;
                }

                return false;
            }
        });
    } // onTouchListener을 각 버튼에 부여하고, 버튼을 눌렀을때 처리할 터치이벤트 메소드를 실행시킴



    private void handleTouchMove(int index,View v, MotionEvent event) {
        float x = event.getX() + v.getLeft(); // 터치 좌표를 상대적 위치에서 절대적 위치로 변환
        int touchedLane = calculateTouchLane(x);
        if (touchedLane != currentLane[index] && touchedLane >= 0 && touchedLane < animationViews.length) {
            currentLane[index] = touchedLane;
            if (currentLane[index] >= 0 && currentLane[index] < laneLights.length) {
                touchEvent(currentLane[index]); // 새 레인에서 터치 이벤트 처리
            }
        }
    }

    private int calculateTouchLane(float x) {
        for (int i = 0; i < buttonPositions.length; i++) {
            if (x >= buttonPositions[i] && x <= buttonPositions[i] + buttonWidths[i]) {
                return i;
            }
        }
        return -1; // 터치된 레인이 없는경우
    } // 버튼들의 좌표값으로 현재 터치된 x위치와 비교하여 무슨버튼인지 식별하게해주는 메소드(int i 값을 반환)

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (noteThread != null) {
            noteThread.interrupt(); // 쓰레드 멈추기
            noteThread = null;  // 쓰레드 참조를 해제하여 가비지 컬렉션을 도울 수 있도록 함
        }

        for (ValueAnimator animator : animators) {
            if (animator != null) {
                animator.cancel();  // 애니메이터 작업 취소(쓰레드는 꺼져도 애니메이터는 계속 돌아가고있으므로 멈춰줘야 없어진객체에 Miss처리를 하지않음)
            }
        }

        if (noteManager.handler != null) {
            noteManager.shutdownHandler(); //노트매니저의 노트생성 스케줄과 콜백 제거메소드
        }

        if (NoteManager.lanes != null) {
            NoteManager.lanes = null;   //노트매니저의 노트리스트 배열을 초기화
            NoteManager.lanes = new List[5];  //그대로두면 NullPointException이 뜨니 배열크기를 다시 지정해줌 (onCreate에서 초기화해도 됨)
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;  //노래 끄기
        }

        stackCombo = 0;

        /*if (gameHandler != null) {
            gameHandler.removeCallbacksAndMessages(null); // NoteView 의 Animator에서 판정시 리스트에서 삭제하게 바꿈 05/04 23:15분 판정노트 검사후 삭제하는 핸들러
        }*/

    } // 뷰가 꺼질때 노래,종소리도 같이 null로 초기화


    /*public void laneButtonListener(Button btn, int index, ImageView laneLightNum, LottieAnimationView animationView) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchEvent(index, laneLightNum, animationView);  // 터치했을때 작동할 모든 메소드들
            }
        });
    }     // 컴퓨터 시뮬레이팅용 onCLickListener

     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A:
                touchEvent(0); //인덱스값으로 터치 로직 시작(모든 계산을 실행)
                return true;
            case KeyEvent.KEYCODE_S:
                touchEvent(1);;
                return true;
            case KeyEvent.KEYCODE_SEMICOLON:
                touchEvent(2);
                return true;
            case KeyEvent.KEYCODE_APOSTROPHE:
                touchEvent(3);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    } //키보드 지원을 위한 키보드 A,S,;,' 키를 1,2,3,4번 버튼을 눌러주게하는 메소드



    /*
    private Runnable gameUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateAllNotes();
            gameHandler.postDelayed(this, 50); // 예를 들어, 16ms는 (약 60FPS)
        }
    }; //게임 핸들러 스레드 - 처리됐거나 놓친노트블럭을 삭제하는 메소드

    public void updateAllNotes() {
        for (List<NoteView> lane : NoteManager.lanes) {
            Iterator<NoteView> iterator = lane.iterator();
            while (iterator.hasNext()) {
                NoteView note = iterator.next();
                // 여기에서 노트의 상태를 로그로 출력
                float checkY = (note.getY()) + (note.getHeight() / 2.0f); // 노트의 y위치 - Height / 2 (노트블럭의 중간) 을 더해줌
                if (note.isJudged()) {
                    iterator.remove();
                }
            }
        }
    } //처리됐거나 화면을 벗어난 노트는 리스트에서 제거하는 메소드 -- NoteView 의 Animator에서 판정시 리스트에서 삭제하게 바꿈 05/04 23:15분 */
}