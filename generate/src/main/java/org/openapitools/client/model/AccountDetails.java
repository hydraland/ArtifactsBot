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


package org.openapitools.client.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.client.model.AccountStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openapitools.client.JSON;

/**
 * AccountDetails
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-12-02T05:49:51.165890400+01:00[Europe/Paris]", comments = "Generator version: 7.9.0")
public class AccountDetails {
  public static final String SERIALIZED_NAME_USERNAME = "username";
  @SerializedName(SERIALIZED_NAME_USERNAME)
  private String username;

  public static final String SERIALIZED_NAME_SUBSCRIBED = "subscribed";
  @SerializedName(SERIALIZED_NAME_SUBSCRIBED)
  private Boolean subscribed;

  public static final String SERIALIZED_NAME_STATUS = "status";
  @SerializedName(SERIALIZED_NAME_STATUS)
  private AccountStatus status;

  public static final String SERIALIZED_NAME_BADGES = "badges";
  @SerializedName(SERIALIZED_NAME_BADGES)
  private List<Object> badges = new ArrayList<>();

  public static final String SERIALIZED_NAME_ACHIEVEMENTS_POINTS = "achievements_points";
  @SerializedName(SERIALIZED_NAME_ACHIEVEMENTS_POINTS)
  private Integer achievementsPoints;

  public static final String SERIALIZED_NAME_BANNED = "banned";
  @SerializedName(SERIALIZED_NAME_BANNED)
  private Boolean banned;

  public static final String SERIALIZED_NAME_BAN_REASON = "ban_reason";
  @SerializedName(SERIALIZED_NAME_BAN_REASON)
  private String banReason;

  public AccountDetails() {
  }

  public AccountDetails username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Username.
   * @return username
   */
  @javax.annotation.Nonnull
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  public AccountDetails subscribed(Boolean subscribed) {
    this.subscribed = subscribed;
    return this;
  }

  /**
   * Subscribed for the current season.
   * @return subscribed
   */
  @javax.annotation.Nonnull
  public Boolean getSubscribed() {
    return subscribed;
  }

  public void setSubscribed(Boolean subscribed) {
    this.subscribed = subscribed;
  }


  public AccountDetails status(AccountStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Member status.
   * @return status
   */
  @javax.annotation.Nonnull
  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus status) {
    this.status = status;
  }


  public AccountDetails badges(List<Object> badges) {
    this.badges = badges;
    return this;
  }

  public AccountDetails addBadgesItem(Object badgesItem) {
    if (this.badges == null) {
      this.badges = new ArrayList<>();
    }
    this.badges.add(badgesItem);
    return this;
  }

  /**
   * Account badges.
   * @return badges
   */
  @javax.annotation.Nullable
  public List<Object> getBadges() {
    return badges;
  }

  public void setBadges(List<Object> badges) {
    this.badges = badges;
  }


  public AccountDetails achievementsPoints(Integer achievementsPoints) {
    this.achievementsPoints = achievementsPoints;
    return this;
  }

  /**
   * Achievement points.
   * @return achievementsPoints
   */
  @javax.annotation.Nonnull
  public Integer getAchievementsPoints() {
    return achievementsPoints;
  }

  public void setAchievementsPoints(Integer achievementsPoints) {
    this.achievementsPoints = achievementsPoints;
  }


  public AccountDetails banned(Boolean banned) {
    this.banned = banned;
    return this;
  }

  /**
   * Banned.
   * @return banned
   */
  @javax.annotation.Nonnull
  public Boolean getBanned() {
    return banned;
  }

  public void setBanned(Boolean banned) {
    this.banned = banned;
  }


  public AccountDetails banReason(String banReason) {
    this.banReason = banReason;
    return this;
  }

  /**
   * Ban reason.
   * @return banReason
   */
  @javax.annotation.Nullable
  public String getBanReason() {
    return banReason;
  }

  public void setBanReason(String banReason) {
    this.banReason = banReason;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountDetails accountDetails = (AccountDetails) o;
    return Objects.equals(this.username, accountDetails.username) &&
        Objects.equals(this.subscribed, accountDetails.subscribed) &&
        Objects.equals(this.status, accountDetails.status) &&
        Objects.equals(this.badges, accountDetails.badges) &&
        Objects.equals(this.achievementsPoints, accountDetails.achievementsPoints) &&
        Objects.equals(this.banned, accountDetails.banned) &&
        Objects.equals(this.banReason, accountDetails.banReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, subscribed, status, badges, achievementsPoints, banned, banReason);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountDetails {\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    subscribed: ").append(toIndentedString(subscribed)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    badges: ").append(toIndentedString(badges)).append("\n");
    sb.append("    achievementsPoints: ").append(toIndentedString(achievementsPoints)).append("\n");
    sb.append("    banned: ").append(toIndentedString(banned)).append("\n");
    sb.append("    banReason: ").append(toIndentedString(banReason)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("username");
    openapiFields.add("subscribed");
    openapiFields.add("status");
    openapiFields.add("badges");
    openapiFields.add("achievements_points");
    openapiFields.add("banned");
    openapiFields.add("ban_reason");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("username");
    openapiRequiredFields.add("subscribed");
    openapiRequiredFields.add("status");
    openapiRequiredFields.add("achievements_points");
    openapiRequiredFields.add("banned");
  }

  /**
   * Validates the JSON Element and throws an exception if issues found
   *
   * @param jsonElement JSON Element
   * @throws IOException if the JSON Element is invalid with respect to AccountDetails
   */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!AccountDetails.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in AccountDetails is not found in the empty JSON string", AccountDetails.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!AccountDetails.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `AccountDetails` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : AccountDetails.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("username").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `username` to be a primitive type in the JSON string but got `%s`", jsonObj.get("username").toString()));
      }
      // validate the required field `status`
      AccountStatus.validateJsonElement(jsonObj.get("status"));
      // ensure the optional json data is an array if present
      if (jsonObj.get("badges") != null && !jsonObj.get("badges").isJsonNull() && !jsonObj.get("badges").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `badges` to be an array in the JSON string but got `%s`", jsonObj.get("badges").toString()));
      }
      if ((jsonObj.get("ban_reason") != null && !jsonObj.get("ban_reason").isJsonNull()) && !jsonObj.get("ban_reason").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `ban_reason` to be a primitive type in the JSON string but got `%s`", jsonObj.get("ban_reason").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!AccountDetails.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'AccountDetails' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<AccountDetails> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(AccountDetails.class));

       return (TypeAdapter<T>) new TypeAdapter<AccountDetails>() {
           @Override
           public void write(JsonWriter out, AccountDetails value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public AccountDetails read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

  /**
   * Create an instance of AccountDetails given an JSON string
   *
   * @param jsonString JSON string
   * @return An instance of AccountDetails
   * @throws IOException if the JSON string is invalid with respect to AccountDetails
   */
  public static AccountDetails fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, AccountDetails.class);
  }

  /**
   * Convert an instance of AccountDetails to an JSON string
   *
   * @return JSON string
   */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

