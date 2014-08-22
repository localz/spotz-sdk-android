package com.localz.spotz.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.LocalzApi;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.DeviceRegisterPostRequest;
import com.localz.spotz.api.models.request.v1.DeviceUpdatePutRequest;
import com.localz.spotz.api.models.response.v1.BeaconsGetResponse;
import com.localz.spotz.api.models.response.v1.DeviceRegisterPostResponse;
import com.localz.spotz.api.models.response.v1.DeviceUpdatePutResponse;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.sdk.api.DeviceRegisterTask;
import com.localz.spotz.sdk.api.DeviceUpdateTask;
import com.localz.spotz.sdk.api.GetAllSpotzTask;
import com.localz.spotz.sdk.api.ListBeaconsTask;
import com.localz.spotz.sdk.api.utils.ObscuredSharedPreferences;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class Spotz {
    public static final String EXTRA_SPOTZ = Spotz.class.getName() + ".EXTRA_SPOTZ";

    private static final String TAG = Spotz.class.getSimpleName();

    private boolean initialized = false;
    private SharedPreferences sharedPreferences;
    private Set<String> cachedUuids;
    private Map<BleData, String> cachedSpotzIdMap = new HashMap<BleData, String>();
    private Map<String, SpotzGetResponse> cachedSpotzMap = new HashMap<String, SpotzGetResponse>();
    private boolean delayedScanStart;
    private ScanMode delayedScanMode;
    private Long delayedInterval;
    private Long delayedDuration;

    public enum ScanMode {
        PASSIVE, NORMAL, EAGER;
    }

    private Spotz() {
    }

    private static class SingletonHolder {
        private static final Spotz INSTANCE = new Spotz();
    }

    public static Spotz getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Map<BleData, String> getCachedSpotzIdMap() {
        return cachedSpotzIdMap;
    }
    public Map<String, SpotzGetResponse> getCachedSpotzMap() {
        return cachedSpotzMap;
    }

    /**
     * Initialize the Spotz SDK. This must be called successfully before using other SDK methods.
     *
     * @param context The application context
     * @param appId Your Application ID
     * @param clientKey Your client key
     * @param listener Callback listener
     */
    public void initialize(Context context, final String appId, final String clientKey,
                           final InitializationListenerAdapter listener) {
        if (Build.VERSION.SDK_INT >= 18) {
            BleManager.getInstance().stopScanning(context);
        }

        final String deviceId = getSharedPreferences(context).getString("deviceId", null);
        final String sid = getSharedPreferences(context).getString("sid", null);
        final String channelId = "Android";

        LocalzApi.getInstance().init(deviceId, sid, appId, clientKey, channelId);

        // Register id no device id
        if (TextUtils.isEmpty(deviceId)) {
            DeviceRegisterPostRequest content = new DeviceRegisterPostRequest();
            content.applicationVersion = "" + getAppVersion(context);
            content.applicationBuild = getAppVersionName(context);
            content.deviceOs = "Android";
            content.deviceOsVersion = "" + Build.VERSION.SDK_INT;
            content.locale = context.getResources().getConfiguration().locale.toString();
            content.language = context.getResources().getConfiguration().locale.getLanguage();
            int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000 / 60 / 60;
            if (offset > 0) {
                content.timeZone = "GMT+" + offset;
            } else if (offset < 0) {
                content.timeZone = "GMT" + offset;
            }

            registerDevice(context, listener, deviceId, content);
        }
        else {
            DeviceUpdatePutRequest content = new DeviceUpdatePutRequest();
            content.applicationVersion = "" + getAppVersion(context);
            content.applicationBuild = getAppVersionName(context);
            content.deviceOs = "Android";
            content.deviceOsVersion = "" + Build.VERSION.SDK_INT;
            content.locale = context.getResources().getConfiguration().locale.toString();
            content.language = context.getResources().getConfiguration().locale.getLanguage();
            int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000 / 60 / 60;
            if (offset > 0) {
                content.timeZone = "GMT+" + offset;
            } else if (offset < 0) {
                content.timeZone = "GMT" + offset;
            }

            updateDeviceForInitialization(context, listener, content);
        }
    }

    /**
     * Scans for Spotz in the background.
     * Results will be broadcasted and clients will need to register a broadcast receiver with an
     * intent filter for actions:
     * <your package>.LOCALZ_BLE_SCAN_FOUND - called for every beacon found. The {@link com.localz.proximity.ble.BleData}
     * is embedded in the intent as an extra with the name BleManager.EXTRA_BLE_DATA.
     * <your package>.LOCALZ_BLE_SCAN_FINISH - called at the end of the scan. An
     * {@link ArrayList<com.localz.proximity.ble.BleData>} is embedded in the intent as an extra
     * with the name BleManager.EXTRA_BLE_DATA.
     *
     * @param context The application context
     * @param scanMode The scanning mode
     */
    public void startScanningForSpotz(final Context context, ScanMode scanMode) {
        if (cachedUuids != null && !cachedUuids.isEmpty()) {
            scanForBeacons(context, cachedUuids, scanMode);
        }
        else {
            delayedScanStart = true;
            delayedScanMode = scanMode;
        }
    }

    /**
     * Scans for Spotz in the background with custom interval / duration.
     * Results will be broadcasted and clients will need to register a broadcast receiver with an
     * intent filter for actions:
     * <your package>.LOCALZ_BLE_SCAN_FOUND - called for every beacon found. The {@link com.localz.proximity.ble.BleData}
     * is embedded in the intent as an extra with the name BleManager.EXTRA_BLE_DATA.
     * <your package>.LOCALZ_BLE_SCAN_FINISH - called at the end of the scan. An
     * {@link ArrayList<com.localz.proximity.ble.BleData>} is embedded in the intent as an extra
     * with the name BleManager.EXTRA_BLE_DATA.
     *
     * @param context The application context
     * @param scanIntervalMs Milliseconds between the start of each scan
     * @param scanDurationMs Milliseconds to actively scan for
     */
    public void startScanningForSpotz(final Context context, long scanIntervalMs, long scanDurationMs) {
        if (cachedUuids != null && !cachedUuids.isEmpty()) {
            scanForBeacons(context, cachedUuids, scanIntervalMs, scanDurationMs);
        }
        else {
            delayedScanStart = true;
            delayedInterval = scanIntervalMs;
            delayedDuration = scanDurationMs;
        }
    }

    /**
     * Stop scanning for beacons.
     * @param context The application context
     */
    public void stopScanningBeacons(Context context) {
        if (Build.VERSION.SDK_INT >= 18) {
            BleManager.getInstance().stopScanning(context);
        }
    }

    /**
     * True if the SDK has been initialized.
     * @return boolean
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void registerDevice(final Context context, final InitializationListenerAdapter listener,
                                final String deviceId, DeviceRegisterPostRequest content) {
        new DeviceRegisterTask(new ResponseListenerAdapter<DeviceRegisterPostResponse>() {
            @Override
            public void onSuccess(Response<DeviceRegisterPostResponse> response) {
                if (deviceId == null || !deviceId.equals(response.data.deviceId)) {
                    storeDeviceId(context, response.data.deviceId);
                }
                LocalzApi.getInstance().setDeviceId(response.data.deviceId);

                new ListBeaconsTask(new ResponseListenerAdapter<BeaconsGetResponse[]>() {
                    @Override
                    public void onSuccess(Response<BeaconsGetResponse[]> response) {
                        if (response.data != null && response.data.length > 0) {
                            cachedUuids = new HashSet<String>();
                            cachedSpotzIdMap = new HashMap<BleData, String>();

                            for (BeaconsGetResponse beacon : response.data) {
                                cachedUuids.add(beacon.uuid);
                                cachedSpotzIdMap.put(
                                        new BleData(beacon.uuid, null, beacon.major, beacon.minor, 0),
                                        beacon.spotzId);
                            }

                            if (delayedScanStart) {
                                if (delayedInterval != null && delayedDuration != null) {
                                    startScanningForSpotz(context, delayedInterval, delayedDuration);
                                }
                                else {
                                    startScanningForSpotz(context, delayedScanMode);
                                }
                            }
                        }
                    }
                }).execute();

                new GetAllSpotzTask(new ResponseListenerAdapter<SpotzGetResponse[]>() {
                    @Override
                    public void onSuccess(Response<SpotzGetResponse[]> response) {
                        if (response.data != null && response.data.length > 0) {
                            cachedSpotzMap = new HashMap<String, SpotzGetResponse>();

                            for (SpotzGetResponse spot : response.data) {
                                cachedSpotzMap.put(spot._id, spot);
                            }
                        }

                    }
                }).execute();

                // We are initialised!!!
                initialized = true;
                if (listener != null) {
                    listener.onInitialized();
                }
            }

            @Override
            public void onError(Response<DeviceRegisterPostResponse> response, Exception exception) {
                initialized = false;

                if (listener != null) {
                    listener.onError(exception);
                }
            }
        }).execute(content);
    }

    private void updateDeviceForInitialization(final Context context,
                                               final InitializationListenerAdapter listener,
                                               final DeviceUpdatePutRequest content) {
        new DeviceUpdateTask(new ResponseListenerAdapter<DeviceUpdatePutResponse>() {
            @Override
            public void onSuccess(Response<DeviceUpdatePutResponse> response) {
                LocalzApi.getInstance().setDeviceId(response.data.deviceId);

                new ListBeaconsTask(new ResponseListenerAdapter<BeaconsGetResponse[]>() {
                    @Override
                    public void onSuccess(Response<BeaconsGetResponse[]> response) {
                        if (response.data != null && response.data.length > 0) {
                            cachedUuids = new HashSet<String>();
                            cachedSpotzIdMap = new HashMap<BleData, String>();

                            for (BeaconsGetResponse beacon : response.data) {
                                cachedUuids.add(beacon.uuid);
                                cachedSpotzIdMap.put(
                                        new BleData(beacon.uuid, null, beacon.major, beacon.minor, 0),
                                        beacon.spotzId);
                            }

                            if (delayedScanStart) {
                                if (delayedInterval != null && delayedDuration != null) {
                                    startScanningForSpotz(context, delayedInterval, delayedDuration);
                                }
                                else {
                                    startScanningForSpotz(context, delayedScanMode);
                                }
                            }
                        }
                    }
                }).execute();

                new GetAllSpotzTask(new ResponseListenerAdapter<SpotzGetResponse[]>() {
                    @Override
                    public void onSuccess(Response<SpotzGetResponse[]> response) {
                        if (response.data != null && response.data.length > 0) {
                            cachedSpotzMap = new HashMap<String, SpotzGetResponse>();

                            for (SpotzGetResponse spot : response.data) {
                                cachedSpotzMap.put(spot._id, spot);
                            }
                        }

                    }

                    @Override
                    public void onError(Response<SpotzGetResponse[]> response, Exception exception) {
                        super.onError(response, exception);
                    }
                }).execute();

                // We are initialised!!!
                initialized = true;
                if (listener != null) {
                    listener.onInitialized();
                }
            }

            @Override
            public void onError(Response<DeviceUpdatePutResponse> response, Exception exception) {
                initialized = false;

                if (listener != null) {
                    listener.onError(exception);
                }
            }
        }).execute(content);
    }

    private void scanForBeacons(Context context, Set<String> uuids, ScanMode scanMode) {
        if (Build.VERSION.SDK_INT >= 18) {
            int mode;
            switch (scanMode) {
                case PASSIVE:
                    mode = BleManager.SCAN_MODE_PASSIVE;
                    break;
                case EAGER:
                    mode = BleManager.SCAN_MODE_EAGER;
                    break;
                default:
                    mode = BleManager.SCAN_MODE_NORMAL;
                    break;
            }

            BleManager.getInstance().uuids(context, uuids.toArray(new String[uuids.size()]))
                    .startScanning(context, mode, "com.localz.spotz.sdk");
        }
    }

    private void scanForBeacons(Context context, Set<String> uuids, long interval, long duration) {
        if (Build.VERSION.SDK_INT >= 18) {
            BleManager.getInstance().uuids(context, uuids.toArray(new String[uuids.size()]))
                    .startScanning(context, interval, duration, "com.localz.spotz.sdk");
        }
    }

    private void storeDeviceId(Context context, String deviceId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("deviceId", deviceId);
        editor.apply();
    }

    private synchronized SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = new ObscuredSharedPreferences(context, context.getSharedPreferences(Spotz.class.getName(),
                    Context.MODE_PRIVATE));
        }

        return sharedPreferences;
    }

    @SuppressWarnings("ConstantConditions")
    private static int getAppVersion(Context context) {
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
