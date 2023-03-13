package com.hindbyte.dating.service;

import android.os.Handler;
import android.os.Looper;

public abstract class AsyncThread extends Thread {

    private Object[] params;
    private Handler mMainHandler;

    public AsyncThread() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    abstract protected void onPreExecute();

    abstract protected void onPostExecute(Object result);

    abstract protected Object doInBackground(Object... params);

    public void execute(Object... params) {
        this.params = params;
        start();
    }

    public void run() {
        mMainHandler.post(this::onPreExecute);
        Object result = null;
        try {
            result = doInBackground(params);
        } finally {
            final Object finalResult = result;
            mMainHandler.post(() -> onPostExecute(finalResult));
        }
    }
}