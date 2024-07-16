package com.investokar.poppi.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.investokar.poppi.constants.Constants;

import org.json.JSONObject;

public class BalanceItem extends Application implements Constants, Parcelable {

    private long id, fromUserId;
    private int amount, createAt, level;
    private String currency, date, timeAgo;

    public BalanceItem() {

    }

    public BalanceItem(JSONObject jsonData) {

        try {
            this.setId(jsonData.getLong("id"));
            this.setFromUserId(jsonData.getLong("user_id"));
            this.setLevelCount(jsonData.getInt("level"));
            this.setAmount(jsonData.getInt("amount"));
            this.setCurrency(jsonData.getString("currency"));
            this.setTimeAgo(jsonData.getString("timeAgo"));
            this.setDate(jsonData.getString("date"));
            this.setCreateAt(jsonData.getInt("createAt"));
        } catch (Throwable t) {

            Log.e("BalanceItem", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("BalanceItem", jsonData.toString());
        }
    }

    public void setId(long id) {

        this.id = id;
    }

    public long getId() {

        return this.id;
    }

    public void setLevelCount(int level) {

        this.level = level;
    }

    public int getLevelCount() {

        return this.level;
    }

    public void setAmount(int amount) {

        this.amount = amount;
    }

    public int getAmount() {

        return this.amount;
    }

    public void setCurrency(String currency) {

        this.currency = currency;
    }

    public String getCurrency() {

        return this.currency;
    }


    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public long getFromUserId() {

        return this.fromUserId;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }

    public String getTimeAgo() {

        return this.timeAgo;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public String getDate() {

        return this.date;
    }

    public void setCreateAt(int createAt) {

        this.createAt = createAt;
    }

    public int getCreateAt() {

        return this.createAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.fromUserId);
        dest.writeInt(this.level);
        dest.writeInt(this.amount);
        dest.writeString(this.currency);
        dest.writeInt(this.createAt);
        dest.writeString(this.timeAgo);
        dest.writeString(this.date);
    }

    protected BalanceItem(Parcel in) {
        this.id = in.readLong();
        this.fromUserId = in.readLong();
        this.level = in.readInt();
        this.amount = in.readInt();
        this.currency = in.readString();
        this.createAt = in.readInt();
        this.timeAgo = in.readString();
        this.date = in.readString();
    }

    public static final Creator<BalanceItem> CREATOR = new Creator<BalanceItem>() {
        @Override
        public BalanceItem createFromParcel(Parcel source) {
            return new BalanceItem(source);
        }

        @Override
        public BalanceItem[] newArray(int size) {
            return new BalanceItem[size];
        }
    };
}

