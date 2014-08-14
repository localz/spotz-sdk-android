package com.localz.spotz.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.localz.proximity.ble.BleData;
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
import com.localz.spotz.sdk.models.InitializedResponse;
import com.localz.proximity.ble.BleManager;

import java.io.IOException;
import java.util.Set;
import java.util.TimeZone;

public class Spotz {
    private static final String TAG = Spotz.class.getSimpleName();
    private static final String SENDER_ID = "need to get one";

    private String gcmRegId;
    private LocationClient locationClient;
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

        LocalzApi.getInstance().init(deviceId, sid, appId, secret);

        // TODO uncomment when we need GCM
        //if (checkPlayServices(context)) {
        //  initGoogleServices(context);
        //} else {
        //  Log.i(TAG, "No valid Google Play Services APK found.");
        //}

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
            if (gcmRegId != null && !gcmRegId.isEmpty()) {
                content.notification = new DeviceRegisterPostRequest.Notification();
                content.notification.pushId = gcmRegId;
                content.notification.pushEnabled = true;
            }

            registerDevice(context, listener, deviceId, content);
        }
        // TODO move this to after we get gcm id
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
            if (gcmRegId != null && !gcmRegId.isEmpty()) {
                content.notification = new DeviceUpdatePutRequest.Notification();
                content.notification.pushId = gcmRegId;
                content.notification.pushEnabled = true;
            }

            updateDeviceForInitialization(context, listener, content);
        }
    }

    public void storeSid(Context context, final String sid) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("sid", sid);
        editor.apply();

        LocalzApi.getInstance().setSid(sid);
    }

    public void getSpotz(ResponseListenerAdapter<SpotzGetResponse[]> listener) {
        new SpotzListingsTask(listener).execute();
    }

    private synchronized SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = new ObscuredSharedPreferences(context, context.getSharedPreferences(Spotz.class.getName(),
                    Context.MODE_PRIVATE));
        }

        return sharedPreferences;
    }

    public void clearDeviceCache(Context context) {
        storeSid(context, null);
        storeDeviceId(context, null);
    }

    public void startScanningBeacons(Context context, String[] uuids) {
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
                if (deviceId == null || !deviceId.equals(response.data)) {
                    storeDeviceId(context, response.data.deviceId);
                }

                LocalzApi.getInstance().setDeviceId(response.data.deviceId);

                // We are initialised!!!
                initialized = true;
                if (listener != null) {
                    InitializedResponse initializedResponse = new InitializedResponse();
                    initializedResponse.deviceId = response.data.deviceId;
                    listener.onInitialized(initializedResponse);
                }
            }

            @Override
            public void onError(Response<DeviceRegisterPostResponse> response, Exception exception) {
                if (listener != null) {
                    if (response != null && "409".equals(response.code)) {
                        listener.onDeviceNotRegistered();
                    } else {
                        listener.onError(exception);
                    }
                }

                initialized = false;
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

                // We are initialised!!!
                initialized = true;
                if (listener != null) {
                    InitializedResponse initializedResponse = new InitializedResponse();
                    initializedResponse.deviceId = response.data.deviceId;
                    initializedResponse.agentUuid = response.data.auid;
                    initializedResponse.propertyUuid = response.data.puid;
                    //initializedResponse.customer = response.data.customer;
                    listener.onInitialized(initializedResponse);
                }
            }

            @Override
            public void onError(Response<DeviceUpdatePutResponse> response, Exception exception) {
                if (listener != null) {
                    if (response != null && "409".equals(response.code)) {
                        listener.onDeviceNotRegistered();
                    } else {
                        listener.onError(exception);
                    }
                }

                initialized = false;
            }
        }).execute(content);
    }

    /**
     * Start scanning for beacons. Only if the user has allowed, and that we are on Android SDK 18+.
     * @param context
     * @param uuids - uuids to scan for
     */
    private void scanForBeacons(Context context, String[] uuids) {
        if (Build.VERSION.SDK_INT >= 18) {
            // Only scan for the UUIDs configured for their application
            BleManager.getInstance().uuids(context, uuids)
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
                    .startScanning(context, BleManager.SCAN_MODE_EAGER);
        }
    }

    private boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void initGoogleServices(Context context) {
        locationClient = new LocationClient(context, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                LocationRequest request = new LocationRequest();
                request.setInterval(900000);
                locationClient.requestLocationUpdates(request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (initialized) {
                            //refreshClosestSiteOffers();
                        }
                    }
                });
            }

            @Override
            public void onDisconnected() {
            }
        }, new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {

            }
        }
        );

        locationClient.connect();

        gcmRegId = getRegistrationId(context);
        if (gcmRegId.isEmpty()) {
            registerInBackground(context);
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = getSharedPreferences(context).getString("registrationId", "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = getSharedPreferences(context).getInt("appVersion", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     *
     * @param context The application context
     */
    private void registerInBackground(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    gcmRegId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + gcmRegId;

                    storeRegistrationId(context);

                    // TODO call update device
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute();
    }

    private void storeRegistrationId(Context context) {
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("registrationId", gcmRegId);
        editor.putInt("appVersion", appVersion);
        editor.apply();
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
