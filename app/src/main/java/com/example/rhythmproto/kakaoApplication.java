package com.example.rhythmproto;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class kakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "6176465f065a1cf0a36ef5a99dcfdf47");
    }
}
