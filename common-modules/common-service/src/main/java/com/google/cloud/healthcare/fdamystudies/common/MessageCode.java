/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@JsonSerialize(using = MessageCode.MessageCodeSerializer.class)
public enum MessageCode {
  ADD_SITE_SUCCESS(HttpStatus.CREATED, "MSG-0001", "Site added successfully"),

  ADD_LOCATION_SUCCESS(HttpStatus.CREATED, "MSG-0002", "New location added successfully"),

  GET_APPS_SUCCESS(HttpStatus.OK, "MSG-0003", "Get apps successfull"),

  GET_STUDIES_SUCCESS(HttpStatus.OK, "MSG-0004", "Get studies successfull"),

  DECOMMISSION_SUCCESS(HttpStatus.OK, "MSG-0003", "Decommission successfully"),

  REACTIVE_SUCCESS(HttpStatus.OK, "MSG-0004", "Reactivate successfully"),

  LOCATION_UPDATE_SUCCESS(HttpStatus.OK, "MSG-0004", "Location updated successfully"),

  GET_LOCATION_SUCCESS(HttpStatus.OK, "MSG-0005", "Get locations successfull"),

  GET_LOCATION_FOR_SITE_SUCCESS(HttpStatus.OK, "MSG-0006", "Get locations for site successfull"),

  GET_USER_PROFILE_SUCCESS(HttpStatus.OK, "MSG-0005", "Get user profile successfull"),

  PROFILE_UPDATED_SUCCESS(HttpStatus.OK, "MSG-0005", "Profile updated successfully"),

  GET_USER_PROFILE_WITH_SECURITY_CODE_SUCCESS(
      HttpStatus.OK, "MSG-0005", "Get user profile with security code successfull"),

  GET_PARTICIPANT_REGISTRY_SUCCESS(
      HttpStatus.OK, "MSG-0005", "Get participant registry successfull");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  static class MessageCodeSerializer extends StdSerializer<MessageCode> {

    private static final long serialVersionUID = 1L;

    public MessageCodeSerializer() {
      super(MessageCode.class);
    }

    @Override
    public void serialize(
        MessageCode msgCode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("status", msgCode.getHttpStatus().value());
      jsonGenerator.writeStringField("code", msgCode.getCode());
      jsonGenerator.writeStringField("message", msgCode.getMessage());
      jsonGenerator.writeEndObject();
    }
  }
}
