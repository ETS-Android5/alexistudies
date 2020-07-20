/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.mapper;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.NO;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.google.cloud.healthcare.fdamystudies.beans.LocationRequest;
import com.google.cloud.healthcare.fdamystudies.beans.LocationResponse;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;

public final class LocationMapper {

  private LocationMapper() {}

  public static LocationResponse toLocationResponse(
      LocationEntity location, MessageCode messageCode) {
    LocationResponse response = new LocationResponse(messageCode);
    response.setLocationId(location.getId());
    response.setCustomId(location.getCustomId());
    response.setDescription(location.getDescription());
    response.setName(location.getName());
    response.setStatus(location.getStatus());
    return response;
  }

  public static LocationEntity fromLocationRequest(LocationRequest locationRequest) {
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.setName(locationRequest.getName());
    locationEntity.setDescription(locationRequest.getDescription());
    locationEntity.setCustomId(locationRequest.getCustomId());
    locationEntity.setStatus(ACTIVE_STATUS);
    locationEntity.setIsDefault(NO);
    return locationEntity;
  }

  public static List<LocationRequest> listOfLocationRequest(List<LocationEntity> listOfLocation) {
    List<LocationRequest> listOfLocationRequest = new LinkedList<>();
    if (!CollectionUtils.isEmpty(listOfLocation)) {
      for (LocationEntity locationEntity : listOfLocation) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setLocationId(locationEntity.getId());
        locationRequest.setName(locationEntity.getName());
        locationRequest.setDescription(locationEntity.getDescription());
        locationRequest.setCustomId(locationEntity.getCustomId());
        locationRequest.setStatus(locationEntity.getStatus());
        listOfLocationRequest.add(locationRequest);
      }
    }
    return listOfLocationRequest;
  }
}
