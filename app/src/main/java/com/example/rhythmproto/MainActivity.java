package com.example.rhythmproto;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager;
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    CarouselRecyclerview carouselRecyclerview; //캐러셀뷰(3d갤러리)
    static String userId ; //유저 식별값
    static String userName ; //유저 닉네임

    String songName;
    String songDifficulty;
    String songBPM;
    int songImage; //곡 이미지
    int song_mp3; //노래 파일
    int noteData; //채보데이터
    String selectSong = ""; // 선택된 곡
    Switch clapSoundSwitch; // 타격음 소리 스위치
    static boolean clapSoundIndex; //타격음 소리 인덱스값
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
    static int syncValue = 0; // 싱크값
    static boolean autoModIndex = false; // 0ff 기본값 오토모드 인덱스
    static int modIndex = 0; // 모드인덱스값 0은 NORMAL , 1은 MIRROR , 2는 RANDOM

    List<ImageItem> items; // 곡메인화면 어레이리스트

    int difficulty; //메인화면 난이도 인자값
    int current_song = -1; //현재 선택된 캐러셀노래 인자값

    MediaPlayer mediaPlayer; // 미리보기노래 미디어플레이어

    static float setSpeed = 1500; // 배속설정
    static int speedIndex = 3; // 배속모드 식별값
    static float setSpeedJudgment = 3; // 배속모드에 따른 판정 배율값

    static float previewSoundAmountIndex = 1.0f; // 배경음악 크기 인덱스값
    static float ingameSoundAmountIndex = 1.0f; // 인게임음악 크기 인덱스값

    LottieAnimationView musicController;

    //1배속 = 3000ms , 1.5배속 = 2000ms , 2배속 = 1500ms , 2.5배속 = 1200ms, 3배속 = 1000ms, 3.5배속 = 857ms
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easyBtn = findViewById(R.id.setEasyBtn);
        hardBtn = findViewById(R.id.setHardBtn);
        clapSoundSwitch = findViewById(R.id.ClapSoundSwitch);
        syncCurrentText = findViewById(R.id.syncTV);
        syncSetBtn = findViewById(R.id.syncBtn);
        syncSetText = findViewById(R.id.syncET);
        bluetoothBtn = findViewById(R.id.bluetoothBtn);
        settingBtn = findViewById(R.id.settingIcon);
        uesrNameTV = findViewById(R.id.userNameTV);

        /*---------------------------------캐러셀 레이아웃 -----------------------------------*/
        carouselRecyclerview = findViewById(R.id.recyclerView); //캐러셀 뷰
        song_name_Main = findViewById(R.id.songNameMainTV); //곡이름 메인타이틀
        song_difficulty_Main = findViewById(R.id.songDifficultyMainTV); //곡난이도 메인타이틀

        Intent intent = getIntent(); //로그인액티비티의 인턴트 받아오기

        if (intent != null) {
            userId = intent.getStringExtra("userId"); //유저 닉네임
            userName = intent.getStringExtra("userName"); //유저 식별값 세팅
        }

        if (userName == null) { //예외사항으로 유저네임을 받아오지 못했을경우
            userName = "Empty";
        }

        uesrNameTV.setText(userName + " 님"); //유저네임 텍스트뷰에 닉네임 표시

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
        //setDefeaultSync(syncValue); //싱크값을 현재 화면에 표시해줌

        //setDefaultClapSound(clapSoundIndex); //화면 구성시 초기 타격음 설정

        /*bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncValue = -250;
                setDefeaultSync(syncValue); //싱크값을 현재 화면에 표시해줌
            }
        }); // 블루투스이어폰 싱크값 -250 세팅*/

        /*syncSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = syncSetText.getText().toString().trim();
                if (!s.isEmpty()) {
                    syncValue = Integer.parseInt(s);
                    syncCurrentText.setText(String.valueOf(syncValue));
                }
            }
        }); //싱크텍스트를 받아서 값을 적용해주는 싱크조절버튼 */

        /*clapSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    clapSoundIndex = true;  // 타격음 인덱스값 변경
                    SoundManager.getInstance().loadSound(MainActivity.this); // 판정타격음 스위치를 켰다면 타격소리를 로딩
                } else {
                    clapSoundIndex = false;
                    SoundManager.instance = null;
                }
            }
        });*/
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
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.limbo_main); //1번 인덱스라면 림보플레이
                    break;
                case 1:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.odysseus_main);
                    break;
                case 2:
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.xeus_main);
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
    } // 인덱스값을 토대로 미디어플레이어를 미리보기 재생시켜주는 메소드

    public void setEasyModAdapter() {
        items = null; // 곡메인화면 리스트초기화
        items = new ArrayList<>(); // 곡 메인화면 어레이리스트 재생성
        items.add(new ImageItem(R.drawable.limbo_main_image, "Limbo", "★★★★★★★★★"));
        items.add(new ImageItem(R.drawable.odysseus_main_image, "Odysseus", "★★★★★"));
        items.add(new ImageItem(R.drawable.xeus_main_image, "Xeus", "★★★★★★"));

        MyAdapter adapter = new MyAdapter(items, MainActivity.this); //어댑터에 이미지 리스트를 매개변수로 생성
        carouselRecyclerview.setAdapter(adapter);
    } //이지모드 캐러셀뷰어댑터

    public void setHardModAdapter() {
        items = null; // 곡메인화면 리스트초기화
        items = new ArrayList<>(); // 곡 이미지 어레이리스트 재생성
        items.add(new ImageItem(R.drawable.limbo_main_image, "Limbo", "★★★★★★★★★"));
        items.add(new ImageItem(R.drawable.odysseus_main_image, "Odysseus", "★★★★★★★★★"));
        items.add(new ImageItem(R.drawable.xeus_main_image, "Xeus", "★★★★★★★"));

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
                        limbo();
                        break;
                    case 1:
                        odysseus();
                        break;
                    case 2:
                        xeus();
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

    public void easyModDifficultyColor(String difficulty, TextView textView) {
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

    public void hardModDifficultyColor(String difficulty, TextView textView) {
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

    public void setDefaultSpeed(int speedIndex, Button speedBtn) {
        switch (speedIndex) {
            case 1:
                setSpeed = 3000; // 1배속 값을 세팅
                setSpeedJudgment = 1; // 1배율을 세팅
                speedBtn.setText("1.0"); //버튼 텍스트를 1배속으로 변경
                break;
            case 2:
                setSpeed = 2000;
                setSpeedJudgment = 1.5f;
                speedBtn.setText("1.5");
                break;
            case 3:
                setSpeed = 1500;
                setSpeedJudgment = 2f;
                speedBtn.setText("2.0");
                break;
            case 4:
                setSpeed = 1200;
                setSpeedJudgment = 2.5f;
                speedBtn.setText("2.5");
                break;
            case 5:
                setSpeed = 1000;
                setSpeedJudgment = 3f;
                speedBtn.setText("3.0");
                break;
            case 6:
                setSpeed = 857;
                setSpeedJudgment = 3.5f;
                speedBtn.setText("3.5");
                break;
        }
    } //화면 구성시 초기 인덱스값(혹은 db에서 받아온 유저의 배속값)을 버튼+배속에 설정

    public void setDefaultClapSound(boolean clapSoundIndex) {
        if (clapSoundIndex) {
            clapSoundSwitch.setChecked(true);
            SoundManager.getInstance().loadSound(MainActivity.this); // 판정타격음 스위치를 켰다면 타격소리를 로딩
        } else {
            clapSoundSwitch.setChecked(false);
            SoundManager.instance = null;
        }
    }  // 화면구성시 타격음 설정

    public void setDefaultAutoMode(boolean automode, Button autoModBtn) {
        if (automode) {
            autoModBtn.setText("AUTO");
        } else {
            autoModBtn.setText("NONE");
        }
    } // 화면구성시 오토모드를 현재값으로 바꿔주는 초기화메소드

    public void settingCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.setting_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        SeekBar previewSoundBar = dialogView.findViewById(R.id.previewSoundBar);
        SeekBar ingameSoundBar = dialogView.findViewById(R.id.ingameSoundBar);
        TextView fastSync = dialogView.findViewById(R.id.syncFastBtn);
        TextView slowSync = dialogView.findViewById(R.id.syncSlowBtn);
        TextView currentSyncTV = dialogView.findViewById(R.id.currentSyncTV);

        previewSoundBar.setProgress((int) previewSoundAmountIndex * 10); // 현재 프리뷰볼륨으로 setting progress값 설정
        previewSoundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                previewSoundAmountIndex = (float) progress / 10; // 설정값을 배경음악 프로그래스에 설정
                mediaPlayer.setVolume(previewSoundAmountIndex, previewSoundAmountIndex);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }); // 프리뷰볼륨 조절

        ingameSoundBar.setProgress((int) ingameSoundAmountIndex * 10); // 현재 인게임볼륨으로 setting progress값 설정
        ingameSoundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ingameSoundAmountIndex = (float) progress / 10; // 설정값을 배경음악 프로그래스에 설정
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }); // 인게임볼륨 조절ㅁ

        currentSyncTV.setText(""+syncValue);
        fastSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncValue -= 10;
                currentSyncTV.setText(""+syncValue);
            }
        });

        slowSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncValue += 10;
                currentSyncTV.setText(""+syncValue);
            }
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //다이어로그 배경 투명처리

        //다이어로그의 크기조절
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    } //세팅 다이어로그 생성메소드

    public void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.song_setting_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageView songImageView = dialogView.findViewById(R.id.selectSongImageView);
        TextView songNameTV = dialogView.findViewById(R.id.songNameTV);
        TextView difficultyTV = dialogView.findViewById(R.id.difficultyTV);
        TextView bpmTV = dialogView.findViewById(R.id.bpmTV);

        TextView startBtn = dialogView.findViewById(R.id.startBtn);
        TextView cancelBtn = dialogView.findViewById(R.id.cencelBtn);

        Button setSpeedBtn = dialogView.findViewById(R.id.setSpeedBtn);
        Button setModBtn = dialogView.findViewById(R.id.setModeBtn);
        Button autoModBtn = dialogView.findViewById(R.id.autoModBtn);

        songImageView.setImageResource(songImage);  // 선택된곡의 이미지 세팅
        songNameTV.setText(songName);  // 선택된곡의 이름 세팅
        bpmTV.setText("bpm : " + songBPM);  // 선택된곡의 bpm 세팅
        if (difficulty == 1) {
            easyModDifficultyColor(songDifficulty, difficultyTV);
        } else {
            hardModDifficultyColor(songDifficulty, difficultyTV);
        } //이지모드는 이지모드색깔로 텍스트뷰 설정, 하드모드는 하드모드 색깔로 텍스트뷰 설정

        setDefaultSpeed(speedIndex, setSpeedBtn); //다이어로그 구성시 초기 인덱스값(혹은 db에서 받아온 유저의 배속값)을 버튼+배속에 설정
        setDefaultAutoMode(autoModIndex, autoModBtn); //화면 구성시 오토모드 인덱스값을 텍스트에 부여


        setSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (speedIndex) {
                    case 1:
                        speedIndex = 2; // 배속 식별값을 2로변경 (1.5배 식별값)
                        setSpeed = 2000; // 1.5배속 값을 세팅
                        setSpeedJudgment = 1.5f; // 1.5배율을 세팅
                        setSpeedBtn.setText("1.5"); //버튼 텍스트를 1.5배속으로 변경
                        break;
                    case 2:
                        speedIndex = 3;
                        setSpeed = 1500;
                        setSpeedJudgment = 2f;
                        setSpeedBtn.setText("2.0");
                        break;
                    case 3:
                        speedIndex = 4;
                        setSpeed = 1200;
                        setSpeedJudgment = 2.5f;
                        setSpeedBtn.setText("2.5");
                        break;
                    case 4:
                        speedIndex = 5;
                        setSpeed = 1000;
                        setSpeedJudgment = 3;
                        setSpeedBtn.setText("3.0");
                        break;
                    case 5:
                        speedIndex = 6;
                        setSpeed = 857;
                        setSpeedJudgment = 3.5f;
                        setSpeedBtn.setText("3.5");
                        break;
                    case 6:
                        speedIndex = 1;
                        setSpeed = 3000;
                        setSpeedJudgment = 1;
                        setSpeedBtn.setText("1.0"); // 배속으로 다시 변경
                        break;
                }
            }
        }); // 버튼클릭시 배속설정을 바꾸는 리스너
        setModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (modIndex) {
                    case 0:
                        setModBtn.setText("MIRR");
                        modIndex = 1;
                        break;
                    case 1:
                        setModBtn.setText("RAND");
                        modIndex = 2;
                    case 2:
                        setModBtn.setText("NORM");
                        modIndex = 0;
                }
            }
        }); // 버튼클릭시 모드를 바꾸는 리스너
        autoModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoModIndex) {
                    autoModBtn.setText("NONE");
                    autoModIndex = false;
                } else {
                    autoModBtn.setText("AUTO");
                    autoModIndex = true;
                }
            }
        }); //버튼 클릭시 오토모드를 바꿔주는 리스너
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<NoteData> notes = OsuFileParser.parseOsuFile(MainActivity.this, noteData);  // 선택된곡 노트데이터를 리스트에 삽입
                Intent intent = new Intent(MainActivity.this, GameActivity.class); // 인턴트생성
                intent.putParcelableArrayListExtra("notes", new ArrayList<>(notes)); // 채보리스트를 인턴트에 담아서 전송
                intent.putExtra("songImage", songImage);  // 곡이미지 인턴트전송
                intent.putExtra("songName", songName);  // 곡이름 인턴트전송
                intent.putExtra("songDifficulty", songDifficulty);  // 곡 난이도 인턴트전송
                intent.putExtra("songBpm", songBPM);  // 곡 bpm 인턴트전송
                intent.putExtra("song_mp3", song_mp3);  // 곡 노래mp3 인턴트 전송
                intent.putExtra("setSpeed", setSpeed);  // 배속설정 인턴트 전송
                intent.putExtra("setSpeedJudgment", setSpeedJudgment); // 배율을 인턴트로 전송
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        }); // 게임정보를 넘기고 게임스타트를 해주는 스타트버튼 리스너
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        }); // 다이어로그를 닫는 취소버튼 리스너
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    } // 곡을 클릭했을때 곡정보,내 최고기록,플레이모드설정,플레이버튼을 띄우는 다이어로그를 생성하는 메소드

    public void odysseus() {
        if (difficulty == 1) {
            songImage = R.drawable.odysseus_img;  // 오디세우스 이미지설정
            songBPM = "187";  // 오디세우스 bpm
            songName = "Odysseus[EASY]";  // 오디세우스 곡제목
            songDifficulty = "★★★★★";  // 오디세우스 난이도
            noteData = R.raw.odysseus; // 오디세우스 채보 데이터
            song_mp3 = R.raw.xeon; // 오디세우스 mp3파일
        }
        if (difficulty == 2) {
            songImage = R.drawable.odysseus_img;
            songBPM = "187";
            songName = "Odysseus[HARD]";
            songDifficulty = "★★★★★★★★★";
            noteData = R.raw.odysseus_hard;
            song_mp3 = R.raw.xeon;
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
        }
        if (difficulty == 2) {
            songImage = R.drawable.xeus_img;
            songBPM = "148";
            songName = "Xeus[HARD]";
            songDifficulty = "★★★★★★★";
            noteData = R.raw.xeus_hard;
            song_mp3 = R.raw.xeus;
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
        }
        if (difficulty == 2) {
            songImage = R.drawable.limbo_img;
            songBPM = "191.9";
            songName = "Limbo[HARD]";
            songDifficulty = "★★★★★★★★★";
            noteData = R.raw.limbo_hard;
            song_mp3 = R.raw.limbo;
        }
    }  // 제우스 곡 정보 설정 메소드

    @Override
    public void onItemSelected() {
        showCustomDialog();
    } //다이어로그 인터페이스 오버라이드 메소드

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance().release();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

