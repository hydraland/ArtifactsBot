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
import java.util.Arrays;
import org.openapitools.client.model.CharacterSchema;
import org.openapitools.client.model.CooldownSchema;
import org.openapitools.client.model.RewardsSchema;

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
 * RewardDataSchema
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-12-02T05:49:51.165890400+01:00[Europe/Paris]", comments = "Generator version: 7.9.0")
public class RewardDataSchema {
  public static final String SERIALIZED_NAME_COOLDOWN = "cooldown";
  @SerializedName(SERIALIZED_NAME_COOLDOWN)
  private CooldownSchema cooldown;

  public static final String SERIALIZED_NAME_REWARDS = "rewards";
  @SerializedName(SERIALIZED_NAME_REWARDS)
  private RewardsSchema rewards;

  public static final String SERIALIZED_NAME_CHARACTER = "character";
  @SerializedName(SERIALIZED_NAME_CHARACTER)
  private CharacterSchema character;

  public RewardDataSchema() {
  }

  public RewardDataSchema cooldown(CooldownSchema cooldown) {
    this.cooldown = cooldown;
    return this;
  }

  /**
   * Cooldown details.
   * @return cooldown
   */
  @javax.annotation.Nonnull
  public CooldownSchema getCooldown() {
    return cooldown;
  }

  public void setCooldown(CooldownSchema cooldown) {
    this.cooldown = cooldown;
  }


  public RewardDataSchema rewards(RewardsSchema rewards) {
    this.rewards = rewards;
    return this;
  }

  /**
   * Reward details.
   * @return rewards
   */
  @javax.annotation.Nonnull
  public RewardsSchema getRewards() {
    return rewards;
  }

  public void setRewards(RewardsSchema rewards) {
    this.rewards = rewards;
  }


  public RewardDataSchema character(CharacterSchema character) {
    this.character = character;
    return this;
  }

  /**
   * Player details.
   * @return character
   */
  @javax.annotation.Nonnull
  public CharacterSchema getCharacter() {
    return character;
  }

  public void setCharacter(CharacterSchema character) {
    this.character = character;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RewardDataSchema rewardDataSchema = (RewardDataSchema) o;
    return Objects.equals(this.cooldown, rewardDataSchema.cooldown) &&
        Objects.equals(this.rewards, rewardDataSchema.rewards) &&
        Objects.equals(this.character, rewardDataSchema.character);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cooldown, rewards, character);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RewardDataSchema {\n");
    sb.append("    cooldown: ").append(toIndentedString(cooldown)).append("\n");
    sb.append("    rewards: ").append(toIndentedString(rewards)).append("\n");
    sb.append("    character: ").append(toIndentedString(character)).append("\n");
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
    openapiFields.add("cooldown");
    openapiFields.add("rewards");
    openapiFields.add("character");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("cooldown");
    openapiRequiredFields.add("rewards");
    openapiRequiredFields.add("character");
  }

  /**
   * Validates the JSON Element and throws an exception if issues found
   *
   * @param jsonElement JSON Element
   * @throws IOException if the JSON Element is invalid with respect to RewardDataSchema
   */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!RewardDataSchema.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in RewardDataSchema is not found in the empty JSON string", RewardDataSchema.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!RewardDataSchema.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `RewardDataSchema` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : RewardDataSchema.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      // validate the required field `cooldown`
      CooldownSchema.validateJsonElement(jsonObj.get("cooldown"));
      // validate the required field `rewards`
      RewardsSchema.validateJsonElement(jsonObj.get("rewards"));
      // validate the required field `character`
      CharacterSchema.validateJsonElement(jsonObj.get("character"));
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!RewardDataSchema.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'RewardDataSchema' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<RewardDataSchema> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(RewardDataSchema.class));

       return (TypeAdapter<T>) new TypeAdapter<RewardDataSchema>() {
           @Override
           public void write(JsonWriter out, RewardDataSchema value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public RewardDataSchema read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

  /**
   * Create an instance of RewardDataSchema given an JSON string
   *
   * @param jsonString JSON string
   * @return An instance of RewardDataSchema
   * @throws IOException if the JSON string is invalid with respect to RewardDataSchema
   */
  public static RewardDataSchema fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, RewardDataSchema.class);
  }

  /**
   * Convert an instance of RewardDataSchema to an JSON string
   *
   * @return JSON string
   */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

