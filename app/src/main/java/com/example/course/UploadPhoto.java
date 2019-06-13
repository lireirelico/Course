package com.example.course;

public class UploadPhoto {
    private String mName;
    private String mImageUrl;

    public UploadPhoto()  {}

    public UploadPhoto(String name, String imageUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public String getmName() {
        return mName;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}
