package com.example.rhythmproto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button gamestartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gamestartBtn = findViewById(R.id.gameStartButton);

        gamestartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<NoteData> notes = OsuFileParser.parseOsuFile(MainActivity.this,R.raw.odysseus);
                Intent intent = new Intent(MainActivity.this,GameActivity.class);
                intent.putParcelableArrayListExtra("notes",new ArrayList<>(notes));
                startActivity(intent);
            }
        });  // 게임 시작

        SoundManager.getInstance().loadSound(this); //판정 드럼소리 로딩
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance().release();
    }
}