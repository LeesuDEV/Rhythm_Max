package com.example.rhythmproto;

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
                if (!(registerId.getText().toString().trim().isEmpty()) && !(registerNickNameET.getText().toString().trim().isEmpty())) { //아이디 닉네임 입력란이 비어있지 않다면
                    String getId = registerId.getText().toString().trim(); //가입 아이디가 널값이 아니라면 일단 문자열로 받아옴
                    String registerNickName = registerNickNameET.getText().toString().trim(); //가입 아이디가 널값이 아니라면 일단 문자열로 받아옴

                    firestore.collection("users").document(getId).get() // 데이터베이스에서 중복된 아이디가 있는지 확인
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Toast.makeText(context, "이미 가입된 아이디입니다.", Toast.LENGTH_SHORT).show(); //중복아이디 문구 출력
                                } else {
                                    // 문서가 존재하지 않을 때만 회원가입 진행
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("registerDate", new Date());  // 첫 가입일자 입력
                                    user.put("userName", registerNickName); //유저이름 삽입

                                    firestore.collection("users").document(getId).set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(context, "회원가입 성공. 환영합니다, " + registerNickName + "님", Toast.LENGTH_SHORT).show(); //환영 문구 출력
                                                startIntent(getId, registerNickName);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "회원가입 실패", Toast.LENGTH_SHORT).show(); //회원가입 실패
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "회원가입 실패", Toast.LENGTH_SHORT).show(); //회원가입 실패
                            });
                } else {
                    Toast.makeText(context, "입력란이 비어있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }); //회원가입
    }

    public void startIntent(String userId, String userName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("userId", userId); //유저식별값 인턴트전송
        intent.putExtra("userName", userName); //유저네임 인턴트전송
        context.startActivity(intent); //인턴트 시작
        dismiss(); //다이어로그 닫기
    } // 인턴트 시작하는 메소드
}
