/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.common;

import java.net.MalformedURLException;
import java.net.URL;

public enum ApiEndpoint {
  HEALTH("http://localhost:8080/participant-manager-service/v1/healthCheck"),

  ADD_NEW_SITE("http://localhost:8080/participant-manager-service/sites"),

  ADD_NEW_LOCATION("http://localhost:8080/participant-manager-service/locations"),

  GET_APPS("http://localhost:8080/participant-manager-service/apps"),

  GET_STUDIES("http://localhost:8080/participant-manager-service/studies"),

  UPDATE_LOCATION("http://localhost:8080/participant-manager-service/locations/{locationId}"),

  GET_LOCATIONS("http://localhost:8003/participant-manager-service/locations"),

  GET_LOCATION_WITH_LOCATION_ID(
      "http://localhost:8003/participant-manager-service/locations/{locationId}"),

  GET_LOCATION_FOR_SITE(
      "http://localhost:8080/participant-manager-service/locations-for-site-creation"),

  GET_USER_PROFILE("http://localhost:8080/participant-manager-service/users"),

  UPDATE_USER_PROFILE("http://localhost:8080/participant-manager-service/updateUserProfile"),

  GET_USER_DETAILS("http://localhost:8080/participant-manager-service/userDetails"),

  GET_STUDY_PARTICIPANT(
      "http://localhost:8080/participant-manager-service/studies/{studyId}/participants"),

  DECOMISSION_SITE("http://localhost:8080/participant-manager-service/sites/{siteId}/decommission"),

  ADD_NEW_PARTICIPANT(
      "http://localhost:8080/participant-manager-service/sites/{siteId}/participants"),

  GET_APPS_PARTICIPANTS(
      "http://localhost:8080/participant-manager-service/apps/{app}/participants");

  private String url;

  private ApiEndpoint(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getPath() throws MalformedURLException {
    return new URL(url).getPath();
  }
}
