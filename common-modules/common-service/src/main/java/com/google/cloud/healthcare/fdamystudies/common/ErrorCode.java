/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import java.io.IOException;
import java.time.Instant;

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
@JsonSerialize(using = ErrorCode.ErrorCodeSerializer.class)
public enum ErrorCode {
  BAD_REQUEST(
      400,
      "EC-400",
      Constants.BAD_REQUEST,
      "Malformed request syntax or invalid request message framing."),

  UNAUTHORIZED(401, "EC-401", "Unauthorized", "Invalid token"),

  USER_NOT_FOUND(404, "EC-114", Constants.BAD_REQUEST, "User not found"),

  CURRENT_PASSWORD_INVALID(400, "EC-119", Constants.BAD_REQUEST, "Current password is invalid"),

  ENFORCE_PASSWORD_HISTORY(
      400,
      "EC-105",
      Constants.BAD_REQUEST,
      "Your new password cannot repeat any of your previous 10 passwords;"),

  EMAIL_EXISTS(
      409,
      "EC-101",
      Constants.BAD_REQUEST,
      "This email has already been used. Please try with a different email address."),

  EMAIL_SEND_FAILED_EXCEPTION(
      500,
      "EC-500",
      "Email Server Error",
      "Your email was unable to send because the connection to mail server was interrupted. Please check your inbox for mail delivery failure notice."),

  APPLICATION_ERROR(
      500,
      "EC-500",
      "Internal Server Error",
      "Sorry, an error has occurred and your request could not be processed. Please try again later."),

  SITE_PERMISSION_ACEESS_DENIED(
      403, "EC-105", HttpStatus.FORBIDDEN.toString(), "Does not have permission to maintain site"),

  SITE_EXISTS(
      400, "EC-106", Constants.BAD_REQUEST, "Site exists with the given locationId and studyId"),

  LOCATION_ACCESS_DENIED(
      403, "EC-882", "Forbidden", "You do not have permission to view or add or update locations"),

  LOCATION_UPDATE_DENIED(
      403, "EC-882", "Forbidden", "You do not have permission to update the location"),

  INVALID_ARGUMENTS(400, "EC_813", Constants.BAD_REQUEST, "Provided argument value is invalid"),

  USER_NOT_EXISTS(401, "EC_861", "Unauthorized", "User does not exist"),

  MISSING_REQUIRED_ARGUMENTS(400, "EC_812", Constants.BAD_REQUEST, "Missing required argument"),

  CUSTOM_ID_EXISTS(400, "EC_883", Constants.BAD_REQUEST, "customId already exists"),

  USER_NOT_ACTIVE(400, "EC_93", Constants.BAD_REQUEST, "User not Active"),

  APP_NOT_FOUND(404, "EC-817", Constants.BAD_REQUEST, "App not found."),

  STUDY_NOT_FOUND(404, "EC-816", Constants.BAD_REQUEST, "Study not found"),

  LOCATION_NOT_FOUND(404, "EC_881", "Not Found", "No Locations Found"),

  DEFAULT_SITE_MODIFY_DENIED(
      400, "EC_888", Constants.BAD_REQUEST, "Default site can't be modified"),

  ALREADY_DECOMMISSIONED(
      400, "EC_886", Constants.BAD_REQUEST, "Can't decommision an already decommissioned location"),

  CANNOT_DECOMMISSIONED(
      400,
      "EC_885",
      Constants.BAD_REQUEST,
      "This Location is being used as an active Site in one or more studies and cannot be decomissioned"),
  CANNOT_REACTIVE(
      400, "EC_887", Constants.BAD_REQUEST, "Can't reactive an already active location"),

  PROFILE_NOT_UPDATED(400, "EC-001", Constants.BAD_REQUEST, "Profile has been not updated."),

  INVALID_SECURITY_CODE(404, "EC_869", "Not Found", "Invalid Security code"),

  SECURITY_CODE_EXPIRED(401, "EC_880", "Unauthorized", "Security code has expired"),

  SITE_NOT_EXIST(400, "EC_865", Constants.BAD_REQUEST, "Site doesn't exists or is inactive"),

  NO_PERMISSION_TO_MANAGE_SITE(
      403, "EC_863", HttpStatus.FORBIDDEN.toString(), "You do not have permission to manage site"),

  SITE_NOT_FOUND(404, "EC-94", Constants.BAD_REQUEST, "Site not found"),

  OPEN_STUDY_FOR_DECOMMISSION_SITE(
      400, "EC-95", Constants.BAD_REQUEST, " Cannot decomission site as studyType is open"),

  MANAGE_SITE_PERMISSION_ACCESS_DENIED(
      403, "EC-105", HttpStatus.FORBIDDEN.toString(), "You do not have permission to manage site"),

  OPEN_STUDY(
      403, "EC-989", HttpStatus.FORBIDDEN.toString(), "Can not add participant to open study"),

  ENROLLED_PARTICIPANT(400, "EC-862", Constants.BAD_REQUEST, "Participant already enrolled"),

  SITE_NOT_EXIST_OR_INACTIVE(
      400, "EC-869", Constants.BAD_REQUEST, "Site doesn't exists or is inactive"),

  STUDY_PERMISSION_ACCESS_DENIED(
      403, "EC-105", HttpStatus.FORBIDDEN.toString(), "Does not have study permission"),

  GET_PARTICIPANTS_ERROR(400, "EC-105", Constants.BAD_REQUEST, "Error getting participants."),

  ACCESS_DENIED(400, "EC-869", Constants.BAD_REQUEST, "Required at least one site permission"),

  // TODO Madhurya N (import we shouldn't use??)........not able to replace {num} since members are
  // private and final
  EMAIL_FAILED_TO_IMPORT(
      409, "EC_915", HttpStatus.CONFLICT.toString(), "{num} email failed to import"),

  USER_ADMIN_ACCESS_DENIED(403, "EC-882", "Forbidden", "You do not have permission of Super Admin");

  private final int status;
  private final String code;
  private final String errorType;
  private final String description;

  private static class Constants {

    private static final String BAD_REQUEST = "Bad Request";
  }

  static class ErrorCodeSerializer extends StdSerializer<ErrorCode> {

    private static final long serialVersionUID = 1L;

    public ErrorCodeSerializer() {
      super(ErrorCode.class);
    }

    @Override
    public void serialize(
        ErrorCode error, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("status", error.getStatus());
      jsonGenerator.writeStringField("error_type", error.getErrorType());
      jsonGenerator.writeStringField("error_code", error.getCode());
      jsonGenerator.writeStringField("error_description", error.getDescription());
      jsonGenerator.writeNumberField("timestamp", Instant.now().toEpochMilli());
      jsonGenerator.writeEndObject();
    }
  }
}
