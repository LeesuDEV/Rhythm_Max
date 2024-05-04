package com.example.rhythmproto;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    Button gamestartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       //Log.d("getKeyHash", "" + getKeyHash(MainActivity.this));// 키해시(로그캣)
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

    public static String getKeyHash(final Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (packageInfo == null)
                return null;

            for (Signature signature : packageInfo.signatures) {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

