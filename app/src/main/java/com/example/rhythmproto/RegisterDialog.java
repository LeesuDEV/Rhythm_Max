package com.example.rhythmproto;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterDialog extends Dialog {
    EditText registerId;
    EditText registerNickNameET;
    Button registerBtn;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조
    Context context;

    public RegisterDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_register);

        registerId = findViewById(R.id.registerIdET);
        registerNickNameET = findViewById(R.id.registerNickNameET);
        registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regId = registerId.getText().toString().trim();
                String regNickName = registerNickNameET.getText().toString().trim();

                if ((regId.isEmpty()) && (regNickName.isEmpty())) { //아이디 닉네임 입력란이 비어있지 않다면
                    Toast.makeText(context, "입력란이 비어있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    if (regNickName.length() > 7) {
                        Toast.makeText(context, "닉네임은 7글자를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        String getId = registerId.getText().toString().trim(); //가입 아이디가 널값이 아니라면 일단 문자열로 받아옴
                        String registerNickName = regNickName; //가입 아이디가 널값이 아니라면 일단 문자열로 받아옴

                        firestore.collection("users").document(getId).get() // 데이터베이스에서 중복된 아이디가 있는지 확인
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Toast.makeText(context, "이미 가입된 아이디입니다.", Toast.LENGTH_SHORT).show(); //중복아이디 문구 출력
                                    } else {
                                        // 문서가 존재하지 않을 때만 회원가입 진행
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("registerDate", new Date());  // 첫 가입일자 입력
                                        user.put("userName", registerNickName); //유저이름 삽입

                                        firestore.collection("users").document(getId).set(user); //유저데이터 베이스 생성

                                        HashMap<String, Object> setting = new HashMap<>();
                                        setting.put("ingame", 1.0);
                                        setting.put("preview", 1.0); // 초기 인자값들
                                        setting.put("background", true);

                                        firestore.collection("users")
                                                .document(getId)
                                                .collection("setting")
                                                .document("sound")
                                                .set(setting); // 유저세팅값을 업로드

                                        HashMap<String, Object> sync = new HashMap<>();
                                        sync.put("value", 0); // 싱크값도 넣을준비를 함

                                        firestore.collection("users")
                                                .document(getId)
                                                .collection("setting")
                                                .document("sync")
                                                .set(sync); // 유저싱크값을 업로드

                                        HashMap<String, Object> mode = new HashMap<>();
                                        mode.put("automode", false);
                                        mode.put("gamemode", 0);
                                        mode.put("speed", 3);

                                        firestore.collection("users")
                                                .document(getId)
                                                .collection("setting")
                                                .document("mode")
                                                .set(mode); // 유저모드값을 업로드

                                        LoginActivity.loadSettingDB(getId); //DB값을 설정값으로 설정

                                        Toast.makeText(context, "회원가입 성공. 환영합니다, " + registerNickName + "님", Toast.LENGTH_SHORT).show(); //환영 문구 출력
                                        startIntent(getId, registerNickName);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "회원가입 실패", Toast.LENGTH_SHORT).show(); //회원가입 실패
                                });
                    }
                }
            }
        }); //회원가입
    }


    public void startIntent(String userId, String userName) {
        Intent intent = new Intent(context, MainActivity.class);
        LoginActivity.userId = userId;
        LoginActivity.userName = userName;

        Activity activity = (Activity) context;
        activity.finish();  // 현재 액티비티 종료

        context.startActivity(intent); //인턴트 시작

        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃

        dismiss(); //다이어로그 닫기
    } // 인턴트 시작하는 메소드
}
