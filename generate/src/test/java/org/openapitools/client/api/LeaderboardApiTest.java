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

import org.openapitools.client.ApiException;
import org.openapitools.client.model.AccountLeaderboardType;
import org.openapitools.client.model.CharacterLeaderboardType;
import org.openapitools.client.model.DataPageAccountLeaderboardSchema;
import org.openapitools.client.model.DataPageCharacterLeaderboardSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for LeaderboardApi
 */
@Disabled
public class LeaderboardApiTest {

    private final LeaderboardApi api = new LeaderboardApi();

    /**
     * Get Accounts Leaderboard
     *
     * Fetch leaderboard details.
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void getAccountsLeaderboardLeaderboardAccountsGetTest() throws ApiException {
        AccountLeaderboardType sort = null;
        Integer page = null;
        Integer size = null;
        DataPageAccountLeaderboardSchema response = api.getAccountsLeaderboardLeaderboardAccountsGet(sort, page, size);
        // TODO: test validations
    }

    /**
     * Get Characters Leaderboard
     *
     * Fetch leaderboard details.
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void getCharactersLeaderboardLeaderboardCharactersGetTest() throws ApiException {
        CharacterLeaderboardType sort = null;
        Integer page = null;
        Integer size = null;
        DataPageCharacterLeaderboardSchema response = api.getCharactersLeaderboardLeaderboardCharactersGet(sort, page, size);
        // TODO: test validations
    }

}
