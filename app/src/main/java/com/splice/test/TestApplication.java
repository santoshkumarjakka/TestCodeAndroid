package com.splice.test;

import android.app.Application;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ConnectionQuality;
import com.androidnetworking.interfaces.ConnectionQualityChangeListener;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static android.content.ContentValues.TAG;

/**
 * Created by Santosh Jakka on 20-01-2018.
 */

public class TestApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
      .retryOnConnectionFailure(false)
      .connectTimeout(18000, TimeUnit.SECONDS)
      .readTimeout(18000, TimeUnit.SECONDS)
      .writeTimeout(18000, TimeUnit.SECONDS)
      .build();
    AndroidNetworking.initialize(getApplicationContext(), okHttpClient);
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPurgeable = true;
    AndroidNetworking.setBitmapDecodeOptions(options);
    AndroidNetworking.enableLogging();
    AndroidNetworking.setConnectionQualityChangeListener(new ConnectionQualityChangeListener() {
      @Override
      public void onChange(ConnectionQuality currentConnectionQuality, int currentBandwidth) {
        Log.d(TAG, "onChange: currentConnectionQuality : " + currentConnectionQuality + " currentBandwidth : " + currentBandwidth);
      }
    });
  }
}
