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


import org.openapitools.client.model.AddCharacterSchema;
import org.openapitools.client.model.CharacterResponseSchema;
import org.openapitools.client.model.DeleteCharacterSchema;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharactersApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public CharactersApi() {
        this(Configuration.getDefaultApiClient());
    }

    public CharactersApi(ApiClient apiClient) {
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
     * Build call for createCharacterCharactersCreatePost
     * @param addCharacterSchema  (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully created character. </td><td>  -  </td></tr>
        <tr><td> 494 </td><td> Name already used. </td><td>  -  </td></tr>
        <tr><td> 495 </td><td> Maximum characters reached on your account. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call createCharacterCharactersCreatePostCall(AddCharacterSchema addCharacterSchema, final ApiCallback _callback) throws ApiException {
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

        Object localVarPostBody = addCharacterSchema;

        // create path and map variables
        String localVarPath = "/characters/create";

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
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "JWTBearer" };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call createCharacterCharactersCreatePostValidateBeforeCall(AddCharacterSchema addCharacterSchema, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'addCharacterSchema' is set
        if (addCharacterSchema == null) {
            throw new ApiException("Missing the required parameter 'addCharacterSchema' when calling createCharacterCharactersCreatePost(Async)");
        }

        return createCharacterCharactersCreatePostCall(addCharacterSchema, _callback);

    }

    /**
     * Create Character
     * Create new character on your account. You can create up to 5 characters.
     * @param addCharacterSchema  (required)
     * @return CharacterResponseSchema
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully created character. </td><td>  -  </td></tr>
        <tr><td> 494 </td><td> Name already used. </td><td>  -  </td></tr>
        <tr><td> 495 </td><td> Maximum characters reached on your account. </td><td>  -  </td></tr>
     </table>
     */
    public CharacterResponseSchema createCharacterCharactersCreatePost(AddCharacterSchema addCharacterSchema) throws ApiException {
        ApiResponse<CharacterResponseSchema> localVarResp = createCharacterCharactersCreatePostWithHttpInfo(addCharacterSchema);
        return localVarResp.getData();
    }

    /**
     * Create Character
     * Create new character on your account. You can create up to 5 characters.
     * @param addCharacterSchema  (required)
     * @return ApiResponse&lt;CharacterResponseSchema&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully created character. </td><td>  -  </td></tr>
        <tr><td> 494 </td><td> Name already used. </td><td>  -  </td></tr>
        <tr><td> 495 </td><td> Maximum characters reached on your account. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<CharacterResponseSchema> createCharacterCharactersCreatePostWithHttpInfo(AddCharacterSchema addCharacterSchema) throws ApiException {
        okhttp3.Call localVarCall = createCharacterCharactersCreatePostValidateBeforeCall(addCharacterSchema, null);
        Type localVarReturnType = new TypeToken<CharacterResponseSchema>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Create Character (asynchronously)
     * Create new character on your account. You can create up to 5 characters.
     * @param addCharacterSchema  (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully created character. </td><td>  -  </td></tr>
        <tr><td> 494 </td><td> Name already used. </td><td>  -  </td></tr>
        <tr><td> 495 </td><td> Maximum characters reached on your account. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call createCharacterCharactersCreatePostAsync(AddCharacterSchema addCharacterSchema, final ApiCallback<CharacterResponseSchema> _callback) throws ApiException {

        okhttp3.Call localVarCall = createCharacterCharactersCreatePostValidateBeforeCall(addCharacterSchema, _callback);
        Type localVarReturnType = new TypeToken<CharacterResponseSchema>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for deleteCharacterCharactersDeletePost
     * @param deleteCharacterSchema  (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully deleted character. </td><td>  -  </td></tr>
        <tr><td> 498 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call deleteCharacterCharactersDeletePostCall(DeleteCharacterSchema deleteCharacterSchema, final ApiCallback _callback) throws ApiException {
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

        Object localVarPostBody = deleteCharacterSchema;

        // create path and map variables
        String localVarPath = "/characters/delete";

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
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] { "JWTBearer" };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call deleteCharacterCharactersDeletePostValidateBeforeCall(DeleteCharacterSchema deleteCharacterSchema, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'deleteCharacterSchema' is set
        if (deleteCharacterSchema == null) {
            throw new ApiException("Missing the required parameter 'deleteCharacterSchema' when calling deleteCharacterCharactersDeletePost(Async)");
        }

        return deleteCharacterCharactersDeletePostCall(deleteCharacterSchema, _callback);

    }

    /**
     * Delete Character
     * Delete character on your account.
     * @param deleteCharacterSchema  (required)
     * @return CharacterResponseSchema
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully deleted character. </td><td>  -  </td></tr>
        <tr><td> 498 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public CharacterResponseSchema deleteCharacterCharactersDeletePost(DeleteCharacterSchema deleteCharacterSchema) throws ApiException {
        ApiResponse<CharacterResponseSchema> localVarResp = deleteCharacterCharactersDeletePostWithHttpInfo(deleteCharacterSchema);
        return localVarResp.getData();
    }

    /**
     * Delete Character
     * Delete character on your account.
     * @param deleteCharacterSchema  (required)
     * @return ApiResponse&lt;CharacterResponseSchema&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully deleted character. </td><td>  -  </td></tr>
        <tr><td> 498 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<CharacterResponseSchema> deleteCharacterCharactersDeletePostWithHttpInfo(DeleteCharacterSchema deleteCharacterSchema) throws ApiException {
        okhttp3.Call localVarCall = deleteCharacterCharactersDeletePostValidateBeforeCall(deleteCharacterSchema, null);
        Type localVarReturnType = new TypeToken<CharacterResponseSchema>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Delete Character (asynchronously)
     * Delete character on your account.
     * @param deleteCharacterSchema  (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully deleted character. </td><td>  -  </td></tr>
        <tr><td> 498 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call deleteCharacterCharactersDeletePostAsync(DeleteCharacterSchema deleteCharacterSchema, final ApiCallback<CharacterResponseSchema> _callback) throws ApiException {

        okhttp3.Call localVarCall = deleteCharacterCharactersDeletePostValidateBeforeCall(deleteCharacterSchema, _callback);
        Type localVarReturnType = new TypeToken<CharacterResponseSchema>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for getCharacterCharactersNameGet
     * @param name The character name. (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched character. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCharacterCharactersNameGetCall(String name, final ApiCallback _callback) throws ApiException {
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
        String localVarPath = "/characters/{name}"
            .replace("{" + "name" + "}", localVarApiClient.escapeString(name.toString()));

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
    private okhttp3.Call getCharacterCharactersNameGetValidateBeforeCall(String name, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling getCharacterCharactersNameGet(Async)");
        }

        return getCharacterCharactersNameGetCall(name, _callback);

    }

    /**
     * Get Character
     * Retrieve the details of a character.
     * @param name The character name. (required)
     * @return CharacterResponseSchema
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched character. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public CharacterResponseSchema getCharacterCharactersNameGet(String name) throws ApiException {
        ApiResponse<CharacterResponseSchema> localVarResp = getCharacterCharactersNameGetWithHttpInfo(name);
        return localVarResp.getData();
    }

    /**
     * Get Character
     * Retrieve the details of a character.
     * @param name The character name. (required)
     * @return ApiResponse&lt;CharacterResponseSchema&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched character. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<CharacterResponseSchema> getCharacterCharactersNameGetWithHttpInfo(String name) throws ApiException {
        okhttp3.Call localVarCall = getCharacterCharactersNameGetValidateBeforeCall(name, null);
        Type localVarReturnType = new TypeToken<CharacterResponseSchema>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get Character (asynchronously)
     * Retrieve the details of a character.
     * @param name The character name. (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Successfully fetched character. </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Character not found. </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCharacterCharactersNameGetAsync(String name, final ApiCallback<CharacterResponseSchema> _callback) throws ApiException {

        okhttp3.Call localVarCall = getCharacterCharactersNameGetValidateBeforeCall(name, _callback);
        Type localVarReturnType = new TypeToken<CharacterResponseSchema>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
