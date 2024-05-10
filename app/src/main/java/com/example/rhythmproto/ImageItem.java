package com.example.rhythmproto;

public class ImageItem {
    private int imageResource; // 이미지 리소스 ID
    private String description; // 곡 제목
    private String difficulty; // 곡 난이도

    public ImageItem (int imageResource,String description,String difficulty) {
        this.imageResource = imageResource;
        this.description = description;
        this.difficulty = difficulty;
    }

    public int getImageResource() {
        return imageResource;
    } //이미지 리소스 반환

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
