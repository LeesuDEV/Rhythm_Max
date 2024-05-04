package com.example.rhythmproto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    LinearLayout odysseus;

    String songName;
    String songDifficulty;
    String songBPM;
    int songImage; //곡 이미지
    int noteData; //채보데이터
    int difficultySet = 1;  //  1은 easy 2는 hard
    String selectSong = ""; // 선택된 곡

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        odysseus = findViewById(R.id.odysseus);

        odysseus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSong(odysseus);  // 곡선택 로직 동작후 커스텀다이어로그 생성
            }
        }); //오디세우스 레이아웃 클릭시

        SoundManager.getInstance().loadSound(this); //판정 드럼소리 로딩
    }

    public void selectSong(LinearLayout song) {
        switch (song.getId()){
            case R.id.odysseus :
                selectSong = "odysseus";
                odysseus(); // 곡정보를 odysseus로 설정
                showCustomDialog();
                break;
        }
    }

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
                List<NoteData> notes = OsuFileParser.parseOsuFile(MainActivity.this, noteData);  // 선택된곡 노트데이터를 리스트에 삽입
                Intent intent = new Intent(MainActivity.this, GameActivity.class); // 인턴트생성
                intent.putParcelableArrayListExtra("notes", new ArrayList<>(notes)); // 채보리스트를 인턴트에 담아서 전송
                intent.putExtra("songImage",songImage);  // 곡이미지 인턴트전송
                intent.putExtra("songName",songName);  // 곡이름 인턴트전송
                intent.putExtra("songDifficulty",songDifficulty);  // 곡 난이도 인턴트전송
                intent.putExtra("songBpm",songBPM);  // 곡 bpm 인턴트전송
                startActivity(intent);
                dialog.dismiss();
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
            songDifficulty = "★★★★★";  // 오디세우스 난이도
            noteData = R.raw.odysseus;
        }
        if (difficultySet == 2) {
            songImage = R.drawable.odysseus_img;  // 오디세우스 이미지설정
            songBPM = "187";  // 오디세우스 bpm
            songName = "Odysseus[HARD]";  // 오디세우스 곡제목
            songDifficulty = "★★★★★★★★";  // 오디세우스 난이도
            noteData = R.raw.odysseus;
        }
    }  // 오디세우스 곡 정보 설정 메소드

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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance().release();
    }
}

