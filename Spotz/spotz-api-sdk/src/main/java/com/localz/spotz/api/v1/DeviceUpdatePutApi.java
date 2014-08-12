package com.localz.spotz.api.v1;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.exceptions.LocalzApiException;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.DeviceUpdatePutRequest;
import com.localz.spotz.api.models.response.v1.DeviceUpdatePutResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class DeviceUpdatePutApi extends ApiMethod<DeviceUpdatePutRequest, DeviceUpdatePutResponse> {

    private static final String PATH = "/devices";

    @Override
    public Response<DeviceUpdatePutResponse> execute(DeviceUpdatePutRequest request) throws LocalzApiException {
        try {
            HttpResponse httpResponse = httpRequestFactory.buildPutRequest(
                new GenericUrl(hostUrl + PATH), new ByteArrayContent("application/json", gson.toJson(request).getBytes()))
                .setHeaders(createDeviceSignedHeaders(new Date(), HttpMethods.PUT, PATH, gson.toJson(request)))
                .execute();

            return response(httpResponse, DeviceUpdatePutResponse.TYPE);

        } catch (IOException e) {
            throw new LocalzApiException("Exception while executing API request: " + DeviceUpdatePutApi.class.getSimpleName(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        } catch (InvalidKeyException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        }
    }
}
