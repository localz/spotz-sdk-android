package com.localz.spotz.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.LocalzApi;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.DeviceRegisterPostRequest;
import com.localz.spotz.api.models.request.v1.DeviceUpdatePutRequest;
import com.localz.spotz.api.models.response.v1.DeviceRegisterPostResponse;
import com.localz.spotz.api.models.response.v1.DeviceUpdatePutResponse;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.sdk.api.DeviceRegisterTask;
import com.localz.spotz.sdk.api.DeviceUpdateTask;
import com.localz.spotz.sdk.api.SpotzListingsTask;
import com.localz.spotz.sdk.api.utils.ObscuredSharedPreferences;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class Spotz {
    private static final String TAG = Spotz.class.getSimpleName();

    private boolean initialized = false;
    private SharedPreferences sharedPreferences;

    private Spotz() {
    }

    private static class SingletonHolder {
        private static final Spotz INSTANCE = new Spotz();
    }

    public static Spotz getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void initialize(Context context, final String appId, final String secret) {
        initialize(context, appId, secret, null);
    }

    public void initialize(Context context, final String appId, final String secret,
                           final InitializationListenerAdapter listener) {
        if (Build.VERSION.SDK_INT >= 18) {
            BleManager.getInstance().stopScanning(context);
        }

        final String deviceId = getSharedPreferences(context).getString("deviceId", null);
        final String sid = getSharedPreferences(context).getString("sid", null);
        final String channelId = "Android";

        LocalzApi.getInstance().init(deviceId, sid, appId, secret, channelId);

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

    public void fetchSpotz(ResponseListenerAdapter<SpotzGetResponse[]> listener) {
        new SpotzListingsTask(listener).execute();
    }

    private synchronized SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = new ObscuredSharedPreferences(context, context.getSharedPreferences(Spotz.class.getName(),
                    Context.MODE_PRIVATE));
        }

        return sharedPreferences;
    }

    public void startScanningBeacons(Context context, List<String> uuids) {
        scanForBeacons(context, uuids);
    }

    public void stopScanningBeacons(Context context) {
        if (Build.VERSION.SDK_INT >= 18) {
            BleManager.getInstance().stopScanning(context);
        }
    }

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

                fetchSpotz(new ResponseListenerAdapter<SpotzGetResponse[]>() {
                    @Override
                    public void onSuccess(Response<SpotzGetResponse[]> response) {
                        if (response.data != null && response.data.length > 0) {
                            List<String> uuids = new ArrayList<String>();
                            for (SpotzGetResponse spotz : response.data) {
                                uuids.add(spotz.uuid);
                            }
                            startScanningBeacons(context, uuids);
                        }
                    }
                });

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

                fetchSpotz(new ResponseListenerAdapter<SpotzGetResponse[]>() {
                    @Override
                    public void onSuccess(Response<SpotzGetResponse[]> response) {
                        if (response.data != null && response.data.length > 0) {
                            List<String> uuids = new ArrayList<String>();
                            for (SpotzGetResponse spotz : response.data) {
                                uuids.add(spotz.uuid);
                            }
                            startScanningBeacons(context, uuids);
                        }
                    }
                });

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

    /**
     * Start scanning for beacons. Only if the user has allowed, and that we are on Android SDK 18+.
     * @param context The application context
     * @param uuids - uuids to scan for
     */
    private void scanForBeacons(Context context, List<String> uuids) {
        if (Build.VERSION.SDK_INT >= 18) {
            // Only scan for the UUIDs configured for their application
            BleManager.getInstance().uuids(context, uuids.toArray(new String[uuids.size()]))
                    /*.scan(context, new BleManager.OnBleScanResultListener() {
                        @Override
                        public void onScanFound(BleData bleData) {
                            Log.d("TAG", "onScanFound");
                        }

                        @Override
                        public void onScanFinish(Set<BleData> bleDatas) {
                            Log.d("TAG", "onScanFinish");
                        }
                    });*/
                    .startScanning(context, 10000, 4000, context.getPackageName());
        }
    }

    private void storeDeviceId(Context context, String deviceId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("deviceId", deviceId);
        editor.apply();
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
