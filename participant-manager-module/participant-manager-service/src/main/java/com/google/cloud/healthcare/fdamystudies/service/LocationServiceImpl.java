/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.LocationDetails;
import com.google.cloud.healthcare.fdamystudies.beans.LocationDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.LocationRequest;
import com.google.cloud.healthcare.fdamystudies.beans.LocationResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateLocationRequest;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerAuditLogHelper;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.mapper.LocationMapper;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationIdStudyNamesPair;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.LocationRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.INACTIVE_STATUS;

@Service
public class LocationServiceImpl implements LocationService {

  private XLogger logger = XLoggerFactory.getXLogger(LocationServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private LocationRepository locationRepository;

  @Autowired private SiteRepository siteRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private ParticipantManagerAuditLogHelper participantManagerHelper;

  @Override
  @Transactional
  public LocationDetailsResponse addNewLocation(
      LocationRequest locationRequest, AuditLogEventRequest auditRequest) {
    logger.entry("begin addNewLocation()");

    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(locationRequest.getUserId());

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (Permission.READ_EDIT != Permission.fromValue(adminUser.getLocationPermission())) {

      logger.exit(
          String.format(
              "Add location failed with error code=%s", ErrorCode.LOCATION_ACCESS_DENIED));
      return new LocationDetailsResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }
    LocationEntity locationEntity = LocationMapper.fromLocationRequest(locationRequest);
    locationEntity.setCreatedBy(adminUser.getId());
    locationEntity = locationRepository.saveAndFlush(locationEntity);
    logger.exit(String.format("locationId=%s", locationEntity.getId()));

    auditRequest.setUserId(locationRequest.getUserId());
    Map<String, String> map = Collections.singletonMap("location_id", locationEntity.getId());

    participantManagerHelper.logEvent(
        ParticipantManagerEvent.NEW_LOCATION_ADDED, auditRequest, map);

    return LocationMapper.toLocationDetailsResponse(
        locationEntity, MessageCode.ADD_LOCATION_SUCCESS);
  }

  @Override
  @Transactional
  public LocationDetailsResponse updateLocation(
      UpdateLocationRequest locationRequest, AuditLogEventRequest auditRequest) {
    logger.entry("begin updateLocation()");

    Optional<LocationEntity> optLocation =
        locationRepository.findById(locationRequest.getLocationId());

    ErrorCode errorCode = validateUpdateLocationRequest(locationRequest, optLocation);
    if (errorCode != null) {
      logger.exit(errorCode);
      return new LocationDetailsResponse(errorCode);
    }

    LocationEntity locationEntity = optLocation.get();
    locationEntity.setName(
        StringUtils.defaultString(locationRequest.getName(), locationEntity.getName()));
    locationEntity.setDescription(
        StringUtils.defaultString(
            locationRequest.getDescription(), locationEntity.getDescription()));

    if (locationRequest.getStatus() != null) {
      locationEntity.setStatus(locationRequest.getStatus());
    }
    locationEntity = locationRepository.saveAndFlush(locationEntity);

    MessageCode messageCode = getMessageCodeByLocationStatus(locationRequest.getStatus());
    LocationDetailsResponse locationResponse =
        LocationMapper.toLocationDetailsResponse(locationEntity, messageCode);

    auditRequest.setUserId(locationRequest.getUserId());
    Map<String, String> map = Collections.singletonMap("location_id", locationEntity.getId());

    if (messageCode == MessageCode.REACTIVE_SUCCESS) {
      participantManagerHelper.logEvent(
          ParticipantManagerEvent.LOCATION_ACTIVATED, auditRequest, map);
    } else if (messageCode == MessageCode.DECOMMISSION_SUCCESS) {
      participantManagerHelper.logEvent(
          ParticipantManagerEvent.LOCATION_DECOMMISSIONED, auditRequest, map);
    } else {
      participantManagerHelper.logEvent(ParticipantManagerEvent.LOCATION_EDITED, auditRequest, map);
    }

    logger.exit(String.format("locationId=%s", locationEntity.getId()));
    return locationResponse;
  }

  private MessageCode getMessageCodeByLocationStatus(Integer status) {
    if (ACTIVE_STATUS.equals(status)) {
      return MessageCode.REACTIVE_SUCCESS;
    } else if (INACTIVE_STATUS.equals(status)) {
      return MessageCode.DECOMMISSION_SUCCESS;
    }
    return MessageCode.LOCATION_UPDATE_SUCCESS;
  }

  private ErrorCode validateUpdateLocationRequest(
      UpdateLocationRequest locationRequest, Optional<LocationEntity> optLocation) {
    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(locationRequest.getUserId());

    if (optUserRegAdminUser.isPresent()) {
      UserRegAdminEntity adminUser = optUserRegAdminUser.get();
      if (Permission.READ_EDIT != Permission.fromValue(adminUser.getLocationPermission())) {
        return ErrorCode.LOCATION_UPDATE_DENIED;
      }
    }

    if (!optLocation.isPresent()) {
      return ErrorCode.LOCATION_NOT_FOUND;
    }

    LocationEntity locationEntity = optLocation.get();
    if (locationEntity.isDefault()) {
      return ErrorCode.DEFAULT_SITE_MODIFY_DENIED;
    }

    if (INACTIVE_STATUS.equals(locationRequest.getStatus())
        && INACTIVE_STATUS.equals(locationEntity.getStatus())) {
      return ErrorCode.ALREADY_DECOMMISSIONED;
    }

    List<SiteEntity> listOfSite =
        siteRepository.findByLocationIdAndStatus(locationRequest.getLocationId(), ACTIVE_STATUS);
    if (INACTIVE_STATUS.equals(locationRequest.getStatus())
        && CollectionUtils.isNotEmpty(listOfSite)) {
      return ErrorCode.CANNOT_DECOMMISSIONED;
    }

    if (ACTIVE_STATUS.equals(locationRequest.getStatus())
        && ACTIVE_STATUS.equals(locationEntity.getStatus())) {
      return ErrorCode.CANNOT_REACTIVATE;
    }

    return null;
  }

  @Override
  @Transactional
  public LocationResponse getLocations(String userId) {
    logger.entry("begin getLocations()");

    Optional<UserRegAdminEntity> optUserRegAdminUser = userRegAdminRepository.findById(userId);
    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (Permission.NO_PERMISSION == Permission.fromValue(adminUser.getLocationPermission())) {
      logger.exit(ErrorCode.LOCATION_ACCESS_DENIED);
      return new LocationResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }

    List<LocationEntity> locations =
        (List<LocationEntity>) CollectionUtils.emptyIfNull(locationRepository.findAll());
    List<String> locationIds =
        locations.stream().map(LocationEntity::getId).distinct().collect(Collectors.toList());
    Map<String, List<String>> locationStudies = getStudiesAndGroupByLocationId(locationIds);

    List<LocationDetails> locationDetailsList =
        locations.stream().map(LocationMapper::toLocationDetails).collect(Collectors.toList());
    for (LocationDetails locationDetails : locationDetailsList) {
      List<String> studies = locationStudies.get(locationDetails.getLocationId());
      if (studies != null) {
        locationDetails.getStudyNames().addAll(studies);
      }
      locationDetails.setStudiesCount(locationDetails.getStudyNames().size());
    }
    LocationResponse locationResponse =
        new LocationResponse(MessageCode.GET_LOCATION_SUCCESS, locationDetailsList);
    logger.exit(String.format("locations size=%d", locationResponse.getLocations().size()));
    return locationResponse;
  }

  private Map<String, List<String>> getStudiesAndGroupByLocationId(List<String> locationIds) {
    List<LocationIdStudyNamesPair> studyNames =
        (List<LocationIdStudyNamesPair>)
            CollectionUtils.emptyIfNull(studyRepository.getStudyNameLocationIdPairs(locationIds));

    Map<String, List<String>> locationStudies = new HashMap<>();
    for (LocationIdStudyNamesPair locationIdStudyNames : studyNames) {
      String locationId = locationIdStudyNames.getLocationId();
      String studiesString = locationIdStudyNames.getStudyNames();
      if (StringUtils.isNotBlank(studiesString)) {
        List<String> studies = Arrays.asList(studiesString.split(","));
        locationStudies.put(locationId, studies);
      }
    }

    return locationStudies;
  }

  @Override
  @Transactional
  public LocationDetailsResponse getLocationById(String userId, String locationId) {
    logger.entry("begin getLocationById()");

    Optional<UserRegAdminEntity> optUserRegAdminUser = userRegAdminRepository.findById(userId);
    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (Permission.NO_PERMISSION == Permission.fromValue(adminUser.getLocationPermission())) {

      logger.exit(ErrorCode.LOCATION_ACCESS_DENIED);
      return new LocationDetailsResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }

    Optional<LocationEntity> optOfEntity = locationRepository.findById(locationId);
    if (!optOfEntity.isPresent()) {

      logger.exit(ErrorCode.LOCATION_NOT_FOUND);
      return new LocationDetailsResponse(ErrorCode.LOCATION_NOT_FOUND);
    }

    LocationEntity locationEntity = optOfEntity.get();
    String studyNames = studyRepository.getStudyNamesByLocationId(locationId);

    LocationDetailsResponse locationResponse =
        LocationMapper.toLocationDetailsResponse(locationEntity, MessageCode.GET_LOCATION_SUCCESS);
    if (!StringUtils.isEmpty(studyNames)) {
      locationResponse.getStudies().addAll(Arrays.asList(studyNames.split(",")));
    }

    logger.exit(String.format("locationId=%s", locationEntity.getId()));
    return locationResponse;
  }

  @Override
  @Transactional
  public LocationResponse getLocationsForSite(
      String userId, Integer status, String excludeStudyId) {
    Optional<UserRegAdminEntity> optUserRegAdminUser = userRegAdminRepository.findById(userId);

    UserRegAdminEntity adminUser = optUserRegAdminUser.get();
    if (Permission.NO_PERMISSION == Permission.fromValue(adminUser.getLocationPermission())) {
      logger.exit(ErrorCode.LOCATION_ACCESS_DENIED);
      return new LocationResponse(ErrorCode.LOCATION_ACCESS_DENIED);
    }
    List<LocationEntity> listOfLocation =
        (List<LocationEntity>)
            CollectionUtils.emptyIfNull(
                locationRepository.findByStatusAndExcludeStudyId(status, excludeStudyId));
    List<LocationDetails> locationDetails =
        listOfLocation.stream().map(LocationMapper::toLocationDetails).collect(Collectors.toList());

    logger.exit(String.format("locations size=%d", locationDetails.size()));
    return new LocationResponse(MessageCode.GET_LOCATION_FOR_SITE_SUCCESS, locationDetails);
  }
}
