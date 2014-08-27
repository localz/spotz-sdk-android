package com.localz.spotz.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.models.ActivityType;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.ActivityReportPostRequest;
import com.localz.spotz.api.models.request.v1.SpotzGetRequest;
import com.localz.spotz.api.models.response.v1.ActivityReportPostResponse;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.sdk.api.ActivityReportTask;
import com.localz.spotz.sdk.api.GetSpotzTask;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OnBeaconDiscoveryFinishedReceiver extends BroadcastReceiver {
    private static final String BROADCAST = ".SPOTZ_ON_SPOT_EXIT";

    private static final String TAG = OnBeaconDiscoveryFinishedReceiver.class.getSimpleName();
    private static Set<String> previousScanSpotz = new HashSet<String>();
    private static Set<String> previousScanVisibleBeacons = new HashSet<String>();
    private static SharedPreferences sharedPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {
        List<BleData> bleDatas = (ArrayList<BleData>) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

        Set<String> currentScanVisibleBeacons = new HashSet<String>();

        String eventId = getRandonEventId();

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Spotz.class.getName(), Context.MODE_PRIVATE);
        }

        if (bleDatas != null) {
            // App is already initialized, let's rock and roll
            if (Spotz.getInstance().isInitialized()) {
                Map<BleData, String> cachedSpotzIdMap = Spotz.getInstance().getCachedSpotzIdMap();
                Set<String> currentScanSpotz = new HashSet<String>();
                for (BleData bleData : bleDatas) {
                    final String spotzId = cachedSpotzIdMap.get(bleData);
                    if (!TextUtils.isEmpty(spotzId)) {
                        currentScanSpotz.add(spotzId);
                    }

                    //currentScanVisibleBeacons.add
                    SpotzGetResponse spotzDataForTheBeacon = Spotz.getInstance().getCachedSpotzMap().get(spotzId);
                    if (spotzDataForTheBeacon != null) {
                        for (SpotzGetResponse.Beacon b : spotzDataForTheBeacon.beacons) {
                            if (b.uuid.equalsIgnoreCase(bleData.uuid) && (b.major == bleData.major) && (b.minor == bleData.minor)) {
                                ActivityType activityType = ActivityType.ENTRY;
                                if (previousScanVisibleBeacons.contains(b.beaconId)) {
                                    // we saw beacon in previous scan, there no really event we are interested in
                                } else {
                                    // we have not see this beacon in the previous scan. This is either ENTRY or INSPOTZ event
                                    if (previousScanSpotz.contains(spotzDataForTheBeacon._id)) {
                                        // on previous scan the spotz was visible, hence this is "In spotz" event
                                        activityType = ActivityType.INSPOTZ;
                                    }
                                    // b is the beacon that we see now, get b.beaconId and send it to server's activity log
                                    ActivityReportPostRequest request = new ActivityReportPostRequest(new Date(), activityType.getName(),
                                            eventId, b.beaconId, null);
                                    new ActivityReportTask(new ResponseListenerAdapter<ActivityReportPostResponse>() {
                                        @Override
                                        public void onSuccess(Response<ActivityReportPostResponse> response) {
                                            super.onSuccess(response);
                                            // do nothing.......
                                        }

                                        @Override
                                        public void onError(Response<ActivityReportPostResponse> response, Exception exception) {
                                            super.onError(response, exception);
                                            // do nothing....... in future will probably be caching..... and resending later
                                        }
                                    }).execute(request);
                                }
                                currentScanVisibleBeacons.add(b.beaconId);
                                break;
                            }
                        }
                    }


                }

                previousScanSpotz.removeAll(currentScanSpotz);
                previousScanVisibleBeacons.removeAll(currentScanVisibleBeacons);

                final HashSet<String> beaconsFromExitedSpotz = new HashSet<String>();

                for (final String exitedSpotzId : previousScanSpotz) {
                    Map<String, SpotzGetResponse> cachedSpotzMap = Spotz.getInstance().getCachedSpotzMap();
                    SpotzGetResponse spot = cachedSpotzMap.get(exitedSpotzId);
                    if (spot == null) {
                        SpotzGetRequest request = new SpotzGetRequest(exitedSpotzId);
                        new GetSpotzTask(new ResponseListenerAdapter<SpotzGetResponse>() {
                            @Override
                            public void onSuccess(Response<SpotzGetResponse> response) {
                                Spotz.getInstance().getCachedSpotzMap().put(exitedSpotzId, response.data);

                                // add all beacons from the spotz we exited, to the list
                                beaconsFromExitedSpotz.addAll(beaconIdFromSpotz(response.data));

                                outSpot(context, response.data);
                            }
                        }).execute(request);
                    } else {
                        // add all beacons from the spotz we exited, to the list
                        beaconsFromExitedSpotz.addAll(beaconIdFromSpotz(spot));
                        outSpot(context, spot);
                    }
                }


                // looking at the previousScanVisibleBeacons (without currentlyVisibleBeacons)
                // if beacon's spotz is not visible any more, then we have EXIT event,
                // otherwise EXITED_BEACON_INSPOTZ event
                for (String exitedBeaconId : previousScanVisibleBeacons) {
                    ActivityType activityType = ActivityType.EXITED_BEACON_INSPOTZ;

                    if (beaconsFromExitedSpotz.contains(exitedBeaconId)) {
                        // we exited spotz
                        activityType = ActivityType.EXIT;
                    }


                    ActivityReportPostRequest request = new ActivityReportPostRequest(new Date(), activityType.getName(),
                            eventId, exitedBeaconId, null);
                    new ActivityReportTask(new ResponseListenerAdapter<ActivityReportPostResponse>() {
                        @Override
                        public void onSuccess(Response<ActivityReportPostResponse> response) {
                            super.onSuccess(response);
                            // do nothing.......
                        }

                        @Override
                        public void onError(Response<ActivityReportPostResponse> response, Exception exception) {
                            super.onError(response, exception);
                            // do nothing....... in future will probably be caching..... and resending later
                        }
                    }).execute(request);


                }

                previousScanSpotz = currentScanSpotz;
                previousScanVisibleBeacons = currentScanVisibleBeacons;
            } else {
                // TODO for now we do nothing. We don't support when the app has been destroyed.
                Log.w(TAG, "SDK not initialized");
            }

        }

        // TODO can we use etags to cache spotz data?
    }


    private void outSpot(Context context, SpotzGetResponse spotzGetResponse) {
        sharedPreferences.edit().putBoolean(spotzGetResponse._id, false).apply();

        Intent broadcastIntent = new Intent(context.getPackageName() + BROADCAST);
        broadcastIntent.putExtra(Spotz.EXTRA_SPOTZ, Spot.clone(spotzGetResponse));
        context.sendBroadcast(broadcastIntent);
    }

    private HashSet<String> beaconIdFromSpotz(SpotzGetResponse spotz) {
        HashSet<String> beaconIds = new HashSet<String>();
        for (SpotzGetResponse.Beacon b : spotz.beacons) {
            beaconIds.add(b.beaconId);
        }

        return beaconIds;
    }

    private String getRandonEventId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}