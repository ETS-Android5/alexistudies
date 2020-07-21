/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.TestConstants;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.AppEntity;
import com.google.cloud.healthcare.fdamystudies.model.LocationEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantRegistrySiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.ParticipantStudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.SiteEntity;
import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserDetailsEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.service.AppService;

public class AppControllerTest extends BaseMockIT {

  @Autowired private AppController controller;

  @Autowired private AppService appService;

  @Autowired private TestDataHelper testDataHelper;

  private ParticipantRegistrySiteEntity participantRegistrySiteEntity;
  private ParticipantStudyEntity participantStudyEntity;
  private UserRegAdminEntity userRegAdminEntity;
  private AppEntity appEntity;
  private StudyEntity studyEntity;
  private SiteEntity siteEntity;
  private UserDetailsEntity userDetailsEntity;
  private LocationEntity locationEntity;

  @BeforeEach
  public void setUp() {
    userRegAdminEntity = testDataHelper.createUserRegAdminEntity();
    appEntity = testDataHelper.createAppEntity(userRegAdminEntity);
    studyEntity = testDataHelper.createStudyEntity(userRegAdminEntity, appEntity);
    siteEntity = testDataHelper.createSiteEntity(studyEntity, userRegAdminEntity, appEntity);
    userDetailsEntity = testDataHelper.createUserDetails(appEntity);
    participantRegistrySiteEntity =
        testDataHelper.createParticipantRegistrySite(siteEntity, studyEntity);
    participantStudyEntity =
        testDataHelper.createParticipantStudyEntity(
            siteEntity, studyEntity, participantRegistrySiteEntity);
  }

  @Test
  public void contextLoads() {
    assertNotNull(controller);
    assertNotNull(mockMvc);
    assertNotNull(appService);
  }

  @Test
  public void shouldReturnAppsRegisteredByUser() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    headers.set(TestConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    mockMvc
        .perform(get(ApiEndpoint.GET_APPS.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.apps").isArray())
        .andExpect(jsonPath("$.studyPermissionCount").value(1));
  }

  @Test
  public void shouldReturnBadRequestForGetApps() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    mockMvc
        .perform(get(ApiEndpoint.GET_APPS.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations[0].path").value("userId"))
        .andExpect(jsonPath("$.violations[0].message").value("header is required"));
  }

  @Test
  public void shouldNotReturnApp() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    headers.add(TestConstants.USER_ID_HEADER, IdGenerator.id());

    mockMvc
        .perform(get(ApiEndpoint.GET_APPS.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description").value(ErrorCode.APP_NOT_FOUND.getDescription()));
  }

  @Test
  public void shouldReturnAppsWithOptionalStudiesAndSites() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    headers.set(TestConstants.USER_ID_HEADER, userRegAdminEntity.getId());
    String[] fields = {"studies", "sites"};
    studyEntity.setAppInfo(appEntity);
    siteEntity.setStudy(studyEntity);
    locationEntity = testDataHelper.createLocation();
    siteEntity.setLocation(locationEntity);
    testDataHelper.getSiteRepository().save(siteEntity);
    mockMvc
        .perform(
            get(ApiEndpoint.GET_APPS.getPath())
                .headers(headers)
                .contextPath(getContextPath())
                .queryParam("fields", fields))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.apps").isArray())
        .andExpect(jsonPath("$.apps[0].studies").isArray())
        .andExpect(jsonPath("$.apps[0].studies[0].sites").isArray())
        .andExpect(jsonPath("$.apps[0].customId").value("MyStudies-Id-1"))
        .andExpect(jsonPath("$.apps[0].name").value("MyStudies-1"));
    testDataHelper.getLocationRepository().delete(locationEntity);
  }

  @Test
  public void shouldNotReturnAppForGetAppDetails() throws Exception {
    HttpHeaders headers = newCommonHeaders();
    headers.add(TestConstants.USER_ID_HEADER, IdGenerator.id());

    mockMvc
        .perform(get(ApiEndpoint.GET_APPS.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error_description").value(ErrorCode.APP_NOT_FOUND.getDescription()));
  }

  @Test
  public void shouldReturnBadRequestForGetAppDetails() throws Exception {
    HttpHeaders headers = newCommonHeaders();

    mockMvc
        .perform(get(ApiEndpoint.GET_APPS.getPath()).headers(headers).contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations[0].path").value("userId"))
        .andExpect(jsonPath("$.violations[0].message").value("header is required"));
  }

  public HttpHeaders newCommonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @AfterEach
  public void cleanUp() {
    testDataHelper.getParticipantStudyRepository().delete(participantStudyEntity);
    testDataHelper.getParticipantRegistrySiteRepository().delete(participantRegistrySiteEntity);
    testDataHelper.getUserDetailsRepository().delete(userDetailsEntity);
    testDataHelper.getSiteRepository().deleteAll();
    testDataHelper.getStudyRepository().delete(studyEntity);
    testDataHelper.getAppRepository().delete(appEntity);
    testDataHelper.getUserRegAdminRepository().delete(userRegAdminEntity);
  }
}
