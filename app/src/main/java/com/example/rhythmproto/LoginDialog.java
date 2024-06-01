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

public class LoginDialog extends Dialog {
    EditText loginId;
    Button loginBtn;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조
    Context context;

    public LoginDialog(Context context){
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_login);

        loginId = findViewById(R.id.insertIdET);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(loginId.getText().toString().trim().isEmpty())) {
                    String getId = loginId.getText().toString().trim(); //로그인 아이디가 널값이 아니라면 일단 문자열로 받아옴

                    firestore.collection("users").document(getId).get() // 데이터베이스에서 중복된 아이디가 있는지 확인
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()){
                                    startIntent(getId,documentSnapshot.getString("userName")); //인턴트 시작 메소드
                                    LoginActivity.loadSettingDB(getId); //DB값을 설정값으로 설정
                                    Toast.makeText(context,"환영합니다,"+documentSnapshot.getString("userName")+"님",Toast.LENGTH_SHORT).show(); //환영문구 출력
                                } else {
                                    Toast.makeText(context,"존재하지 않는 아이디입니다.",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context,"데이터를 불러오는데 실패했습니다.",Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(context,"ID입력란이 비어있습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void startIntent(String userId,String userName){
        Intent intent = new Intent(context,MainActivity.class);
        LoginActivity.userId = userId;
        LoginActivity.userName = userName;

        Activity activity = (Activity) context;
        activity.finish();  // 현재 액티비티 종료

        context.startActivity(intent); //인턴트 시작

        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃

        dismiss(); //다이어로그 닫기
    } // 인턴트 시작하는 메소드
}
