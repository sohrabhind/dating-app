package com.hindbyte.dating.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.hindbyte.dating.constants.Constants;

import org.json.JSONObject;


public class Profile extends Application implements Constants, Parcelable {

    private long id;

    private int age = 0, height, state, gender = 3, level, itemsCount, likesCount, imagesCount, allowMessages, lastAuthorize;

    private double distance = 0;

    private int religiousView, viewsOnSmoking, viewsOnAlcohol, youLooking, youLike;

    private int allowShowOnline;

    private String username, fullname, bigPhotoUrl = "", location, interests, bio, lastAuthorizeDate, lastAuthorizeTimeAgo, createDate;

    private Boolean blocked = false;

    private Boolean inBlackList = false;

    private Boolean online = false;

    private Boolean iLiked = false;
    private Boolean myFan = false;

    private String last_visit = "";

    private int photoModerateAt = 0;

    public Profile() {


    }

    public Profile(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setReligiousView(jsonData.getInt("iReligiousView"));
                this.setViewsOnSmoking(jsonData.getInt("iSmokingViews"));
                this.setViewsOnAlcohol(jsonData.getInt("iAlcoholViews"));
                this.setYouLooking(jsonData.getInt("iLooking"));
                this.setYouLike(jsonData.getInt("iInterested"));

                this.setId(jsonData.getLong("id"));
                this.setState(jsonData.getInt("state"));
                this.setGender(jsonData.getInt("gender"));
                this.setUsername(jsonData.getString("username"));
                this.setFullname(jsonData.getString("fullname"));
                this.setLocation(jsonData.getString("location"));
                this.setInterests(jsonData.getString("interests"));
                this.setBio(jsonData.getString("bio"));

                this.setBigPhotoUrl(jsonData.getString("bigPhotoUrl"));

                this.setLikesCount(jsonData.getInt("likesCount"));
                this.setImagesCount(jsonData.getInt("imagesCount"));

                this.setAllowMessages(jsonData.getInt("allowMessages"));

                this.setInBlackList(jsonData.getBoolean("inBlackList"));
                this.setOnline(jsonData.getBoolean("online"));
                this.setBlocked(jsonData.getBoolean("blocked"));
                this.setILike(jsonData.getBoolean("iLiked"));
                this.setIsMyFan(jsonData.getBoolean("myFan"));

                this.setLastActive(jsonData.getInt("lastAuthorize"));
                this.setLastActiveDate(jsonData.getString("lastAuthorizeDate"));
                this.setLastActiveTimeAgo(jsonData.getString("lastAuthorizeTimeAgo"));

                this.setCreateDate(jsonData.getString("createDate"));

                if (jsonData.has("distance")) {

                    this.setDistance(jsonData.getDouble("distance"));
                }

                if (jsonData.has("level")) {

                    this.setLevelMode(jsonData.getInt("level"));
                }

                if (jsonData.has("age")) {

                    this.setAge(jsonData.getInt("age"));
                }

                if (jsonData.has("height")) {

                    this.setHeight(jsonData.getInt("height"));
                }


                if (jsonData.has("allowShowOnline")) {

                    this.setAllowShowOnline(jsonData.getInt("allowShowOnline"));
                }

                if (jsonData.has("photoModerateAt")) {

                    this.setPhotoModerateAt(jsonData.getInt("photoModerateAt"));
                }
            }

        } catch (Throwable t) {

            Log.e("Profile", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Profile", jsonData.toString());
        }
    }


    public void setReligiousView(int religiousView) {

        this.religiousView = religiousView;
    }

    public int getReligiousView() {

        return this.religiousView;
    }


    public void setViewsOnSmoking(int viewsOnSmoking) {

        this.viewsOnSmoking = viewsOnSmoking;
    }

    public int getViewsOnSmoking() {

        return this.viewsOnSmoking;
    }

    public void setViewsOnAlcohol(int viewsOnAlcohol) {

        this.viewsOnAlcohol = viewsOnAlcohol;
    }

    public int getViewsOnAlcohol() {

        return this.viewsOnAlcohol;
    }

    public void setYouLooking(int youLooking) {

        this.youLooking = youLooking;
    }

    public int getYouLooking() {

        return this.youLooking;
    }

    public void setYouLike(int youLike) {

        this.youLike = youLike;
    }

    public int getYouLike() {

        return this.youLike;
    }

    public void setId(long profile_id) {

        this.id = profile_id;
    }

    public long getId() {

        return this.id;
    }

    public void setState(int profileState) {

        this.state = profileState;
    }

    public int getState() {

        return this.state;
    }

    public void setGender(int gender) {

        this.gender = gender;
    }

    public int getGender() {

        return this.gender;
    }

    public void setLevelMode(int levelMode) {

        this.level = levelMode;
    }

    public int getLevelMode() {

        return this.level;
    }


    public void setAge(int age) {

        this.age = age;
    }

    public int getAge() {

        return this.age;
    }

    public void setHeight(int height) {

        this.height = height;
    }

    public int getHeight() {

        return this.height;
    }

    public void setUsername(String profile_username) {

        this.username = profile_username;
    }

    public String getUsername() {

        return this.username;
    }

    public void setFullname(String profile_fullname) {
        this.fullname = profile_fullname;
    }

    public String getFullname() {

        return this.fullname;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    public String getLocation() {

        if (this.location == null) {

            this.location = "";
        }

        return this.location;
    }

    public void setInterests(String interests) {

        this.interests = interests;
    }

    public String getInterests() {

        return this.interests;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public String getBio() {

        return this.bio;
    }

    public void setBigPhotoUrl(String bigPhotoUrl) {

        this.bigPhotoUrl = bigPhotoUrl;
    }

    public String getBigPhotoUrl() {

        if (this.bigPhotoUrl == null) {

            this.bigPhotoUrl = "";
        }

        return this.bigPhotoUrl;
    }


    public void setItemsCount(int itemsCount) {

        this.itemsCount = itemsCount;
    }

    public int getItemsCount() {

        return this.itemsCount;
    }

    public void setLikesCount(int likesCount) {

        this.likesCount = likesCount;
    }

    public int getLikesCount() {

        return this.likesCount;
    }


    public void setImagesCount(int imagesCount) {

        this.imagesCount = imagesCount;
    }

    public int getImagesCount() {

        return this.imagesCount;
    }


    public void setAllowMessages(int allowMessages) {

        this.allowMessages = allowMessages;
    }

    public int getAllowMessages() {

        return this.allowMessages;
    }

    public void setLastActive(int lastAuthorize) {

        this.lastAuthorize = lastAuthorize;
    }

    public int getLastActive() {

        return this.lastAuthorize;
    }

    public void setDistance(double distance) {

        this.distance = distance;
    }

    public double getDistance() {

        return this.distance;
    }

    public void setLastActiveDate(String lastAuthorizeDate) {

        this.lastAuthorizeDate = lastAuthorizeDate;
    }

    public String getLastActiveDate() {

        return this.lastAuthorizeDate;
    }

    public void setCreateDate(String createDate) {

        this.createDate = createDate;
    }

    public String getCreateDate() {

        return this.createDate;
    }


    public void setLastActiveTimeAgo(String lastAuthorizeTimeAgo) {

        this.lastAuthorizeTimeAgo = lastAuthorizeTimeAgo;
    }

    public String getLastActiveTimeAgo() {

        return this.lastAuthorizeTimeAgo;
    }

    public void setBlocked(Boolean blocked) {

        this.blocked = blocked;
    }

    public Boolean isBlocked() {

        return this.blocked;
    }


    public void setOnline(Boolean online) {

        this.online = online;
    }

    public Boolean isOnline() {

        return this.online;
    }

    public void setILike(Boolean iLiked) {

        this.iLiked = iLiked;
    }

    public Boolean isILike() {

        return this.iLiked;
    }

    public void setIsMyFan(Boolean myFan) {

        this.myFan = myFan;
    }

    public Boolean isMyFan() {

        return this.myFan;
    }

    public void setInBlackList(Boolean inBlackList) {

        this.inBlackList = inBlackList;
    }

    public Boolean isInBlackList() {

        return this.inBlackList;
    }

    // Privacy

    public void setAllowShowOnline(int allowShowOnline) {

        this.allowShowOnline = allowShowOnline;
    }

    public int getAllowShowOnline() {

        return this.allowShowOnline;
    }



    // For guests only

    public void setLastVisit(String last_visit) {

        this.last_visit = last_visit;
    }

    public String getLastVisit() {

        if (this.last_visit == null) {

            this.last_visit = "";
        }

        return this.last_visit;
    }

    //

    public void setPhotoModerateAt(int photoModerateAt) {

        this.photoModerateAt = photoModerateAt;
    }

    public int getPhotoModerateAt() {

        return this.photoModerateAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.age);
        dest.writeInt(this.height);
        dest.writeInt(this.state);
        dest.writeInt(this.gender);
        dest.writeInt(this.level);
        dest.writeInt(this.itemsCount);
        dest.writeInt(this.likesCount);
        dest.writeInt(this.imagesCount);
        dest.writeInt(this.allowMessages);
        dest.writeInt(this.lastAuthorize);
        dest.writeDouble(this.distance);
        dest.writeInt(this.religiousView);
        dest.writeInt(this.viewsOnSmoking);
        dest.writeInt(this.viewsOnAlcohol);
        dest.writeInt(this.youLooking);
        dest.writeInt(this.youLike);
        dest.writeInt(this.allowShowOnline);
        dest.writeString(this.username);
        dest.writeString(this.fullname);
        dest.writeString(this.bigPhotoUrl);
        dest.writeString(this.location);
        dest.writeString(this.interests);
        dest.writeString(this.bio);
        dest.writeString(this.lastAuthorizeDate);
        dest.writeString(this.lastAuthorizeTimeAgo);
        dest.writeString(this.createDate);
        dest.writeValue(this.blocked);
        dest.writeValue(this.inBlackList);
        dest.writeValue(this.online);
        dest.writeValue(this.iLiked);
        dest.writeString(this.last_visit);
        dest.writeInt(this.photoModerateAt);
    }

    protected Profile(Parcel in) {
        this.id = in.readLong();
        this.age = in.readInt();
        this.height = in.readInt();
        this.state = in.readInt();
        this.gender = in.readInt();
        this.level = in.readInt();
        this.itemsCount = in.readInt();
        this.likesCount = in.readInt();
        this.imagesCount = in.readInt();
        this.allowMessages = in.readInt();
        this.lastAuthorize = in.readInt();
        this.distance = in.readDouble();
        this.religiousView = in.readInt();
        this.viewsOnSmoking = in.readInt();
        this.viewsOnAlcohol = in.readInt();
        this.youLooking = in.readInt();
        this.youLike = in.readInt();
        this.allowShowOnline = in.readInt();
        this.username = in.readString();
        this.fullname = in.readString();
        this.bigPhotoUrl = in.readString();
        this.location = in.readString();
        this.interests = in.readString();
        this.bio = in.readString();
        this.lastAuthorizeDate = in.readString();
        this.lastAuthorizeTimeAgo = in.readString();
        this.createDate = in.readString();
        this.blocked = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.inBlackList = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.iLiked = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.last_visit = in.readString();
        this.photoModerateAt = in.readInt();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
