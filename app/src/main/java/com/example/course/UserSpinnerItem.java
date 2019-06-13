package com.example.course;

public class UserSpinnerItem {
    private String mUserName;
    private String mUserID;
    private int mImage;
    private String mUrl;

    public UserSpinnerItem(String userName, String userID, int image){
        mUserName = userName;
        mUserID = userID;
        mImage = image;
        mUrl = null;
    }

    public UserSpinnerItem(String userName, String userID){
        mUserName = userName;
        mUserID = userID;
    }

    public UserSpinnerItem(String userName, String userID, String url){
        mUserName = userName;
        mUserID = userID;
        mUrl = url;
    }

    public String getUserName(){
        return mUserName;
    }

    public int getImage() {
        return mImage;
    }

    public String getUserID(){
        return mUserID;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
