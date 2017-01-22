package com.example.shreyaprabhu.wear;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by HP on 22-01-2017.
 */

public class WeatherDataListener extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/weatherData";

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        DataMap dataMap;

        for (DataEvent event : dataEventBuffer) {

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {
                }
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v("myTag", "DataMap received on watch: " + dataMap);
            }
        }
    }
}
