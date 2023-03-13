package com.hindbyte.dating.model;

import android.app.Application;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.hindbyte.dating.constants.Constants;

public class Settings extends Application implements Constants, Parcelable {

    private int defaultProModeCost = 170, defaultMessagesPackageCost = 20;
    private Boolean allowShowNotModeratedProfilePhotos = true;

    public Settings() {

    }


    public void setProModeCost(int defaultProModeCost) {

        this.defaultProModeCost = defaultProModeCost;
    }

    public int getProModeCost() {

        return this.defaultProModeCost;
    }

    public void setMessagePackageCost(int defaultMessagesPackageCost) {

        this.defaultMessagesPackageCost = defaultMessagesPackageCost;
    }

    public int getMessagePackageCost() {

        return this.defaultMessagesPackageCost;
    }

    public void setAllowShowNotModeratedProfilePhotos(Boolean allowShowNotModeratedProfilePhotos) {

        this.allowShowNotModeratedProfilePhotos = allowShowNotModeratedProfilePhotos;
    }

    public Boolean isAllowShowNotModeratedProfilePhotos() {
        return this.allowShowNotModeratedProfilePhotos;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.defaultProModeCost);
        dest.writeInt(this.defaultMessagesPackageCost);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(this.allowShowNotModeratedProfilePhotos);
        }
    }

    protected Settings(Parcel in) {
        this.defaultProModeCost = in.readInt();
        this.defaultMessagesPackageCost = in.readInt();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.allowShowNotModeratedProfilePhotos = in.readBoolean();
        }
    }

    public static final Creator<Settings> CREATOR = new Creator<Settings>() {
        @Override
        public Settings createFromParcel(Parcel source) {
            return new Settings(source);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };
}
