package com.localz.spotz.api.security;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignatureFactory {

    private static final String TAG = SignatureFactory.class.getSimpleName();

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public static String create(String appId, String secret, Date date, String host, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        Mac sha256 = getSha256(secret);

        String dateString = dateFormat.format(date);

        String data = "Localz:" + appId + ":" + dateString + ":" + method + ":" + host + ":" + uri;

        if ("POST".equals(method) || "PUT".equals(method)) {
            data = data.concat(":" + json);
        }

        byte[] shaHash = sha256.doFinal(data.getBytes());

        return Base64.encodeBase64String(shaHash);
    }

    public static HttpHeaders createHttpHeaders(String appId, String secret, Date date, String host, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        String signature = create(appId, secret, date, host, method, uri, json);

        String dateString = dateFormat.format(date);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAuthorization(dateString + "," + signature);
        httpHeaders.set("x-localz-appid", appId);

        return httpHeaders;
    }

    private static Mac getSha256(String secretKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        sha256.init(secretKeySpec);
        return sha256;
    }
}