package com.google.cloud.healthcare.fdamystudies.controller.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.healthcare.fdamystudies.beans.EmailIdVerificationForm;
import com.google.cloud.healthcare.fdamystudies.common.BaseMockIT;
import com.google.cloud.healthcare.fdamystudies.controller.VerifyEmailIdController;
import com.google.cloud.healthcare.fdamystudies.dao.UserProfileManagementDaoImpl;
import com.google.cloud.healthcare.fdamystudies.repository.UserDetailsBORepository;
import com.google.cloud.healthcare.fdamystudies.service.CommonService;
import com.google.cloud.healthcare.fdamystudies.testutils.Constants;
import com.google.cloud.healthcare.fdamystudies.testutils.TestUtils;
import com.google.cloud.healthcare.fdamystudies.usermgmt.model.AppInfoDetailsBO;
import com.google.cloud.healthcare.fdamystudies.usermgmt.model.UserDetailsBO;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class VerifyEmailIdControllerTest extends BaseMockIT {

  private static final int VERIFIED_STATUS = 1;

  private static final String VERIFY_EMAIL_ID_PATH = "/myStudiesUserMgmtWS/verifyEmailId";

  @Autowired private VerifyEmailIdController controller;

  @Autowired private CommonService commonService;

  @Autowired private UserDetailsBORepository repository;

  @Autowired private UserProfileManagementDaoImpl userProfileDao;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void contextLoads() {
    assertNotNull(controller);
    assertNotNull(mockMvc);
    assertNotNull(commonService);
  }

  @Test
  public void shouldReturnBadRequestForInvalidContent() throws Exception {
    HttpHeaders headers =
        TestUtils.getCommonHeaders(
            Constants.APP_ID_HEADER, Constants.ORG_ID_HEADER, Constants.USER_ID_HEADER);
    headers.set(Constants.USER_ID_HEADER, Constants.USER_ID_FOR_VERIFY_EMAIL_ID);
    // invalid code
    String requestJson = getEmailIdVerificationForm(Constants.INVALID_CODE, "abc1234@gmail.com");
    mockMvc
        .perform(
            post(VERIFY_EMAIL_ID_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
        .andExpect(jsonPath("$.message", is(Constants.INVALID_EMAIL_CODE)));

    verifyTokenIntrospectRequest(1);

    // invalid emailId
    requestJson = getEmailIdVerificationForm(Constants.CODE, Constants.INVALID_EMAIL);
    mockMvc
        .perform(
            post(VERIFY_EMAIL_ID_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
        .andExpect(jsonPath("$.message", is(Constants.EMAIL_NOT_EXIST)));

    verifyTokenIntrospectRequest(2);
  }

  @Test
  @Disabled
  public void shouldUpdateEmailStatusToVerified() throws Exception {
    HttpHeaders headers =
        TestUtils.getCommonHeaders(
            Constants.APP_ID_HEADER, Constants.ORG_ID_HEADER, Constants.USER_ID_HEADER);

    String requestJson = getEmailIdVerificationForm(Constants.CODE, Constants.EMAIL);

    mockMvc
        .perform(
            post(VERIFY_EMAIL_ID_PATH)
                .content(requestJson)
                .headers(headers)
                .contextPath(getContextPath()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verified").value(Boolean.TRUE));

    // get list of userDetails by emailId
    List<UserDetailsBO> userDetailsList = repository.findByEmail(Constants.EMAIL);
    UserDetailsBO userDetailsBO =
        userDetailsList
            .stream()
            .filter(
                user -> {
                  AppInfoDetailsBO appDetail =
                      userProfileDao.getAppPropertiesDetailsByAppId(user.getAppInfoId());
                  return StringUtils.equals(user.getEmail(), Constants.EMAIL)
                      && StringUtils.equals(appDetail.getAppId(), Constants.APP_ID_VALUE);
                })
            .findAny()
            .orElse(null);
    assertNotNull(userDetailsBO);
    assertTrue(VERIFIED_STATUS == userDetailsBO.getStatus());

    verify(1, putRequestedFor(urlEqualTo("/oauth-scim-service/users/" + Constants.VALID_USER_ID)));

    verifyTokenIntrospectRequest(1);
  }

  private String getEmailIdVerificationForm(String code, String emailId)
      throws JsonProcessingException {
    EmailIdVerificationForm emailIdVerificationForm = new EmailIdVerificationForm(code, emailId);
    return getObjectMapper().writeValueAsString(emailIdVerificationForm);
  }

  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
