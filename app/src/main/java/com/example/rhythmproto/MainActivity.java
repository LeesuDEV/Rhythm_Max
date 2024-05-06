package com.example.rhythmproto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    LinearLayout odysseus;
    LinearLayout Xeus;
    LinearLayout Limbo;

    String songName;
    String songDifficulty;
    String songBPM;
    int songImage; //곡 이미지
    int song_mp3; //노래 파일
    int noteData; //채보데이터
    int difficultySet = 1;  //  1은 easy 2는 hard
    String selectSong = ""; // 선택된 곡
    Switch clapSoundSwitch; // 타격음 소리 스위치
    static boolean clapSoundIndex; //타격음 소리 인덱스값
    Button setSpeedBtn; //배속설정 버튼
    Button autoModBtn; // 오토모드 버튼
    Button syncSetBtn; // 싱크조절 버튼
    Button bluetoothBtn; // 블루투스 이어폰 자동세팅버튼
    EditText syncSetText; // 싱크조절 에딧텍스트
    TextView syncCurrentText; // 싱크현재값 텍스트
    static int syncValue=0; // 싱크값
    static float setSpeed = 1500; // 배속설정
    static int speedIndex = 3; // 배속모드 식별값
    static float setSpeedJudgment = 3; // 배속모드에 따른 판정 배율값
    static boolean autoModIndex = false; // 0ff 기본값 오토모드 인덱스

    //1배속 = 3000ms , 1.5배속 = 2000ms , 2배속 = 1500ms , 2.5배속 = 1200ms, 3배속 = 1000ms, 3.5배속 = 857ms
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        odysseus = findViewById(R.id.odysseus);
        Xeus = findViewById(R.id.Xeus);
        Limbo = findViewById(R.id.Limbo);
        clapSoundSwitch = findViewById(R.id.ClapSoundSwitch);
        setSpeedBtn = findViewById(R.id.speedSetBtn);
        autoModBtn = findViewById(R.id.autoModBtn);
        syncCurrentText = findViewById(R.id.syncTV);
        syncSetBtn = findViewById(R.id.syncBtn);
        syncSetText = findViewById(R.id.syncET);
        bluetoothBtn = findViewById(R.id.bluetoothBtn);

        setDefaultSpeed(speedIndex); //화면 구성시 초기 인덱스값(혹은 db에서 받아온 유저의 배속값)을 버튼+배속에 설정

        setDefaultMode(autoModIndex); //화면 구성시 오토모드 인덱스값을 텍스트에 부여

        setDefeaultSync(syncValue); //싱크값을 현재 화면에 표시해줌

        setDefaultClapSound(clapSoundIndex); //화면 구성시 초기 타격음 설정

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncValue = -250;
                setDefeaultSync(syncValue); //싱크값을 현재 화면에 표시해줌
            }
        }); // 블루투스이어폰 싱크값 -250 세팅

        syncSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = syncSetText.getText().toString().trim();
                if (!s.isEmpty()){
                    syncValue = Integer.parseInt(s);
                    syncCurrentText.setText(String.valueOf(syncValue));
                }
            }
        }); //싱크텍스트를 받아서 값을 적용해주는 싱크조절버튼

        autoModBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!autoModIndex){
                    autoModBtn.setText("AutoMod"); //버튼 텍스트를 오토모드로 변경
                    autoModIndex=true; //오토모드를 ON
                } else {
                    autoModBtn.setText("PlayMod"); //플레이 모드로 텍스트 변경
                    autoModIndex=false; //오토모드를 OFF
                }
            }
        });
        setSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (speedIndex) {
                    case 1 :
                        speedIndex = 2; // 배속 식별값을 2로변경 (1.5배 식별값)
                        setSpeed = 2000; // 1.5배속 값을 세팅
                        setSpeedJudgment = 1.5f; // 1.5배율을 세팅
                        setSpeedBtn.setText("1.5X"); //버튼 텍스트를 1.5배속으로 변경
                        break;
                    case 2 :
                        speedIndex = 3;
                        setSpeed = 1500;
                        setSpeedJudgment = 2f;
                        setSpeedBtn.setText("2X");
                        break;
                    case 3 :
                        speedIndex = 4;
                        setSpeed = 1200;
                        setSpeedJudgment = 2.5f;
                        setSpeedBtn.setText("2.5X");
                        break;
                    case 4 :
                        speedIndex = 5;
                        setSpeed = 1000;
                        setSpeedJudgment = 3;
                        setSpeedBtn.setText("3X");
                        break;
                    case 5 :
                        speedIndex = 6;
                        setSpeed = 857;
                        setSpeedJudgment = 3.5f;
                        setSpeedBtn.setText("3.5X");
                        break;
                    case 6 :
                        speedIndex = 1;
                        setSpeed = 3000;
                        setSpeedJudgment = 1;
                        setSpeedBtn.setText("1X"); // 배속으로 다시 변경
                        break;
                }
            }
        });  // 배속설정버튼


        clapSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        });

        odysseus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSong(odysseus);  // 곡선택 로직 동작후 커스텀다이어로그 생성
            }
        }); //오디세우스 레이아웃 클릭시

        Xeus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSong(Xeus);  // 곡선택 로직 동작후 커스텀다이어로그 생성
            }
        }); //제우스 레이아웃 클릭시

        Limbo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSong(Limbo);  // 곡선택 로직 동작후 커스텀다이어로그 생성
            }
        });
    }

    public void setDefaultSpeed(int speedIndex) {
       switch (speedIndex){
           case 1 :
               setSpeed = 3000; // 1배속 값을 세팅
               setSpeedJudgment = 1; // 1배율을 세팅
               setSpeedBtn.setText("1X"); //버튼 텍스트를 1배속으로 변경
               break;
           case 2 :
               setSpeed = 2000;
               setSpeedJudgment = 1.5f;
               setSpeedBtn.setText("1.5X");
               break;
           case 3 :
               setSpeed = 1500;
               setSpeedJudgment = 2f;
               setSpeedBtn.setText("2X");
               break;
           case 4 :
               setSpeed = 1200;
               setSpeedJudgment = 2.5f;
               setSpeedBtn.setText("2.5X");
               break;
           case 5 :
               setSpeed = 1000;
               setSpeedJudgment = 3f;
               setSpeedBtn.setText("3X");
               break;
           case 6 :
               setSpeed = 857;
               setSpeedJudgment = 3.5f;
               setSpeedBtn.setText("3.5X");
               break;
       }
    } //화면 구성시 초기 인덱스값(혹은 db에서 받아온 유저의 배속값)을 버튼+배속에 설정

    public void setDefaultClapSound(boolean clapSoundIndex){
        if (clapSoundIndex){
            clapSoundSwitch.setChecked(true);
            SoundManager.getInstance().loadSound(MainActivity.this); // 판정타격음 스위치를 켰다면 타격소리를 로딩
        } else {
            clapSoundSwitch.setChecked(false);
            SoundManager.instance = null;
        }
    }  // 화면구성시 타격음 설정
    public void setDefeaultSync(int syncValue){
        syncCurrentText.setText(String.valueOf(syncValue));
    } //현재 싱크값을 표시해줌
    public void selectSong(LinearLayout song) {
        switch (song.getId()){
            case R.id.odysseus :
                selectSong = "odysseus";  // 선택된곡 String으로 난이도 조절 메소드에서 곡정보를 인식
                odysseus(); // 곡정보를 odysseus로 설정
                showCustomDialog();
                break;
            case R.id.Xeus :
                selectSong = "xeus";  // 선택된곡 String으로 난이도 조절 메소드에서 곡정보를 인식
                xeus(); // 곡정보를 xeus로  설정
                showCustomDialog();
                break;
            case R.id.Limbo :
                selectSong = "limbo";  // 선택된곡 String으로 난이도 조절 메소드에서 곡정보를 인식
                limbo(); // 곡정보를 xeus로  설정
                showCustomDialog();
                break;
        }
    } //레이아웃 이름을 가지고와서 곡정보를 세팅하고 다이어로그를 띄움

    public void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.song_setting_dialog,null);
        builder.setView(dialogView);

        difficultySet = 1; // 다이어로그를 열면 항상 난이도는 이지로 설정

        AlertDialog dialog = builder.create();

        ImageView songImageView = dialogView.findViewById(R.id.selectSongImageView);
        TextView songNameTV = dialogView.findViewById(R.id.songNameTV);
        TextView difficultyTV = dialogView.findViewById(R.id.difficultyTV);
        TextView bpmTV = dialogView.findViewById(R.id.bpmTV);

        Button startBtn = dialogView.findViewById(R.id.startBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button easyBtn = dialogView.findViewById(R.id.setEasyBtn);
        Button hardBtn = dialogView.findViewById(R.id.setHardBtn);

        changeDialogInfo(songImageView,songNameTV,difficultyTV,bpmTV); // 다이어로그 정보 변경

        easyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficultySet = 1;
                setDifficulty(difficultySet,songImageView,songNameTV,difficultyTV,bpmTV); // 난이도 변경후 다이어로그 정보 갱신
                easyBtn.setTextColor(Color.parseColor("#ff0000")); //버튼색상변경
                hardBtn.setTextColor(Color.parseColor("#000000"));
            }
        });  // 이지모드 선택시 곡정보 업데이트후 다이어로그 정보 변경
        hardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficultySet = 2;
                setDifficulty(difficultySet,songImageView,songNameTV,difficultyTV,bpmTV); // 난이도 변경후 다이어로그 정보 갱신
                easyBtn.setTextColor(Color.parseColor("#000000"));  //버튼색상변경
                hardBtn.setTextColor(Color.parseColor("#ff0000"));
            }
        });  // 하드모드 선택시 곡정보 업데이트후 다이어로그 정보 변경
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*----------------------------보낼 인턴트 값 모음 시작------------------------*/
                List<NoteData> notes = OsuFileParser.parseOsuFile(MainActivity.this, noteData);  // 선택된곡 노트데이터를 리스트에 삽입
                Intent intent = new Intent(MainActivity.this, GameActivity.class); // 인턴트생성
                intent.putParcelableArrayListExtra("notes", new ArrayList<>(notes)); // 채보리스트를 인턴트에 담아서 전송
                intent.putExtra("songImage",songImage);  // 곡이미지 인턴트전송
                intent.putExtra("songName",songName);  // 곡이름 인턴트전송
                intent.putExtra("songDifficulty",songDifficulty);  // 곡 난이도 인턴트전송
                intent.putExtra("songBpm",songBPM);  // 곡 bpm 인턴트전송
                intent.putExtra("song_mp3",song_mp3);  // 곡 노래mp3 인턴트 전송
                intent.putExtra("setSpeed",setSpeed);  // 배속설정 인턴트 전송
                intent.putExtra("setSpeedJudgment",setSpeedJudgment); // 배율을 인턴트로 전송
                startActivity(intent);
                dialog.dismiss();
                /*----------------------------보낼 인턴트 값 모음 끝------------------------*/
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void odysseus(){
        if (difficultySet == 1) {
            songImage = R.drawable.odysseus_img;  // 오디세우스 이미지설정
            songBPM = "187";  // 오디세우스 bpm
            songName = "Odysseus[EASY]";  // 오디세우스 곡제목
            songDifficulty = "★★★★★★";  // 오디세우스 난이도
            noteData = R.raw.odysseus; // 오디세우스 채보 데이터
            song_mp3 = R.raw.xeon; // 오디세우스 mp3파일
        }
        if (difficultySet == 2) {
            songImage = R.drawable.odysseus_img;
            songBPM = "187";
            songName = "Odysseus[HARD]";
            songDifficulty = "★★★★★★★★";
            noteData = R.raw.odysseus_hard;
            song_mp3 = R.raw.xeon;
        }
    }  // 오디세우스 곡 정보 설정 메소드

    public void xeus(){
        if (difficultySet == 1) {
            songImage = R.drawable.xeus_img;
            songBPM = "148";
            songName = "Xeus[EASY]";
            songDifficulty = "★★★★★★";
            noteData = R.raw.xeus_easy;
            song_mp3 = R.raw.xeus;
        }
        if (difficultySet == 2) {
            songImage = R.drawable.xeus_img;
            songBPM = "148";
            songName = "Xeus[HARD]";
            songDifficulty = "★★★★★★★★★";
            noteData = R.raw.xeus_hard ;
            song_mp3 = R.raw.xeus;
        }
    }  // 제우스 곡 정보 설정 메소드

    public void limbo(){
        if (difficultySet == 1) {
            songImage = R.drawable.limbo_img;
            songBPM = "191.9";
            songName = "Limbo[EASY]";
            songDifficulty = "★★★★★★";
            noteData = R.raw.limbo_easy;
            song_mp3 = R.raw.limbo;
        }
        if (difficultySet == 2) {
            songImage = R.drawable.limbo_img;
            songBPM = "191.9";
            songName = "Limbo[HARD]";
            songDifficulty = "★★★★★★★★★";
            noteData = R.raw.limbo_hard ;
            song_mp3 = R.raw.limbo;
        }
    }  // 제우스 곡 정보 설정 메소드

    public void changeDialogInfo(ImageView songImageView,TextView songNameTV,TextView difficultyTV,TextView bpmTV){
        songImageView.setImageResource(songImage);  // 선택된곡의 이미지 세팅
        songNameTV.setText(songName);  // 선택된곡의 이름 세팅
        difficultyTV.setText(songDifficulty);  // 선택된곡의 난이도 세팅
        bpmTV.setText(songBPM);  // 선택된곡의 bpm 세팅
    } //다이어로그 정보를 바꿔주는 메소드 - 재사용을 위해 뺴놓음

    public void setDifficulty(int difficulty,ImageView songImageView,TextView songNameTV,TextView difficultyTV,TextView bpmTV){
        difficultySet = difficulty;
        switch (selectSong) {
            case "odysseus" :
                odysseus();
                changeDialogInfo(songImageView,songNameTV,difficultyTV,bpmTV); // 다이어로그 정보 변경
                break;
            case "xeus" :
                xeus();
                changeDialogInfo(songImageView,songNameTV,difficultyTV,bpmTV); // 다이어로그 정보 변경
                break;
            case "limbo" :
                limbo();
                changeDialogInfo(songImageView,songNameTV,difficultyTV,bpmTV); // 다이어로그 정보 변경
                break;
        }
    }

    public void setDefaultMode(boolean automode){
        if (automode){
            autoModBtn.setText("AutoMode");
        } else {
            autoModBtn.setText("PlayMode");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance().release();
    }
}

