package com.example.android.sunshine;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by HP on 22-01-2017.
 */

public class WearableDataSend extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private final String mPath = "/weatherData";

    public WearableDataSend() {
        super(WearableDataSend.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mGoogleApiClient = new GoogleApiClient.Builder(WearableDataSend.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // connection failed
        Log.d("CONNECTION", connectionResult.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        // connection suspended
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // get data here to send before starting thread
        long normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday();
        Uri weatherWithDateUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(normalizedUtcStartDay);
        Log.v("tag", "weatherWithDateUri" + weatherWithDateUri);
        Cursor cursor = getContentResolver().query(
                weatherWithDateUri,
                new String[]{
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
                },
                null,
                null,
                null);
        DataMap dataMap = new DataMap();
        int max;
        int min;
        int weatherId;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            max = cursor.getInt(0);
            Log.v("tag","weasendmax" + max);
            min = cursor.getInt(1);
            Log.v("tag","weasendmin" + min);
            weatherId = cursor.getInt(2);
            Log.v("tag","weasendID" + weatherId);
            dataMap.putInt("max", max);
            dataMap.putInt("min", min);
            dataMap.putInt("weatherId", weatherId);
            cursor.close();
        }

        sendDataToWearable(dataMap);
    }

    public void sendDataToWearable(DataMap dataMap) {
        // Construct the data request to send to the wearable over the data layer
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(mPath);
        putDataMapRequest.getDataMap().putAll(dataMap);
        PutDataRequest dataRequest = putDataMapRequest.asPutDataRequest().setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    // successfully sent data
                    Log.v("tag","weasend" + "sent");
                    mGoogleApiClient.disconnect();
                } else {
                    // failed to send data
                    mGoogleApiClient.disconnect();
                    Log.v("tag","weasend" + "not sent");
                }
            }
        });

    }
}
