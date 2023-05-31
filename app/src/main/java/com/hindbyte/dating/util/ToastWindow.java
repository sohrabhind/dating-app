package com.hindbyte.dating.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hindbyte.dating.R;

public class ToastWindow {

    private final Handler handlerToast = new Handler(Looper.getMainLooper());
    public PopupWindow pw;

    public void makeText(Context context, int resId, int duration)  {
        makeText(context, context.getResources().getText(resId), duration);
    }

    public void makeText(Context context, CharSequence text, int duration) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View customMenu = inflater.inflate(R.layout.toast_layout, null);
            pw = new PopupWindow(context);
            TextView tv = customMenu.findViewById(R.id.toast_text);
            tv.setText(text);
            pw.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            pw.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            pw.setOutsideTouchable(true);
            pw.setTouchable(false);
            pw.setFocusable(false);
            pw.setContentView(customMenu);
            pw.showAtLocation(customMenu, Gravity.BOTTOM, 0, context.getResources().getDimensionPixelSize(R.dimen.spacing_xlarge));
            handlerToast.postDelayed(pw::dismiss, duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}