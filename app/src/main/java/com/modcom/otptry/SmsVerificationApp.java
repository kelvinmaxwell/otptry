package com.modcom.otptry;

import android.app.Application;

public class SmsVerificationApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        appSignatureHelper.getAppSignatures();
    }
}
