package com.localz.spotz.api;

public class LocalzApi {

    private static final String TAG = LocalzApi.class.getSimpleName();

    private String deviceId;
    private String appId;
    private String sid;
    private String secret;

    private String channelId;
    private final String host = "https://api.localz.co/spotz-dev/v1";
    //private final String host = "http://dev-spotz-api.herokuapp.com";
    //private final String host = "http://10.0.1.36:3000";
    //private final String host = "http://192.168.1.11:3000";

    private LocalzApi() {
    }

    private static class SingletonHolder {
        private static final LocalzApi INSTANCE = new LocalzApi();
    }

    public static LocalzApi getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(String deviceId, String sid, String appId, String secret, String channelId) {
        this.deviceId = deviceId;
        this.sid = sid;
        this.appId = appId;
        this.secret = secret;
        this.channelId = channelId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    String getDeviceId() {
        return deviceId;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public String getAppId() {
        return appId;
    }

    public String getChannelId() {
        return channelId;
    }

    String getSecret() {
        return secret;
    }

    String getHost() {
        return host;
    }

}

