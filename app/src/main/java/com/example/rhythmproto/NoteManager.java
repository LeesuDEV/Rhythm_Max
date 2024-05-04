package com.example.rhythmproto;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class NoteManager {
    private Context context;
    private ViewGroup noteLayout; // 노트가 표시될 레이아웃
    private List<NoteView> notes = new ArrayList<>(); // 생성된 노트 뷰들을 관리하는 리스트
    public Handler handler = new Handler();
    private int note_Speed = 1500; //노트의 배속 (내려오는데 걸리는 시간)
    public static List<NoteView>[] lanes = new List[5];  // 5개의 레인을 위한 배열

    public NoteManager(Context context, ViewGroup noteLayout) {
        this.context = context;
        this.noteLayout = noteLayout;
        this.handler = new Handler(); // 핸들러 초기화

        for (int i = 0; i < lanes.length; i++) {
            lanes[i] = new ArrayList<>();
        }
    } // 노트매니저 초기화값

    public void createNotesFromData(List<NoteData> notesData) {
        long gameStartTime = System.currentTimeMillis();

        for (NoteData data : notesData) {
            long delay = data.time - (System.currentTimeMillis() - gameStartTime);  // 지연 시간 계산
            if (delay < 0) {
                continue; // 과거 시간에 대한 노트는 처리하지 않음
            }
            handler.postDelayed(() -> {
                createNoteView(data);
            }, delay);
        }
    }  // 레인별 노트 데이터를 지연시간을 계산하여 대기 큐에 올린후, 차례가되면 해당 데이터를 가지고 노트출력을 시작하는 메소드

    private void createNoteView(NoteData data) {
        // 노트 라인 위치 계산 (x 값에 따라)
        float startX = calculateXPosition(data.x);
        int index = getLaneIndex(data.x);  // 라인구별을 위한 Index값
        NoteView noteView = new NoteView(context, startX, 0, Color.RED,noteLayout.getHeight(),noteLayout,index);  // 초기 y 위치는 0
        noteLayout.addView(noteView);  // noteLayout은 노트를 포함할 레이아웃의 ID
        notes.add(noteView);  // 관리 목록에 노트 뷰 추가
        // 노트를 내리는 로직 시작
        startFallingAnimation(noteView, noteLayout.getHeight(), note_Speed); // 노트 애니메이션 시작
        assignNoteToLane(data.x,noteView);  //data.x (2번레인 = 103)값을 매개변수로 lane을 구별하여 각 lane의 List<>에 데이터 삽입
    }  // 노트의 파싱데이터로 각 노트를 레인을 구분하여 레인에 데이터를 넣어주고. 노트의 떨어지는 애니메이션 시작을 선언하는 메소드

    private void startFallingAnimation(NoteView noteView, int endY, int duration) {
        noteView.startFalling(noteView,endY, duration);
    } // 노트의 떨어지는 애니메이션


    private float calculateXPosition(int x) {
        // x 좌표 값에 따라 레인 인덱스를 결정
        int laneIndex = getLaneIndex(x);
        int layoutWidth = noteLayout.getWidth();
        float laneWidth = layoutWidth / 4.0f;  // 5개 레인을 가정 165가 나와야함 4개레인 230.25
        float returnValue = (laneWidth * laneIndex);
        return returnValue;
    }  // getLaneIndex의 x값으로 float X좌표값을 반환

    private int getLaneIndex(int x) {
        // 예제 코드, 실제 값에 따라 인덱스를 조정해야 할 수 있습니다.
        if (x == 0) return 0;
        else if (x == 103) return 1;
        else if (x == 205) return 2;
        else if (x == 308) return 3;
        else if (x == 410) return 4;
        return 0;
    }  // 레인 인덱스값을 반환

    private void assignNoteToLane(int index,NoteView noteView){
        if (getLaneIndex(index) == 0) {
            lanes[0].add(noteView);
        } else if (getLaneIndex(index) == 1) {
            lanes[1].add(noteView);
        } else if (getLaneIndex(index) == 2) {
            lanes[2].add(noteView);
        } else if (getLaneIndex(index) == 3) {
            lanes[3].add(noteView);
        } else if (getLaneIndex(index) == 4) {
            lanes[4].add(noteView);
        }
    }// 레인별 노트를 노트리스트에 담아주는 메소드

    public List<NoteView> getLaneNotes(int laneIndex) {
        switch (laneIndex) {
            case 1:
                return lanes[0];
            case 2:
                return lanes[1];
            case 3:
                return lanes[2];
            case 4:
                return lanes[3];
            case 5:
                return lanes[4];
            default:
                return null;
        }
    }  // GameActivity의 TouchEvent에서 해당 터치버튼의 노트라인 데이터를 반환받고자 할때 사용하는 메소드

    public synchronized void removeNoteFromLane(NoteView note) {
        for (List<NoteView>lane : lanes) {
            lane.remove(note);
        }
    }  // NoteView내에서 판정됐거나 Miss처리된 객체를 리스트에서 삭제하는 동기화 메소드

    public void shutdownHandler() {
        handler.removeCallbacksAndMessages(null);  //GameActvity 종료시 예약메세지와 콜백을 삭제하기위함
    }  // NoteManager 노트생성 핸들러 종료메소드
}