package com.example.rhythmproto;

public class UserScore { // 랭킹데이터를 위한 유저점수 데이터모델
    private String userName;
    private int score;
    private String accuracy;

    public UserScore() {
    }

    public UserScore(String userName, int score, String accuracy) {
        this.userName = userName;
        this.score = score;
        this.accuracy = accuracy;
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        return accuracy;
    }
}
