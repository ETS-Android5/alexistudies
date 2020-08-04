package com.google.cloud.healthcare.fdamystudies.controller;

import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.asJsonString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.cloud.healthcare.fdamystudies.beans.UserProfileRequest;
import com.google.cloud.healthcare.fdamystudies.common.ApiEndpoint;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.helper.TestDataHelper;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import com.google.cloud.healthcare.fdamystudies.service.UserProfileService;

public class UserProfileControllerTest extends BaseMockIT {

  @Autowired private UserProfileController controller;

  @Autowired private UserProfileService userProfileService;

  @Autowired private TestDataHelper testDataHelper;

  private UserRegAdminEntity userRegAdminEntity;

  @Autowired UserRegAdminRepository userRegAdminRepository;

  @Test
  public void contextLoads() {
    assertNotNull(controller);
    assertNotNull(mockMvc);
    assertNotNull(userProfileService);
  }

  @BeforeEach
  public void setUp() {
    userRegAdminEntity = testDataHelper.createUserRegAdmin();
    WireMock.resetAllRequests();
  }

  @Test
  public void shouldReturnUserProfile() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath(), TestDataHelper.ADMIN_AUTH_ID_VALUE)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is(TestDataHelper.FIRST_NAME)))
        .andExpect(jsonPath("$.lastName", is(TestDataHelper.LAST_NAME)))
        .andExpect(jsonPath("$.email", is(TestDataHelper.EMAIL_VALUE)))
        .andExpect(jsonPath("$.superAdmin", is(true)))
        .andExpect(jsonPath("$.message", is(MessageCode.GET_USER_PROFILE_SUCCESS.getMessage())));

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnUserNotExistForUserProfile() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath(), IdGenerator.id())
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_EXISTS.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnUserNotActiveForUserProfile() throws Exception {
    // Step 1: change the status to inactive
    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    // Step 2: Call API and expect error message USER_NOT_ACTIVE
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_PROFILE.getPath(), TestDataHelper.ADMIN_AUTH_ID_VALUE)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_description", is(ErrorCode.USER_NOT_ACTIVE.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldUpdateUserProfile() throws Exception {
    // Step 1: Call API to update user profile
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(getUserProfileRequest()))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk());

    // Step 2: verify updated values
    Optional<UserRegAdminEntity> optUserRegAdminUser =
        userRegAdminRepository.findById(userRegAdminEntity.getId());
    UserRegAdminEntity userRegAdminEntity = optUserRegAdminUser.get();
    assertNotNull(userRegAdminEntity);
    assertEquals("mockit_email_updated@grr.la", userRegAdminEntity.getEmail());
    assertEquals("mockito_updated", userRegAdminEntity.getFirstName());
    assertEquals("mockito_updated_last_name", userRegAdminEntity.getLastName());

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnUserNotExistsForUpdatedUserDetails() throws Exception {
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath(), IdGenerator.id())
                .content(asJsonString(getUserProfileRequest()))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized());

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnUserNotActiveForUpdatedUserDetails() throws Exception {
    // Step 1: change the status to inactive
    userRegAdminEntity.setStatus(CommonConstants.INACTIVE_STATUS);
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    // Step 2: Call API
    HttpHeaders headers = testDataHelper.newCommonHeaders();
    mockMvc
        .perform(
            put(ApiEndpoint.UPDATE_USER_PROFILE.getPath(), userRegAdminEntity.getId())
                .content(asJsonString(getUserProfileRequest()))
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnUserDetailsWithSecurityCode() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", userRegAdminEntity.getSecurityCode())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", notNullValue()))
        .andExpect(jsonPath("$.firstName", is(TestDataHelper.FIRST_NAME)))
        .andExpect(jsonPath("$.lastName", is(TestDataHelper.LAST_NAME)))
        .andExpect(jsonPath("$.email", is(TestDataHelper.EMAIL_VALUE)))
        .andExpect(
            jsonPath(
                "$.message",
                is(MessageCode.GET_USER_PROFILE_WITH_SECURITY_CODE_SUCCESS.getMessage())));

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnNotFoundForUserDetailsWithSecurityCode() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", IdGenerator.id())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.INVALID_SECURITY_CODE.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @Test
  public void shouldReturnUnauthorizedForUserDetailsWithSecurityCode() throws Exception {
    // Step 1: change the security code expire date to before current date
    userRegAdminEntity.setSecurityCodeExpireDate(
        new Timestamp(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()));
    userRegAdminRepository.saveAndFlush(userRegAdminEntity);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", VALID_BEARER_TOKEN);

    // Step 2: Call API and expect error message SECURITY_CODE_EXPIRED
    mockMvc
        .perform(
            get(ApiEndpoint.GET_USER_DETAILS.getPath())
                .headers(headers)
                .param("securityCode", userRegAdminEntity.getSecurityCode())
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.error_description", is(ErrorCode.SECURITY_CODE_EXPIRED.getDescription())));

    verifyTokenIntrospectRequest();
  }

  @AfterEach
  public void cleanUp() {
    testDataHelper.getUserRegAdminRepository().deleteAll();
  }

  public UserProfileRequest getUserProfileRequest() {
    UserProfileRequest userProfileRequest = new UserProfileRequest();
    userProfileRequest.setFirstName("mockito_updated");
    userProfileRequest.setLastName("mockito_updated_last_name");
    userProfileRequest.setEmail("mockit_email_updated@grr.la");
    return userProfileRequest;
  }
}
