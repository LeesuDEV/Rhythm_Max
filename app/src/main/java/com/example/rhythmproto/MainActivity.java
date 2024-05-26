package com.example.rhythmproto;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager;
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    CarouselRecyclerview carouselRecyclerview; //캐러셀뷰(3d갤러리)

    static String songName;
    static String songDifficulty;
    static String songBPM;
    static int songImage; //곡 이미지
    static int song_mp3; //노래 파일
    static int noteData; //채보데이터
    static int songMV; //곡 영상데이터
    String selectSong = ""; // 선택된 곡
    Button syncSetBtn; // 싱크조절 버튼
    Button bluetoothBtn; // 블루투스 이어폰 자동세팅버튼

    ImageView settingBtn; //세팅 다이어로그 버튼

    TextView easyBtn;  //이지모드 버튼
    TextView hardBtn;  //하드모드 버튼
    EditText syncSetText; // 싱크조절 에딧텍스트
    TextView syncCurrentText; // 싱크현재값 텍스트
    TextView uesrNameTV; //유저이름 텍스트뷰
    TextView song_name_Main; // 현재 선택된노래 이름 텍스트
    TextView song_difficulty_Main; // 현재 선택된노래 난이도 텍스트
    TextView rankingBtn; //랭킹버튼
    static int syncValue = 0; // 싱크값
    static boolean autoModIndex = false; // 0ff 기본값 오토모드 인덱스
    static int gameModIndex = 0; // 모드인덱스값 0은 NORMAL , 1은 MIRROR , 2는 RANDOM

    List<ImageItem> items; // 곡메인화면 어레이리스트

    static List<NoteData> notes; //노트배열(파싱데이터 기반)

    static int difficulty; //메인화면 난이도 인자값 1= 이지모드 2=하드모드
    int current_song = -1; //현재 선택된 캐러셀노래 인자값

    static MediaPlayer mediaPlayer; // 미리보기노래 미디어플레이어

    static float setSpeed = 1500; // 배속설정
    static int speedIndex = 3; // 배속모드 식별값
    static float setSpeedJudgment = 3; // 배속모드에 따른 판정 배율값

    static float previewSoundAmountIndex = 1.0f; // 배경음악 크기 인덱스값
    static float ingameSoundAmountIndex = 1.0f; // 인게임음악 크기 인덱스값

    LottieAnimationView musicController;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //1배속 = 3000ms , 1.5배속 = 2000ms , 2배속 = 1500ms , 2.5배속 = 1200ms, 3배속 = 1000ms, 3.5배속 = 857ms
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easyBtn = findViewById(R.id.setEasyBtn);
        hardBtn = findViewById(R.id.setHardBtn);
        settingBtn = findViewById(R.id.settingIcon);
        uesrNameTV = findViewById(R.id.userNameTV);

        /*---------------------------------캐러셀 레이아웃 -----------------------------------*/
        carouselRecyclerview = findViewById(R.id.recyclerView); //캐러셀 뷰
        song_name_Main = findViewById(R.id.songNameMainTV); //곡이름 메인타이틀
        song_difficulty_Main = findViewById(R.id.songDifficultyMainTV); //곡난이도 메인타이틀
        rankingBtn = findViewById(R.id.rankingBtn); //랭킹버튼

        if (LoginActivity.userName != null) {
            uesrNameTV.setText(LoginActivity.userName + " 님"); //유저네임 텍스트뷰에 닉네임 표시
        } else {
            uesrNameTV.setText("Empt 님"); //예기치못한 닉네임관련 이슈시 Empty로 설정
        } // 닉네임 상단 텍스트뷰 설정

        CarouselLayoutManager layoutManager = new CarouselLayoutManager(
                false,
                true,
                1,
                false,
                true,
                true,
                LinearLayoutManager.HORIZONTAL);
        carouselRecyclerview.setLayoutManager(layoutManager);

        setEasyModAdapter(); //이지모드 어댑터 초기설정

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playPreview(0); //메인화면 limbo 초기 플레이
            }
        }, 1000);

        musicController = findViewById(R.id.musicController);
        musicController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        musicController.pauseAnimation();
                    } else {
                        mediaPlayer.start();
                        musicController.resumeAnimation();
                    }
                }
            }
        });
        carouselRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateCenterItem_Descripttion(recyclerView, items); // 리사이클뷰와 아이템리스트를 넘겨 -> 중간위치에 걸친 자손뷰를 탐색
            }
        }); //캐러셀리사이클뷰를 슬라이드할때마다 아이템을 찾는 리스너

        carouselRecyclerview.setItemSelectListener(new CarouselLayoutManager.OnSelected() {
            @Override
            public void onItemSelected(int i) {
            }
        }); //캐러셀리사이클뷰 화면중앙에 아이템이 선택된게 확정됐을때 작업을 수행하는 리스너

        if (!items.isEmpty()) { // 아이템리스트가 비지않았다면
            song_name_Main.setText(items.get(0).getDescription()); //0번째 곡리스트 설명을 텍스트뷰에 설정
        }
        /*---------------------------------캐러셀 레이아웃 -----------------------------------*/

        startDifficultySet(); //화면구성시 난이도 설정 (이지)
        easyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (difficulty != 1) { //난이도가 어려움일때만 메소드실행
                    difficultyChangeText(easyBtn, 1.0f, 1.3f);
                    easyBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    difficultyChangeText(hardBtn, 1.3f, 1.0f);
                    hardBtn.setTextColor(Color.parseColor("#787878"));
                    difficulty = 1; //난이도 이지로 설정

                    setEasyModAdapter(); //이지모드 어댑터 설정
                }
            }
        }); //이지버튼 클릭시 버튼크기와 색상조절후 아이템리스트를 교체하고 어댑터를 다시 끼우는 리스너
        hardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (difficulty != 2) {
                    difficultyChangeText(hardBtn, 1.0f, 1.3f);
                    hardBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    difficultyChangeText(easyBtn, 1.3f, 1.0f);
                    easyBtn.setTextColor(Color.parseColor("#787878"));
                    difficulty = 2;

                    setHardModAdapter(); //하드모드 어댑터 설정
                }
            }
        }); //하드버튼 클릭시 버튼크기와 색상조절후 아이템리스트를 교체하고 어댑터를 다시 끼우는 리스너

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingCustomDialog(); //세팅 다이어로그 호출
            }
        });

    }

    public void playPreview(int index) {
        if (index != current_song) { // 인자값이 현재 선택된곡 인자값과 똑같지 않을때만 실행
            if (mediaPlayer != null) { //미디어플레이어 객체가 비지않고
                if (mediaPlayer.isPlaying()) { // 플레이중이라면
                    mediaPlayer.stop(); // 플레이어를 멈춤
                }
                mediaPlayer.release(); // 재생중이던곡 제거
                mediaPlayer = null; //널값을 삽입해 안전하게 비우기
            }

            switch (index) {
                case 0:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.cannon_main);
                    //캐러셀뷰가 선택한 인덱스가 1번 인덱스라면 림보 미리듣기 플레이
                    break;
                case 1:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.shining_light_main);
                    break;
                case 2:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.xeus_main);
                    break;
                case 3:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.odysseus_main);
                    break;
                case 4:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.limbo_main);
                    break;
                case 5:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.skyhigh_main);
                    break;

            }

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true); // 반복재생
                mediaPlayer.start(); // 미디어플레이어 스타트
                mediaPlayer.setVolume(previewSoundAmountIndex, previewSoundAmountIndex); // 전역변수 프리뷰음악볼륨을 설정
            }

            current_song = index; // 현재선택된곡 인자값을 i인자값으로 설정 ( 중복확인 )

            if (!musicController.isAnimating()) {
                musicController.resumeAnimation();
            } // 프리뷰 뮤직컨트롤러가 멈춰져있는 상태라면 노래를 다시킬떄 뮤직컨트롤러도 다시 킴. (Lottie Animation)
        }

        rankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRankingDialog();
            }
        }); //랭킹 다이어로그 출력

    } // 인덱스값을 토대로 미디어플레이어를 미리보기 재생시켜주는 메소드

    public void setEasyModAdapter() {
        items = null; // 곡메인화면 리스트초기화
        items = new ArrayList<>(); // 곡 메인화면 어레이리스트 재생성
        items.add(new ImageItem(R.drawable.cannon_main_img, "Cannon_Remix", "★★"));
        items.add(new ImageItem(R.drawable.shining_light_main_img, "Shining_Light", "★★★"));
        items.add(new ImageItem(R.drawable.xeus_main_img, "Xeus", "★★★★★★"));
        items.add(new ImageItem(R.drawable.odysseus_main_image, "Odysseus", "★★★★★"));
        items.add(new ImageItem(R.drawable.limbo_main_image, "Limbo", "★★★★★★★★★"));
        items.add(new ImageItem(R.drawable.skyhing_main_img, "SkyHigh", "★★★★★★★★"));

        MyAdapter adapter = new MyAdapter(items, MainActivity.this); //어댑터에 이미지 리스트를 매개변수로 생성
        carouselRecyclerview.setAdapter(adapter);
    } //이지모드 캐러셀뷰어댑터

    public void setHardModAdapter() {
        items = null; // 곡메인화면 리스트초기화
        items = new ArrayList<>(); // 곡 이미지 어레이리스트 재생성
        items.add(new ImageItem(R.drawable.cannon_main_img, "Cannon_Remix", "★★★"));
        items.add(new ImageItem(R.drawable.shining_light_main_img, "Shining_Light", "★★★★"));
        items.add(new ImageItem(R.drawable.xeus_main_img, "Xeus", "★★★★★"));
        items.add(new ImageItem(R.drawable.odysseus_main_image, "Odysseus", "★★★★★★★★"));
        items.add(new ImageItem(R.drawable.limbo_main_image, "Limbo", "★★★★★★★★★"));
        items.add(new ImageItem(R.drawable.skyhing_main_img, "SkyHigh", "★★★★★★★"));

        MyAdapter adapter = new MyAdapter(items, MainActivity.this); //어댑터에 이미지 리스트를 매개변수로 생성
        carouselRecyclerview.setAdapter(adapter);
    } //하드모드 캐러셀뷰어댑터

    public void startDifficultySet() {
        difficulty = 1; //기본값 이지로 설정
        difficultyChangeText(easyBtn, 1.0f, 1.6f); //이지버튼 점점크게 애니메이터
    }  // 초기 난이도버튼 설정

    public void difficultyChangeText(TextView text, float startSize, float endSize) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(text, "scaleX", startSize, endSize);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(text, "scaleY", startSize, endSize);

        scaleXAnimator.setDuration(600);
        scaleYAnimator.setDuration(600);

        scaleXAnimator.start();
        scaleYAnimator.start();
    }

    private void updateCenterItem_Descripttion(RecyclerView
                                                       recyclerView, List<ImageItem> items) {
        int recyclerViewCenterX = (recyclerView.getLeft() + recyclerView.getRight()) / 2; //중앙위치
        int minDistance = Integer.MAX_VALUE; //최소거리
        View closetChild = null; //센터값 기본값

        for (int i = 0; i <= recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i); // 해당 순서의 자손을 뷰로 참조\
            if (child != null) { //자손이 null인지 아닌지 확인( 자손이 로딩되기전에 사용해 NullPointException을 방지하기위해)
                int childCenterX = (child.getLeft() + child.getRight()) / 2; //자손의 x값 계산
                int distance = Math.abs(recyclerViewCenterX - childCenterX); //리사이클러뷰 x좌표와 자손x값을 빼서 거리계산(노트랑비슷함ㅋㅋ)

                if (distance < minDistance) {
                    minDistance = distance; // 최소거리가 거리차이보다 크다면 -> 최소거리를 거리값으로 변경
                    closetChild = child; // 가까운 자손뷰 변수에 자손입력
                }
            }
        }

        if (closetChild != null) {
            int position = recyclerView.getChildAdapterPosition(closetChild);
            if (position != RecyclerView.NO_POSITION) {// -1 기본값이 아니라면

                switch (position) {
                    case 0:
                        cannonRemix();
                        break;
                    case 1:
                        shiningLight();
                        break;
                    case 2:
                        xeus();
                        break;
                    case 3:
                        odysseus();
                        break;
                    case 4:
                        limbo();
                        break;
                    case 5:
                        skyHigh();
                        break;
                } //위치를 받아 곡정보를 실시간으로 삽입

                playPreview(position); //인자값을 토대로 노래플레이어 시작.

                if (difficulty == 1) {
                    easyModDifficultyColor(
                            items.get(position).getDifficulty(),
                            song_difficulty_Main
                    );
                } else if (difficulty == 2) {
                    hardModDifficultyColor(
                            items.get(position).getDifficulty(),
                            song_difficulty_Main
                    );
                }
                song_name_Main.setText(items.get(position).getDescription()); //선택된 자손의 설명값을 텍스트로설정
            }
        }
    }

    public static void easyModDifficultyColor(String difficulty, TextView textView) {
        SpannableString spannableString = new SpannableString(difficulty); //난이도텍스트를 spannable String으로 받음

        int maxLength = difficulty.length();// 받은 난이도(별)의 글자 길이

        // 아래의 모든코드는 maxLength 까지만 글자색깔을 인식해 변경합니다.
        if (maxLength >= 7) { //글자가 7글자 이상일때
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 6, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //빨강
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FFA500")), 3, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //주황
            spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //노랑색
        }
        if (maxLength >= 4 && !(maxLength >= 7)) { //글자가 1~6글자일시
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FFA500")), 3, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //주황
            spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //노랑색
        }
        if (maxLength >= 1 && !(maxLength >= 4)) { //글자가 1~3글자일시
            spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //노랑색
        }

        textView.setText(spannableString); //선택된 자손의 난이도값을 텍스트로설정
    } // 이지모드 선택시 난이도 별색깔 SpannableString + 곡명 세팅

    public static void hardModDifficultyColor(String difficulty, TextView textView) {
        SpannableString spannableString = new SpannableString(difficulty); // 난이도텍스트를 spannable String으로 받음

        int maxLength = difficulty.length(); // 받은 난이도(별)의 글자 길이

        // 아래의 모든코드는 maxLength 까지만 글자색깔을 인식해 변경합니다.
        if (maxLength >= 7) { //글자가 7글자 이상일때
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3D67FF")), 6, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //진한파랑
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#C504E2")), 3, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //진한보라
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#E00074")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //진한빨강
        }
        if (maxLength >= 4 && !(maxLength >= 7)) { //글자가 1~6글자일시
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#C504E2")), 3, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //진한보라
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#E00074")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //진한빨강
        }
        if (maxLength >= 1 && !(maxLength >= 4)) { //글자가 1~3글자일시
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#E00074")), 0, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //진한빨강
        }

        textView.setText(spannableString); //선택된 자손의 난이도값을 텍스트로설정
    } // 하드모드 선택시 난이도 별색깔 SpannableString + 곡명 세팅

    public void settingCustomDialog() {
        SettingDialog dialog = new SettingDialog(MainActivity.this);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //다이어로그 배경 투명처리
    } //세팅 다이어로그 생성메소드

    public static void setDefaultSpeed(int speedIndex, ImageView speedBtn) {
        switch (speedIndex) {
            case 1:
                setSpeed = 3000; // 1배속 값을 세팅
                setSpeedJudgment = 1; // 1배율을 세팅
                speedBtn.setImageResource(R.drawable.speedbtn_1x_img); //버튼 텍스트를 1배속으로 변경
                break;
            case 2:
                setSpeed = 2000;
                setSpeedJudgment = 1.5f;
                speedBtn.setImageResource(R.drawable.speedbtn_15x_img);
                break;
            case 3:
                setSpeed = 1500;
                setSpeedJudgment = 2f;
                speedBtn.setImageResource(R.drawable.speedbtn_2x_img);
                break;
            case 4:
                setSpeed = 1200;
                setSpeedJudgment = 2.5f;
                speedBtn.setImageResource(R.drawable.speedbtn_25x_img);
                break;
            case 5:
                setSpeed = 1000;
                setSpeedJudgment = 3f;
                speedBtn.setImageResource(R.drawable.speedbtn_3x_img);
                break;
            case 6:
                setSpeed = 857;
                setSpeedJudgment = 3.5f;
                speedBtn.setImageResource(R.drawable.speedbtn_35x_img);
                break;
        }
    } // 다이어로그 화면 구성시 사용할 배속모드 초기 인덱스값(혹은 db에서 받아온 유저의 배속값)을 버튼+배속에 설정

    public static void setDefaultAutoMode(boolean automode, ImageView autoModBtn) {
        if (automode) {
            autoModBtn.setImageResource(R.drawable.autobtn_img);
        } else {
            autoModBtn.setImageResource(R.drawable.nonebtn_img);
        }
    } // 다이어로그 화면구성시 사용할 오토모드를 앱내변수값으로 바꿔주는 초기화메소드

    public static void setDefaultGameMode(int gameModeIndex, ImageView gameModBtn) {
        switch (gameModeIndex) {
            case 0:
                gameModBtn.setImageResource(R.drawable.normalbtn_img); //버튼 텍스트를 1배속으로 변경
                break;
            case 1:
                gameModBtn.setImageResource(R.drawable.mirrorbtn_img);
                break;
            case 2:
                gameModBtn.setImageResource(R.drawable.randombtn_img);
                break;
        }
    } // 다이얼로그 화면구성시 사용할 모드값을 앱내 모드변수로 설정해주는 메소드

    public void showCustomDialog() {
        SelectSongDialog dialog = new SelectSongDialog(MainActivity.this);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    } // 곡을 클릭했을때 곡정보,내 최고기록,플레이모드설정,플레이버튼을 띄우는 다이어로그를 생성하는 메소드

    public void showRankingDialog() {
        RankingDialog dialog = new RankingDialog(MainActivity.this);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    } // 랭킹버튼을 눌렀을시 랭킹 다이어로그 출력

    public void odysseus() {
        if (difficulty == 1) {
            songImage = R.drawable.odysseus_img;  // 오디세우스 이미지설정
            songBPM = "187";  // 오디세우스 bpm
            songName = "Odysseus[EASY]";  // 오디세우스 곡제목
            songDifficulty = "★★★★★";  // 오디세우스 난이도
            noteData = R.raw.odysseus; // 오디세우스 채보 데이터
            song_mp3 = R.raw.xeon; // 오디세우스 mp3파일
            songMV = R.raw.odysseus_mv;
        }
        if (difficulty == 2) {
            songImage = R.drawable.odysseus_img;
            songBPM = "187";
            songName = "Odysseus[HARD]";
            songDifficulty = "★★★★★★★★";
            noteData = R.raw.odysseus_hard;
            song_mp3 = R.raw.xeon;
            songMV = R.raw.odysseus_mv;
        }
    }  // 오디세우스 곡 정보 설정 메소드

    public void xeus() {
        if (difficulty == 1) {
            songImage = R.drawable.xeus_img;
            songBPM = "148";
            songName = "Xeus[EASY]";
            songDifficulty = "★★★★★★";
            noteData = R.raw.xeus_easy;
            song_mp3 = R.raw.xeus;
            songMV = R.raw.xeus_mv;
        }
        if (difficulty == 2) {
            songImage = R.drawable.xeus_img;
            songBPM = "148";
            songName = "Xeus[HARD]";
            songDifficulty = "★★★★★";
            noteData = R.raw.xeus_hard;
            song_mp3 = R.raw.xeus;
            songMV = R.raw.xeus_mv;
        }
    }  // 제우스 곡 정보 설정 메소드

    public void limbo() {
        if (difficulty == 1) {
            songImage = R.drawable.limbo_img;
            songBPM = "191.9";
            songName = "Limbo[EASY]";
            songDifficulty = "★★★★★★★★★";
            noteData = R.raw.limbo_easy;
            song_mp3 = R.raw.limbo;
            songMV = R.raw.limbo_mv;
        }
        if (difficulty == 2) {
            songImage = R.drawable.limbo_img;
            songBPM = "191.9";
            songName = "Limbo[HARD]";
            songDifficulty = "★★★★★★★★★";
            noteData = R.raw.limbo_hard;
            song_mp3 = R.raw.limbo;
            songMV = R.raw.limbo_mv;
        }
    }  // 림보 곡 정보 설정 메소드

    public void shiningLight() {
        if (difficulty == 1) {
            songImage = R.drawable.shining_light_img;
            songBPM = "178";
            songName = "ShiningLight[EASY]";
            songDifficulty = "★★★";
            noteData = R.raw.shining_light_easy;
            song_mp3 = R.raw.shining_light;
            songMV = R.raw.shining_light_mv;
        }
        if (difficulty == 2) {
            songImage = R.drawable.shining_light_img;
            songBPM = "178";
            songName = "ShiningLight[HARD]";
            songDifficulty = "★★★★";
            noteData = R.raw.shining_light_hard;
            song_mp3 = R.raw.shining_light;
            songMV = R.raw.shining_light_mv;
        }
    }  // 샤이닝라이트 곡 정보 설정 메소드

    public void cannonRemix() {
        if (difficulty == 1) {
            songImage = R.drawable.cannon_img;
            songBPM = "160";
            songName = "Cannon_Remix[EASY]";
            songDifficulty = "★★";
            noteData = R.raw.cannon_easy;
            song_mp3 = R.raw.cannon;
            songMV = 0;
        }
        if (difficulty == 2) {
            songImage = R.drawable.cannon_img;
            songBPM = "160";
            songName = "Cannon_Remix[HARD]";
            songDifficulty = "★★★";
            noteData = R.raw.cannon_hard;
            song_mp3 = R.raw.cannon;
            songMV = 0;
        }
    }  // 캐논리믹스 곡 정보 설정 메소드

    public void skyHigh() {
        if (difficulty == 1) {
            songImage = R.drawable.skyhigh_img;
            songBPM = "128";
            songName = "SkyHigh[EASY]";
            songDifficulty = "★★★★★★★★";
            noteData = R.raw.sky_high_easy;
            song_mp3 = R.raw.sky_high;
            songMV = 0;
        }
        if (difficulty == 2) {
            songImage = R.drawable.skyhigh_img;
            songBPM = "128";
            songName = "SkyHigh[HARD]";
            songDifficulty = "★★★★★★★";
            noteData = R.raw.sky_high_hard;
            song_mp3 = R.raw.sky_high;
            songMV = 0;
        }
    }  // 샤이닝라이트 곡 정보 설정 메소드

    @Override
    public void onItemSelected() {
        showCustomDialog();
    } //다이어로그 인터페이스 오버라이드 메소드

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

