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
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Gets or Sets ActionType
 */
@JsonAdapter(ActionType.Adapter.class)
public enum ActionType {
  
  MOVEMENT("movement"),
  
  FIGHT("fight"),
  
  CRAFTING("crafting"),
  
  GATHERING("gathering"),
  
  BUY_GE("buy_ge"),
  
  SELL_GE("sell_ge"),
  
  CANCEL_GE("cancel_ge"),
  
  DELETE_ITEM("delete_item"),
  
  DEPOSIT("deposit"),
  
  WITHDRAW("withdraw"),
  
  DEPOSIT_GOLD("deposit_gold"),
  
  WITHDRAW_GOLD("withdraw_gold"),
  
  EQUIP("equip"),
  
  UNEQUIP("unequip"),
  
  TASK("task"),
  
  CHRISTMAS_EXCHANGE("christmas_exchange"),
  
  RECYCLING("recycling"),
  
  REST("rest"),
  
  USE("use"),
  
  BUY_BANK_EXPANSION("buy_bank_expansion");

  private String value;

  ActionType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static ActionType fromValue(String value) {
    for (ActionType b : ActionType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  public static class Adapter extends TypeAdapter<ActionType> {
    @Override
    public void write(final JsonWriter jsonWriter, final ActionType enumeration) throws IOException {
      jsonWriter.value(enumeration.getValue());
    }

    @Override
    public ActionType read(final JsonReader jsonReader) throws IOException {
      String value = jsonReader.nextString();
      return ActionType.fromValue(value);
    }
  }

  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
    String value = jsonElement.getAsString();
    ActionType.fromValue(value);
  }
}

