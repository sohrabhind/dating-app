package com.hindbyte.dating.util;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper extends Application {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    private AppCompatActivity activity;
    private Context context;

    public Helper(Context current) {
        this.context = current;
    }

    public Helper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public Helper(FragmentActivity activity) {

    }

    private Bitmap resizeImg(Uri filename) throws IOException {
        int maxWidth = 1200;
        int maxHeight = 1200;

        // create the options
        BitmapFactory.Options opts = new BitmapFactory.Options();
        BitmapFactory.decodeFile(getRealPath(filename), opts);

        //get the original size
        int orignalHeight = opts.outHeight;
        int orignalWidth = opts.outWidth;

        //opts = new BitmapFactory.Options();
        //just decode the file
        opts.inJustDecodeBounds = true;

        //initialization of the scale
        int resizeScale = 1;
        Log.e("qascript orignalWidth", String.valueOf(orignalWidth));
        Log.e("qascript orignalHeight", String.valueOf(orignalHeight));

        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {
            resizeScale = 2;
        }

        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale;
        opts.inJustDecodeBounds = false;
        //get the future size of the bitmap
        int bmSize = 6000;
        //check if it's possible to store into the vm java the picture
        if (Runtime.getRuntime().freeMemory() > bmSize) {
            //decode the file
            InputStream is = this.context.getContentResolver().openInputStream(filename);
            Bitmap bp = BitmapFactory.decodeStream(is, new Rect(0, 0, 512, 512), opts);
            is.close();

            return bp;
        } else {
            Log.e("qascript", "not resize image");
            return null;
        }
    }

    public static String getRealPath(Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        Uri contentUri;

        switch (type) {
            case "image":
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "audio":
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            default:
                contentUri = MediaStore.Files.getContentUri("external");
        }

        String selection = "_id=?";
        String[] selectionArgs = new String[]{split[1]};

        return getDataColumn(App.getInstance().getApplicationContext(), contentUri, selection, selectionArgs);
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                String value = cursor.getString(column_index);
                if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith("file://")) {
                    return null;
                }
                return value;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static String getGenderTitle(Context ctx, int gender) {
        switch (gender) {
            case 0: {
                return ctx.getString(R.string.action_choose_gender) + ": " + ctx.getString(R.string.label_male);
            }
            case 1: {
                return ctx.getString(R.string.action_choose_gender) + ": " + ctx.getString(R.string.label_female);
            }
            case 2: {
                return ctx.getString(R.string.action_choose_gender) + ": " + ctx.getString(R.string.label_other);
            }
            default: {
                return ctx.getString(R.string.label_select_gender);
            }
        }
    }

    public static int getGalleryGridCount(FragmentActivity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.gallery_item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static int getGridSpanCount(FragmentActivity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static int getStickersGridSpanCount(FragmentActivity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.sticker_item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public boolean isValidEmail(String email) {
    	if (TextUtils.isEmpty(email)) {
    		return false;
    	} else {
    		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    	}
    }

    public boolean isValidLogin(String login) {
        String regExpn = "^[a-zA-Z0-9_-]{4,64}$";
        CharSequence inputStr = login;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();
    }

    public boolean isValidSearchQuery(String query) {
        String regExpn = "^[a-zA-Z0-9_-]{1,64}$";
        CharSequence inputStr = query;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        return matcher.matches();
    }
    
    public boolean isValidPassword(String password) {
        String regExpn = "^.{6,64}$";
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    public String getReligiousView(int mWorld) {
        switch (mWorld) {
            case 0: {
                return "-";
            }
            case 1: {
                return context.getString(R.string.religious_view_1);
            }
            case 2: {
                return context.getString(R.string.religious_view_2);
            }
            case 3: {
                return context.getString(R.string.religious_view_3);
            }
            case 4: {
                return context.getString(R.string.religious_view_4);
            }
            case 5: {
                return context.getString(R.string.religious_view_5);
            }
            case 6: {
                return context.getString(R.string.religious_view_6);
            }
            case 7: {
                return context.getString(R.string.religious_view_7);
            }
            case 8: {
                return context.getString(R.string.religious_view_8);
            }
            default: {
                break;
            }
        }

        return "-";
    }


    public String getSmokingViews(int mSmoking) {
        switch (mSmoking) {
            case 0: {
                return "-";
            }
            case 1: {
                return context.getString(R.string.smoking_views_1);
            }
            case 2: {
                return context.getString(R.string.smoking_views_2);
            }
            case 3: {
                return context.getString(R.string.smoking_views_3);
            }
            case 4: {
                return context.getString(R.string.smoking_views_4);
            }
            case 5: {
                return context.getString(R.string.smoking_views_5);
            }
            default: {
                break;
            }
        }
        return "-";
    }

    public String getAlcoholViews(int mAlcohol) {
        switch (mAlcohol) {
            case 0: {
                return "-";
            }

            case 1: {
                return context.getString(R.string.alcohol_views_1);
            }

            case 2: {
                return context.getString(R.string.alcohol_views_2);
            }

            case 3: {
                return context.getString(R.string.alcohol_views_3);
            }

            case 4: {
                return context.getString(R.string.alcohol_views_4);
            }

            case 5: {
                return context.getString(R.string.alcohol_views_5);
            }

            default: {
                break;
            }
        }

        return "-";
    }

    public String getLooking(int mLooking) {
        switch (mLooking) {
            case 0: {
                return "-";
            }

            case 1: {
                return context.getString(R.string.you_looking_1);
            }

            case 2: {
                return context.getString(R.string.you_looking_2);
            }

            case 3: {
                return context.getString(R.string.you_looking_3);
            }

            default: {
                break;
            }
        }

        return "-";
    }

    public String getGenderLike(int mLike) {
        switch (mLike) {
            case 0: {
                return "-";
            }
            case 1: {
                return context.getString(R.string.profile_like_1);
            }
            case 2: {
                return context.getString(R.string.profile_like_2);
            }
            default: {
                break;
            }
        }
        return "-";
    }

}