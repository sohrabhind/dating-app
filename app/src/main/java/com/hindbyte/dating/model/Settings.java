package com.hindbyte.dating.model;

import android.app.Application;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.hindbyte.dating.constants.Constants;

public class Settings extends Application implements Constants, Parcelable {


    private Boolean allowShowNotModeratedProfilePhotos = true;

    public Settings() {

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(this.allowShowNotModeratedProfilePhotos);
        }
    }

    protected Settings(Parcel in) {
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
