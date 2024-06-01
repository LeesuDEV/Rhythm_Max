package com.example.rhythmproto;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private View loginButton, startup;
    TextView logOutBtn;
    private TextView nickName;
    static String userId; //유저 아이디 전역변수
    static String userName; //유저 닉네임 전역변수

    private ImageView loginTV, registerTV,titleCircle;
    FrameLayout kakaoLayout,simpleLoginForm;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // 파이어스토어 인스턴스 참조

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        titleCircle = findViewById(R.id.titleCircle);
        loginButton = findViewById(R.id.login);
        logOutBtn = findViewById(R.id.logOutBtn);
        nickName = findViewById(R.id.nickname);
        startup = findViewById(R.id.startup);
        kakaoLayout = findViewById(R.id.kakaoLayout);
        simpleLoginForm = findViewById(R.id.simpleLoginForm);

        loginTV = findViewById(R.id.simpleAccountLogin);
        registerTV = findViewById(R.id.simpleAccountRegister);

        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(8000);
        rotate.setRepeatCount(Animation.INFINITE);
        titleCircle.startAnimation(rotate);


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

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().logout(throwable -> {
                    if (throwable != null) {
                        //로그아웃 실패
                        Log.e("Failed Logout Kakao",String.valueOf(throwable));
                    } else {
                        //로그아웃 성공
                        Log.d("Success","로그아웃 성공");

                        nickName.setText(null);

                        simpleLoginForm.setAlpha(0f);
                        simpleLoginForm.setVisibility(View.VISIBLE);

                        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(simpleLoginForm, "alpha", 0f, 1f);
                        alphaAnimator.setDuration(1500);
                        alphaAnimator.start();

                        kakaoLayout.setVisibility(View.GONE);
                    }
                    return null;
                });
            }
        });

        updateKakaoLoginUi();
    }

    private void updateKakaoLoginUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어있으면
                if (user != null) {  // 유저데이터가 있다면
                    nickName.setText(user.getKakaoAccount().getProfile().getNickname() + " 님"); //닉네임 텍스트에 유저이름 설정
                    simpleLoginForm.setVisibility(View.GONE);

                    kakaoLayout.setAlpha(0f);
                    kakaoLayout.setVisibility(View.VISIBLE);

                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(kakaoLayout, "alpha", 0f, 1f);
                    alphaAnimator.setDuration(1500);
                    alphaAnimator.start();

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
                    simpleLoginForm.setAlpha(0f);
                    simpleLoginForm.setVisibility(View.VISIBLE);

                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(simpleLoginForm, "alpha", 0f, 1f);
                    alphaAnimator.setDuration(1500);
                    alphaAnimator.start();

                    kakaoLayout.setVisibility(View.GONE);
                }
                return null;
            }
        });

    }

    public void saveUserDataIfNotExists(String userId, String userName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 'users' 컬렉션에서 userId에 해당하는 문서 참조
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startIntent(userId, userName); //인턴트 시작 메소드
                        loadSettingDB(userId); // 세팅정보 불러오기
                        //Toast.makeText(LoginActivity.this, "환영합니다," + documentSnapshot.getString("userName") + "님", Toast.LENGTH_SHORT).show(); //환영문구 출력
                    } else {
                        // 문서가 존재하지 않을 때만 새 데이터 저장
                        Map<String, Object> user = new HashMap<>();
                        user.put("registerDate", new Date());  // 첫 가입일자 입력
                        user.put("userName", userName); //유저이름 삽입

                        firestore.collection("users").document(userId).set(user); //유저데이터 베이스 생성

                        HashMap<String, Object> setting = new HashMap<>();
                        setting.put("ingame", 1.0);
                        setting.put("preview", 1.0); // 초기 인자값들

                        firestore.collection("users")
                                .document(userId)
                                .collection("setting")
                                .document("sound")
                                .set(setting, SetOptions.merge()); // 유저세팅값을 업로드

                        HashMap<String, Object> sync = new HashMap<>();
                        sync.put("value", 0); // 싱크값도 넣을준비를 함

                        firestore.collection("users")
                                .document(userId)
                                .collection("setting")
                                .document("sync")
                                .set(sync, SetOptions.merge()); // 유저싱크값을 업로드

                        HashMap<String, Object> mode = new HashMap<>();
                        mode.put("automode", false);
                        mode.put("gamemode", 0);
                        mode.put("speed", 3);

                        firestore.collection("users")
                                .document(userId)
                                .collection("setting")
                                .document("mode")
                                .set(mode, SetOptions.merge()); // 유저모드값을 업로드

                        loadSettingDB(userId); //DB값을 설정값으로 설정

                        //Toast.makeText(LoginActivity.this, "회원가입 성공. 환영합니다, " + userName + "님", Toast.LENGTH_SHORT).show(); //환영 문구 출력
                        startIntent(userId, userName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "사용자 데이터 확인 실패", e);
                });
    }

    public void startIntent(String userId, String userName) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        this.userId = userId;
        this.userName = userName;

        startActivity(intent); //인턴트 시작

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 2초동안 페이드인아웃

        finish();
    } // 인턴트 시작하는 메소드

    public static void loadSettingDB(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(userId)
                .collection("setting")
                .document("sound")
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MainActivity.previewSoundAmountIndex = documentSnapshot.getDouble("preview").floatValue();
                        MainActivity.ingameSoundAmountIndex = documentSnapshot.getDouble("ingame").floatValue();
                        MainActivity.backgroundIndex = documentSnapshot.getBoolean("background").booleanValue();
                    }
                }); // 서버에서 프리뷰와 인게임볼륨을 받아옴

        firestore.collection("users")
                .document(userId)
                .collection("setting")
                .document("sync")
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MainActivity.syncValue = documentSnapshot.getLong("value").intValue();
                    }
                }); // 서버에서 싱크값을 받아옴

        firestore.collection("users")
                .document(userId)
                .collection("setting")
                .document("mode")
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MainActivity.autoModIndex = documentSnapshot.getBoolean("automode").booleanValue();
                        MainActivity.gameModIndex = documentSnapshot.getLong("gamemode").intValue();
                        MainActivity.speedIndex = documentSnapshot.getLong("speed").intValue();
                    }
                }); // 서버에서 오토모드,모드,스피드 인덱스값 받아옴
    } //DB에서 설정값을 불러오는 메소드
}