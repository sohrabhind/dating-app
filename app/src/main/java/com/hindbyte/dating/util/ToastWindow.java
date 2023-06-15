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
import android.widget.Toast;

import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;

public class ToastWindow {

    public Toast toast;

    public void makeText(int resId, int duration)  {
        makeText(App.getInstance().getResources().getText(resId), duration);
    }

    public void makeText(CharSequence text, int duration) {
        try {
            LayoutInflater inflater = (LayoutInflater) App.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customMenu = inflater.inflate(R.layout.toast_layout, null);
            TextView tv = customMenu.findViewById(R.id.toast_text);
            tv.setText(text);
            toast = new Toast(App.getInstance());
            toast.setGravity(Gravity.BOTTOM, 0,  App.getInstance().getResources().getDimensionPixelSize(R.dimen.spacing_xxlarge));
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(customMenu);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}