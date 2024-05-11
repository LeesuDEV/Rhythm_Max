package com.example.rhythmproto;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private View loginButton,startup;
    private TextView nickName;

    private TextView loginTV,registerTV;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        loginButton = findViewById(R.id.login);
        nickName = findViewById(R.id.nickname);
        startup = findViewById(R.id.startup);

        loginTV = findViewById(R.id.simpleAccountLogin);
        registerTV = findViewById(R.id.simpleAccountRegister);

        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginDialog dialog = new LoginDialog(LoginActivity.this);
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //다이어로그 배경 투명처리
            }
        }); // 로그인 다이어로그
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterDialog dialog = new RegisterDialog(LoginActivity.this);
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //다이어로그 배경 투명처리
            }
        }); // 회원가입 다이어로그

        // 카카오가 설치되어 있는지 확인 하는 메서드또한 카카오에서 제공 콜백 객체를 이용함
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
                if (oAuthToken != null) {

                }
                if (throwable != null) {

                }
                updateKakaoLoginUi();
                return null;
            }
        };
        // 로그인 버튼
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
            }
        }); //카카오 최초로그인 버튼

        updateKakaoLoginUi();
    }

    private void updateKakaoLoginUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어있으면
                if (user != null) {  // 유저데이터가 있다면
                    nickName.setText(user.getKakaoAccount().getProfile().getNickname() + " 님"); //닉네임 텍스트에 유저이름 설정
                    loginButton.setVisibility(View.GONE); //카카오 로그인버튼 비활성화
                    startup.setVisibility(View.VISIBLE); //해당계정으로 시작 버튼 활성화
                    startup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveUserDataIfNotExists(String.valueOf(
                                    user.getId()), //유저 식별아이디
                                    user.getKakaoAccount().getProfile().getNickname() //유저닉네임
                            ); //유저id를 토대로 데이터베이스 생성 - 이미 있다면 로그인만 진행함.
                        }
                    });
                } else {
                    // 로그인이 되어 있지 않다면 위와 반대로
                    nickName.setText(null);
                    loginButton.setVisibility(View.VISIBLE);
                }
                return null;
            }
        });

    }

    public void saveUserDataIfNotExists(String userId,String userName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 'users' 컬렉션에서 userId에 해당하는 문서 참조
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startIntent(userId,userName); //인턴트 시작 메소드
                        Toast.makeText(LoginActivity.this,"환영합니다,"+documentSnapshot.getString("userName")+"님",Toast.LENGTH_SHORT).show(); //환영문구 출력
                    } else {
                        // 문서가 존재하지 않을 때만 새 데이터 저장
                        Map<String, Object> user = new HashMap<>();
                        user.put("registerDate", new Date());  // 첫 가입일자 입력
                        user.put("userName",userName); //유저이름 삽입

                        db.collection("users").document(userId).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "사용자 데이터 저장 성공");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "사용자 데이터 저장 실패", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "사용자 데이터 확인 실패", e);
                });
    }

    public void startIntent(String userId,String userName){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("userId",userId); //유저식별값 인턴트전송
        intent.putExtra("userName",userName); //유저네임 인턴트전송
        startActivity(intent); //인턴트 시작
        finish();
    } // 인턴트 시작하는 메소드

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