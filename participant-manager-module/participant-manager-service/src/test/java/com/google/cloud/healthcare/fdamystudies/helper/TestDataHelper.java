/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.helper;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.ACTIVE_STATUS;
import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.NO;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.CUSTOM_ID_VALUE;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.LOCATION_DESCRIPTION_VALUE;
import static com.google.cloud.healthcare.fdamystudies.common.TestConstants.LOCATION_NAME_VALUE;
import static com.google.cloud.healthcare.fdamystudies.util.Constants.EDIT_VALUE;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.AppPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.OrgInfoEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.SitePermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyConsentEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyPermissionEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.AppRepository;
import com.google.cloud.healthcare.fdamystudies.repository.LocationRepository;
import com.google.cloud.healthcare.fdamystudies.repository.OrgInfoRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantRegistrySiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.ParticipantStudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SiteRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyConsentRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserDetailsRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;

import lombok.Getter;

@Getter
@Component
public class TestDataHelper {

  public static final String LAST_NAME = "mockito_last_name";

  public static final String FIRST_NAME = "mockito";

  public static final String ADMIN_AUTH_ID_VALUE =
      "TuKUeFdyWz4E2A1-LqQcoYKBpMsfLnl-KjiuRFuxWcM3sQg";

  public static final String EMAIL_VALUE = "mockit_email@grr.la";

  @Autowired private UserRegAdminRepository userRegAdminRepository;

  @Autowired private StudyRepository studyRepository;

  @Autowired private LocationRepository locationRepository;

  @Autowired private StudyPermissionRepository studyPermissionRepository;

  @Autowired SitePermissionRepository sitePermissionRepository;

  @Autowired AppPermissionRepository appPermissionRepository;

  @Autowired AppRepository appRepository;

  @Autowired private SiteRepository siteRepository;

  @Autowired private UserDetailsRepository userDetailsRepository;

  @Autowired private ParticipantRegistrySiteRepository participantRegistrySiteRepository;

  @Autowired private ParticipantStudyRepository participantStudyRepository;

  @Autowired private AppPropertyConfig appConfig;

  @Autowired private OrgInfoRepository orgInfoRepository;

  @Autowired private StudyConsentRepository studyConsentRepository;

  public UserRegAdminEntity newUserRegAdminEntity() {
    UserRegAdminEntity userRegAdminEntity = new UserRegAdminEntity();
    userRegAdminEntity.setEmail(EMAIL_VALUE);
    userRegAdminEntity.setFirstName(FIRST_NAME);
    userRegAdminEntity.setLastName(LAST_NAME);
    userRegAdminEntity.setEditPermission(Permission.READ_EDIT.value());
    userRegAdminEntity.setStatus(CommonConstants.ACTIVE_STATUS);
    userRegAdminEntity.setUrAdminAuthId(ADMIN_AUTH_ID_VALUE);
    userRegAdminEntity.setSuperAdmin(true);
    userRegAdminEntity.setSecurityCode("xnsxU1Ax1V2Xtpk-qNLeiZ-417JiqyjytC-706-km6gCq9HAXNYWd8");
    userRegAdminEntity.setSecurityCodeExpireDate(
        new Timestamp(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()));
    return userRegAdminEntity;
  }

  public UserRegAdminEntity createUserRegAdmin() {
    UserRegAdminEntity userRegAdminEntity = newUserRegAdminEntity();
    return userRegAdminRepository.saveAndFlush(userRegAdminEntity);
  }

  public StudyEntity newStudyEntity() {
    StudyEntity studyEntity = new StudyEntity();
    studyEntity.setCustomId("StudyID01");
    studyEntity.setCategory("Public Health");
    studyEntity.setEnrolling("Yes");
    studyEntity.setStatus("Active");
    studyEntity.setName("Covid19");
    studyEntity.setSponsor("FDA");
    return studyEntity;
  }

  public AppEntity newAppEntity() {
    AppEntity appEntity = new AppEntity();
    appEntity.setAppId("MyStudies-Id-1");
    appEntity.setAppName("MyStudies-1");
    return appEntity;
  }

  public UserRegAdminEntity createUserRegAdminEntity() {
    return userRegAdminRepository.saveAndFlush(newUserRegAdminEntity());
  }

  public AppEntity createAppEntity(UserRegAdminEntity userEntity) {
    AppEntity appEntity = newAppEntity();
    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    appPermissionEntity.setEdit(EDIT_VALUE);
    appPermissionEntity.setUrAdminUser(userEntity);
    appPermissionEntity.setAppInfo(appEntity);
    appEntity.addAppPermissionEntity(appPermissionEntity);
    return appRepository.saveAndFlush(appEntity);
  }

  public StudyEntity createStudyEntity(UserRegAdminEntity userEntity, AppEntity appEntity) {
    StudyEntity studyEntity = new StudyEntity();
    studyEntity.setType("CLOSE");
    studyEntity.setName("COVID Study");
    StudyPermissionEntity studyPermissionEntity = new StudyPermissionEntity();
    studyPermissionEntity.setUrAdminUser(userEntity);
    studyPermissionEntity.setEdit(EDIT_VALUE);
    studyPermissionEntity.setAppInfo(appEntity);
    studyEntity.addStudyPermissionEntity(studyPermissionEntity);
    return studyRepository.saveAndFlush(studyEntity);
  }

  public SiteEntity createSiteEntity(
      StudyEntity studyEntity, UserRegAdminEntity urAdminUser, AppEntity appEntity) {
    SiteEntity siteEntity = newSiteEntity();
    SitePermissionEntity sitePermissionEntity = new SitePermissionEntity();
    sitePermissionEntity.setCanEdit(Permission.READ_EDIT.value());
    sitePermissionEntity.setCanEdit(EDIT_VALUE);
    sitePermissionEntity.setStudy(studyEntity);
    sitePermissionEntity.setUrAdminUser(urAdminUser);
    sitePermissionEntity.setAppInfo(appEntity);
    siteEntity.addSitePermissionEntity(sitePermissionEntity);
    return siteRepository.saveAndFlush(siteEntity);
  }

  public ParticipantRegistrySiteEntity createParticipantRegistrySite(
      SiteEntity siteEntity, StudyEntity studyEntity) {
    ParticipantRegistrySiteEntity participantRegistrySiteEntity =
        new ParticipantRegistrySiteEntity();
    participantRegistrySiteEntity.setEnrollmentToken("BSEEMNH6");
    participantRegistrySiteEntity.setInvitationCount(2L);
    participantRegistrySiteEntity.setSite(siteEntity);
    participantRegistrySiteEntity.setStudy(studyEntity);
    return participantRegistrySiteRepository.saveAndFlush(participantRegistrySiteEntity);
  }

  public ParticipantStudyEntity createParticipantStudyEntity(
      SiteEntity siteEntity,
      StudyEntity studyEntity,
      ParticipantRegistrySiteEntity participantRegistrySiteEntity) {
    ParticipantStudyEntity participantStudyEntity = new ParticipantStudyEntity();
    participantStudyEntity.setSite(siteEntity);
    participantStudyEntity.setStudy(studyEntity);
    participantStudyEntity.setParticipantRegistrySite(participantRegistrySiteEntity);
    return participantStudyRepository.saveAndFlush(participantStudyEntity);
  }

  public UserDetailsEntity newUserDetails() {
    UserDetailsEntity userDetailsEntity = new UserDetailsEntity();
    userDetailsEntity.setEmail(EMAIL_VALUE);
    userDetailsEntity.setStatus(1);
    userDetailsEntity.setFirstName(FIRST_NAME);
    userDetailsEntity.setLastName(LAST_NAME);
    userDetailsEntity.setLocalNotificationFlag(false);
    userDetailsEntity.setRemoteNotificationFlag(false);
    userDetailsEntity.setTouchId(false);
    userDetailsEntity.setUsePassCode(false);
    return userDetailsEntity;
  }

  public UserDetailsEntity createUserDetails(AppEntity appEntity) {
    UserDetailsEntity userDetailsEntity = newUserDetails();
    userDetailsEntity.setAppInfo(appEntity);
    return userDetailsRepository.saveAndFlush(userDetailsEntity);
  }

  public SiteEntity newSiteEntity() {
    SiteEntity siteEntity = new SiteEntity();
    siteEntity.setName("Boston");
    siteEntity.setStatus(ACTIVE_STATUS);
    return siteEntity;
  }

  public LocationEntity newLocationEntity() {
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.setCustomId(CUSTOM_ID_VALUE);
    locationEntity.setDescription(LOCATION_DESCRIPTION_VALUE);
    locationEntity.setName(LOCATION_NAME_VALUE);
    locationEntity.setStatus(ACTIVE_STATUS);
    locationEntity.setIsDefault(NO);
    return locationEntity;
  }

  public LocationEntity createLocation() {
    LocationEntity locationEntity = newLocationEntity();
    SiteEntity siteEntity = newSiteEntity();
    locationEntity.addSiteEntity(siteEntity);
    siteEntity.setStudy(newStudyEntity());
    return locationRepository.saveAndFlush(locationEntity);
  }

  public OrgInfoEntity createOrgInfo() {
    OrgInfoEntity orgInfoEntity = new OrgInfoEntity();
    orgInfoEntity.setName("OrgName");
    return orgInfoRepository.saveAndFlush(orgInfoEntity);
  }

  public StudyConsentEntity createStudyConsentEntity(ParticipantStudyEntity participantStudy) {
    StudyConsentEntity studyConsent = new StudyConsentEntity();
    studyConsent.setPdfPath("http://pdfPath");
    studyConsent.setVersion("1.0");
    studyConsent.setParticipantStudy(participantStudy);
    return studyConsentRepository.saveAndFlush(studyConsent);
  }
}
