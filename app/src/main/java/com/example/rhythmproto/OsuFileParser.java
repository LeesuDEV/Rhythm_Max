package com.example.rhythmproto;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class OsuFileParser {
    public static List<NoteData> parseOsuFile(Context context , int resId) {
        List<NoteData> notes = new ArrayList<>();
        try (InputStream is = context.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean hitObjectsSection = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[HitObjects]")) {
                    hitObjectsSection = true;
                    continue;
                }
                if (hitObjectsSection) {
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",");
                    int x = Integer.parseInt(parts[0]);
                    float timeInMs = Float.parseFloat(parts[2]); // 실수로 시간 파싱
                    int time = (int) timeInMs; // 밀리초 단위로 변환
                    // 추가적인 정보 처리가 필요하면 여기에서 파싱
                    notes.add(new NoteData(x, time));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notes;
    }
}
