/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.shreyaprabhu.wear;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.TextPaint;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.example.shreyaprabhu.wear.R.drawable.ic_fog;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 */
public class SunshineWatchFace extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        return new SunshineWatchFaceEngine();
    }

    private class SunshineWatchFaceEngine extends Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private final String TAG = SunshineWatchFaceEngine.class.getSimpleName();

        private final String WEARABLE_DATA_PATH = "/weatherData";

        Calendar mCalendar;

        Paint mBackgroundPaint;
        TextPaint mTimeTextPaint;
        TextPaint mDateTextPaint;
        TextPaint mHighTextPaint;
        TextPaint mLowTextPaint;


        SimpleDateFormat timeFormat;
        SimpleDateFormat dateFormat;

        String mHigh = "";
        String mLow = "";
        int mWeatherId = 0;

        Bitmap mWeatherImage;

        Resources mResources;

        boolean mRegisteredReceiver = false;

        private final Typeface BOLD_TYPEFACE =
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

        final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // logic to update when time zone changes or when new weather data is received
                Log.d(TAG, "Received broadcast");
            }
        };

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(SunshineWatchFace.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(getColor(R.color.colorPrimary));

            // time text configuration
            mTimeTextPaint = new TextPaint();
            mTimeTextPaint.setColor(Color.WHITE);
            mTimeTextPaint.setTypeface(BOLD_TYPEFACE);
            mTimeTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.time_size));
            mTimeTextPaint.setAntiAlias(true);

            // date text configuration
            mDateTextPaint = new TextPaint();
            mDateTextPaint.setColor(Color.WHITE);
            mDateTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.date_size));
            mDateTextPaint.setAntiAlias(true);

            // high/low text configuration
            mHighTextPaint = new TextPaint();
            mLowTextPaint = new TextPaint();
            mHighTextPaint.setColor(Color.WHITE);
            mLowTextPaint.setColor(Color.WHITE);
            mHighTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.weather_size));
            mHighTextPaint.setAntiAlias(true);
            mLowTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.weather_size));
            mLowTextPaint.setAntiAlias(true);

            mCalendar = Calendar.getInstance();

            mResources = getApplicationContext().getResources();

            initFormats();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {

            Log.d(TAG,"weachange" + "Data changed event");

            DataMap dataMap;

            for (DataEvent event : dataEventBuffer) {

                // Check the data type
                boolean type =event.getType() == DataEvent.TYPE_CHANGED;
                Log.d(TAG,"weatype" + type);
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    // Check the data path
                    String path = event.getDataItem().getUri().getPath();
                    if (path.equals(WEARABLE_DATA_PATH)) {
                        Log.d(TAG,"weapath" + path);
                    }
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    mHigh = String.valueOf(dataMap.getInt("max"));
                    Log.d(TAG,"weamaxset" + mHigh);
                    mLow = String.valueOf(dataMap.getInt("min"));
                    Log.d(TAG,"weaminset" + mLow);
                    mWeatherId = dataMap.getInt("weatherId");
                    Log.d(TAG,"weaidset" + mWeatherId);
                    invalidate();
                }
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            String timeString = timeFormat.format(now);
            Rect textBounds = new Rect();
            mTimeTextPaint.getTextBounds(timeString, 0, timeString.length(), textBounds);

            int textX = Math.abs(bounds.centerX() - textBounds.centerX());
            int textY = Math.abs(bounds.centerY() / 2 - textBounds.centerY());

            canvas.drawText(timeString, textX, textY, mTimeTextPaint);

            String dateString = dateFormat.format(now);
            Rect dateTextBounds = new Rect();
            mDateTextPaint.getTextBounds(dateString, 0, dateString.length(), dateTextBounds);
            int dateTextX = Math.abs(bounds.centerX() - dateTextBounds.centerX());
            int dateTextY = Math.abs(textY + dateTextBounds.height() + 10);

            canvas.drawText(dateString, dateTextX, dateTextY, mDateTextPaint);

            if(mWeatherId!=0 && mHigh!="" && mLow!= ""){
                int resHigh = mHigh.indexOf((char) 0x00B0);
                if(resHigh<0){
                    mHigh = mHigh+ (char) 0x00B0;
                }
                Rect highTextBounds = new Rect();
                mHighTextPaint.getTextBounds(mHigh, 0, mHigh.length(), highTextBounds);
                int highTextX = Math.abs(bounds.centerX() + 35);
                int highTextY = Math.abs(dateTextY + highTextBounds.height() + 50);
                canvas.drawText(mHigh, highTextX, highTextY, mHighTextPaint);

                int resLow = mLow.indexOf((char) 0x00B0);
                if(resLow<0){
                    mLow = mLow+ (char) 0x00B0;
                }
                Rect lowTextBounds = new Rect();
                mLowTextPaint.getTextBounds(mLow, 0, mLow.length(), lowTextBounds);
                int lowTextX = Math.abs(bounds.centerX() - lowTextBounds.centerX());
                int lowTextY = Math.abs(dateTextY + lowTextBounds.height() + 50);
                canvas.drawText(mLow, lowTextX, lowTextY, mLowTextPaint);


                int weatherImageX = bounds.centerX() - lowTextBounds.centerX() - 110;
                int weatherImageY = Math.abs(dateTextY + lowTextBounds.height());
                mWeatherImage = BitmapFactory.decodeResource(mResources, WearableWeatherUtilities.getWeatherResource(mWeatherId));
                canvas.drawBitmap(mWeatherImage, weatherImageX, weatherImageY, null);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();

                mCalendar.setTimeZone(TimeZone.getDefault());
                initFormats();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }
        }

        private void registerReceiver() {
            if (mRegisteredReceiver) {
                return;
            }

            mRegisteredReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            SunshineWatchFace.this.registerReceiver(mBroadcastReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredReceiver) {
                return;
            }
            mRegisteredReceiver = false;
            SunshineWatchFace.this.unregisterReceiver(mBroadcastReceiver);
        }

        private void initFormats() {
            dateFormat = new SimpleDateFormat("EEEE, MMM. d yyyy", Locale.getDefault());
            timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        }



        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, SunshineWatchFaceEngine.this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            // handle suspended connection
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // handle failure
        }
    }

}
