package com.localz.spotz.api.v1;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.exceptions.LocalzApiException;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.ActivityReportPostRequest;
import com.localz.spotz.api.models.request.v1.DeviceRegisterPostRequest;
import com.localz.spotz.api.models.response.v1.ActivityReportPostResponse;
import com.localz.spotz.api.models.response.v1.DeviceRegisterPostResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class ActivityReportPostApi extends ApiMethod<ActivityReportPostRequest, ActivityReportPostResponse> {

    private static final String PATH = "/report/beacons/{beaconId}";

    @Override
    public Response<ActivityReportPostResponse> execute(ActivityReportPostRequest request) throws LocalzApiException {
        try {
            String json = gson.toJson(request);

            HttpResponse httpResponse = httpRequestFactory.buildPostRequest(
                new GenericUrl(hostUrl + PATH), new ByteArrayContent("application/json", json.getBytes()))
                .setHeaders(createDeviceSignedHeaders(new Date(), HttpMethods.POST, PATH, json))
                .execute();

            return response(httpResponse, ActivityReportPostResponse.TYPE);

        } catch (IOException e) {
            throw new LocalzApiException("Exception while executing API request: " + ActivityReportPostApi.class.getSimpleName(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        } catch (InvalidKeyException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        }
    }
}
