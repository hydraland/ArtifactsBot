/*
 * Artifacts API
 *  Artifacts is an API-based MMO game where you can manage 5 characters to explore, fight, gather resources, craft items and much more.  Website: https://artifactsmmo.com/  Documentation: https://docs.artifactsmmo.com/  OpenAPI Spec: https://api.artifactsmmo.com/openapi.json 
 *
 * The version of the OpenAPI document: 3.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.client.api;

import org.openapitools.client.ApiCallback;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.Configuration;
import org.openapitools.client.Pair;
import org.openapitools.client.ProgressRequestBody;
import org.openapitools.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import org.openapitools.client.model.DataPageMapSchema;
import org.openapitools.client.model.MapContentType;
import org.openapitools.client.model.MapResponseSchema;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public MapsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public MapsApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public int getHostIndex() {
        return localHostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.localHostIndex = hostIndex;
    }

    public String getCustomBaseUrl() {
        return localCustomBaseUrl;
    }

    public void setCustomBaseUrl(String customBaseUrl) {
        this.localCustomBaseUrl = customBaseUrl;
    }

    /**
     * Build call for getAllMapsMapsGet
     * @param contentType Type of content on the map. (optional)
     * @param contentCode Content code on the map. (optional)
     * @param page Page number (optional, default to 1)
     * @param size Page size (optional, default to 50)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched maps details. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getAllMapsMapsGetCall(MapContentType contentType, String contentCode, Integer page, Integer size, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/maps";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        if (contentType != null) {
            localVarQueryParams.addAll(localVarApiClient.parameterToPair("content_type", contentType));
        }

        if (contentCode != null) {
            localVarQueryParams.addAll(localVarApiClient.parameterToPair("content_code", contentCode));
        }

        if (page != null) {
            localVarQueryParams.addAll(localVarApiClient.parameterToPair("page", page));
        }

        if (size != null) {
            localVarQueryParams.addAll(localVarApiClient.parameterToPair("size", size));
        }

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getAllMapsMapsGetValidateBeforeCall(MapContentType contentType, String contentCode, Integer page, Integer size, final ApiCallback _callback) throws ApiException {
        return getAllMapsMapsGetCall(contentType, contentCode, page, size, _callback);

    }

    /**
     * Get All Maps
     * Fetch maps details.
     * @param contentType Type of content on the map. (optional)
     * @param contentCode Content code on the map. (optional)
     * @param page Page number (optional, default to 1)
     * @param size Page size (optional, default to 50)
     * @return DataPageMapSchema
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched maps details. </td><td>  -  </td></tr>
     </table>
     */
    public DataPageMapSchema getAllMapsMapsGet(MapContentType contentType, String contentCode, Integer page, Integer size) throws ApiException {
        ApiResponse<DataPageMapSchema> localVarResp = getAllMapsMapsGetWithHttpInfo(contentType, contentCode, page, size);
        return localVarResp.getData();
    }

    /**
     * Get All Maps
     * Fetch maps details.
     * @param contentType Type of content on the map. (optional)
     * @param contentCode Content code on the map. (optional)
     * @param page Page number (optional, default to 1)
     * @param size Page size (optional, default to 50)
     * @return ApiResponse&lt;DataPageMapSchema&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched maps details. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<DataPageMapSchema> getAllMapsMapsGetWithHttpInfo(MapContentType contentType, String contentCode, Integer page, Integer size) throws ApiException {
        okhttp3.Call localVarCall = getAllMapsMapsGetValidateBeforeCall(contentType, contentCode, page, size, null);
        Type localVarReturnType = new TypeToken<DataPageMapSchema>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get All Maps (asynchronously)
     * Fetch maps details.
     * @param contentType Type of content on the map. (optional)
     * @param contentCode Content code on the map. (optional)
     * @param page Page number (optional, default to 1)
     * @param size Page size (optional, default to 50)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched maps details. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getAllMapsMapsGetAsync(MapContentType contentType, String contentCode, Integer page, Integer size, final ApiCallback<DataPageMapSchema> _callback) throws ApiException {

        okhttp3.Call localVarCall = getAllMapsMapsGetValidateBeforeCall(contentType, contentCode, page, size, _callback);
        Type localVarReturnType = new TypeToken<DataPageMapSchema>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for getMapMapsXYGet
     * @param x The position x of the map. (required)
     * @param y The position X of the map. (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched map. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Map not found. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getMapMapsXYGetCall(Integer x, Integer y, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/maps/{x}/{y}"
            .replace("{" + "x" + "}", localVarApiClient.escapeString(x.toString()))
            .replace("{" + "y" + "}", localVarApiClient.escapeString(y.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getMapMapsXYGetValidateBeforeCall(Integer x, Integer y, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'x' is set
        if (x == null) {
            throw new ApiException("Missing the required parameter 'x' when calling getMapMapsXYGet(Async)");
        }

        // verify the required parameter 'y' is set
        if (y == null) {
            throw new ApiException("Missing the required parameter 'y' when calling getMapMapsXYGet(Async)");
        }

        return getMapMapsXYGetCall(x, y, _callback);

    }

    /**
     * Get Map
     * Retrieve the details of a map.
     * @param x The position x of the map. (required)
     * @param y The position X of the map. (required)
     * @return MapResponseSchema
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched map. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Map not found. </td><td>  -  </td></tr>
     </table>
     */
    public MapResponseSchema getMapMapsXYGet(Integer x, Integer y) throws ApiException {
        ApiResponse<MapResponseSchema> localVarResp = getMapMapsXYGetWithHttpInfo(x, y);
        return localVarResp.getData();
    }

    /**
     * Get Map
     * Retrieve the details of a map.
     * @param x The position x of the map. (required)
     * @param y The position X of the map. (required)
     * @return ApiResponse&lt;MapResponseSchema&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched map. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Map not found. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<MapResponseSchema> getMapMapsXYGetWithHttpInfo(Integer x, Integer y) throws ApiException {
        okhttp3.Call localVarCall = getMapMapsXYGetValidateBeforeCall(x, y, null);
        Type localVarReturnType = new TypeToken<MapResponseSchema>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get Map (asynchronously)
     * Retrieve the details of a map.
     * @param x The position x of the map. (required)
     * @param y The position X of the map. (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched map. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Map not found. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getMapMapsXYGetAsync(Integer x, Integer y, final ApiCallback<MapResponseSchema> _callback) throws ApiException {

        okhttp3.Call localVarCall = getMapMapsXYGetValidateBeforeCall(x, y, _callback);
        Type localVarReturnType = new TypeToken<MapResponseSchema>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
