package com.example.seekbarrecycler;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class ScrollbarException extends RuntimeException{
    public ScrollbarException() {
    }

    public ScrollbarException(String message) {
        super(message);
    }

    public ScrollbarException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrollbarException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ScrollbarException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
