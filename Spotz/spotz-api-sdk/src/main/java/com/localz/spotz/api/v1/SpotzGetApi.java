package com.localz.spotz.api.v1;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.exceptions.LocalzApiException;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.DeviceRegisterPostRequest;
import com.localz.spotz.api.models.response.v1.DeviceRegisterPostResponse;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class SpotzGetApi extends ApiMethod<Void, SpotzGetResponse[]> {

    private static final String PATH = "/spotz";

    @Override
    public Response<SpotzGetResponse[]> execute() throws LocalzApiException {
        try {

            HttpResponse httpResponse = httpRequestFactory.buildGetRequest(
                    new GenericUrl(hostUrl + PATH))
                    .setHeaders(createAuthSignedHeaders(new Date(), HttpMethods.GET, PATH))
                    .execute();

            return response(httpResponse, SpotzGetResponse.TYPE);

        } catch (IOException e) {
            throw new LocalzApiException("Exception while executing API request: " + SpotzGetApi.class.getSimpleName(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        } catch (InvalidKeyException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        }
    }
}
