package com.localz.spotz.api;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Base64;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.localz.spotz.api.exceptions.LocalzApiException;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.utils.GsonHolder;
import com.localz.spotz.api.utils.HttpRequestHolder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class ApiMethod<T, V> {
    private static final String TAG = ApiMethod.class.getSimpleName();

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private final String appId = LocalzApi.getInstance().getAppId();
    private final String secret = LocalzApi.getInstance().getSecret();
    private static final String host = "api.localz.co";

    protected static final String hostUrl = LocalzApi.getInstance().getHost();
    protected final Gson gson = GsonHolder.INSTANCE.getGson();
    protected final HttpRequestFactory httpRequestFactory = HttpRequestHolder.INSTANCE.getHttpRequestFactory();

    /**
     * Execute the API call with no request content.
     * @return The API response
     * @throws LocalzApiException
     */
    public Response<V> execute() throws LocalzApiException {
        throw new UnsupportedOperationException();
    }

    /**
     * Execute the API call and expect request parameter.
     * @param request Request body
     * @return The API response
     * @throws LocalzApiException
     */
    public Response<V> execute(T request) throws LocalzApiException {
        throw new UnsupportedOperationException();
    }

    protected Response response(HttpResponse httpResponse) throws IOException, LocalzApiException {
        Response response;
        if (httpResponse.getContent() != null) {
            try {
                response = gson.fromJson(new InputStreamReader(httpResponse.getContent()), Response.class);
            }
            catch (JsonParseException e) {
                Scanner scanner = new Scanner(httpResponse.getContent()).useDelimiter("\\A");
                throw new LocalzApiException("Could not parse this response: "
                        + (scanner.hasNext() ? scanner.next() : ""), e);
            }
        }
        else {
            response = new Response();
        }

        response.httpStatusCode = httpResponse.getStatusCode();
        response.httpStatusMessage = httpResponse.getStatusMessage();
        response.isSuccess = httpResponse.isSuccessStatusCode();

        return response;
    }

    protected Response<V> response(HttpResponse httpResponse, Type type) throws IOException, LocalzApiException {
        Response<V> response;

        if (httpResponse.getContent() != null) {
            try {
                response = gson.fromJson(new InputStreamReader(httpResponse.getContent()), type);
            }
            catch (JsonParseException e) {
                Scanner scanner = new Scanner(httpResponse.getContent()).useDelimiter("\\A");
                throw new LocalzApiException("Could not parse this response: "
                        + (scanner.hasNext() ? scanner.next() : ""), e);
            }
        }
        else {
            response = new Response<V>();
        }

        response.httpStatusCode = httpResponse.getStatusCode();
        response.httpStatusMessage = httpResponse.getStatusMessage();
        response.isSuccess = httpResponse.isSuccessStatusCode();

        return response;
    }

    protected String getDeviceId() {
        return LocalzApi.getInstance().getDeviceId();
    }

    protected HttpHeaders createAuthSignedHeaders(Date date, String method, String uri) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return createAuthSignedHeaders(date, method, uri, null);
    }

    protected HttpHeaders createAuthSignedHeaders(Date date, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return createSignedHeaders(LocalzApi.getInstance().getDeviceId(), LocalzApi.getInstance().getSid(), date, method, uri, json);
    }

    protected HttpHeaders createDeviceSignedHeaders(Date date, String method, String uri) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return createDeviceSignedHeaders(date, method, uri, null);
    }

    protected HttpHeaders createDeviceSignedHeaders(Date date, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return createSignedHeaders(LocalzApi.getInstance().getDeviceId(), null, date, method, uri, json);
    }

    protected HttpHeaders createDevicelessSignedHeaders(Date date, String method, String uri) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return createDevicelessSignedHeaders(date, method, uri, null);
    }

    protected HttpHeaders createDevicelessSignedHeaders(Date date, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return createSignedHeaders(null, null, date, method, uri, json);
    }

    private HttpHeaders createSignedHeaders(String deviceId, String sid, Date date, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String signature = createSignature(deviceId, date, method, uri, json);
        String dateString = dateFormat.format(date);
        System.out.println(dateString + "," + signature);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAuthorization(dateString + "," + signature);
        httpHeaders.set("x-localz-appid", appId);
        if (deviceId != null) {
            httpHeaders.set("x-localz-deviceid", deviceId);
        }
        if (sid != null) {
            httpHeaders.setCookie(sid);
        }

        return httpHeaders;
    }

    private String createSignature(String deviceId, Date date, String method, String uri, String json) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Mac sha256 = getSha256(secret);
        String dateString = dateFormat.format(date);

        String data = "LocalzSpotz:" + appId + ":" + (deviceId == null ? "" : deviceId + ":")
                + dateString + ":" + method + ":" + host + ":" + uri;

        if (json != null && ("POST".equals(method) || "PUT".equals(method))) {
            data = data.concat(":" + json);
        }
        System.out.println(data);

        byte[] shaHash = sha256.doFinal(data.getBytes());

        return Base64.encodeBase64String(shaHash);
    }

    private static Mac getSha256(String secretKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        sha256.init(secretKeySpec);
        return sha256;
    }
}
