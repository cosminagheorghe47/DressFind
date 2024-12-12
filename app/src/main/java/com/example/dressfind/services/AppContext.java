package com.example.dressfind.services;

import android.content.Context;

public class AppContext {
    private static Context context;

    public static void setContext(Context context) {
        AppContext.context = context;
    }

    public static Context getContext() {
        return context;
    }
}