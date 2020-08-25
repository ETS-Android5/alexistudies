/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ImportParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantRequest;
import com.google.cloud.healthcare.fdamystudies.beans.InviteParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantRegistryResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantStatusRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ParticipantStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteRequest;
import com.google.cloud.healthcare.fdamystudies.beans.SiteResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SiteStatusResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.OnboardingStatus;
import com.google.cloud.healthcare.fdamystudies.mapper.AuditEventMapper;
import com.google.cloud.healthcare.fdamystudies.service.SiteService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.google.cloud.healthcare.fdamystudies.common.CommonConstants.USER_ID_HEADER;

@RestController
public class SiteController {

  private static final String STATUS_LOG = "status=%d ";

  private static final String BEGIN_REQUEST_LOG = "%s request";

  private XLogger logger = XLoggerFactory.getXLogger(SiteController.class.getName());

  @Autowired private SiteService siteService;

  @PostMapping(
      value = "/sites",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SiteResponse> addNewSite(
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @Valid @RequestBody SiteRequest siteRequest,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    siteRequest.setUserId(userId);
    SiteResponse siteResponse = siteService.addSite(siteRequest, auditRequest);

    logger.exit(
        String.format(
            "status=%d and siteId=%s", siteResponse.getHttpStatusCode(), siteResponse.getSiteId()));

    return ResponseEntity.status(siteResponse.getHttpStatusCode()).body(siteResponse);
  }

  @PutMapping(
      value = "/sites/{siteId}/decommission",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SiteStatusResponse> decomissionSite(
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @PathVariable String siteId,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    SiteStatusResponse decomissionSiteResponse =
        siteService.toggleSiteStatus(userId, siteId, auditRequest);

    logger.exit(String.format(STATUS_LOG, decomissionSiteResponse.getHttpStatusCode()));
    return ResponseEntity.status(decomissionSiteResponse.getHttpStatusCode())
        .body(decomissionSiteResponse);
  }

  @PostMapping(
      value = "/sites/{siteId}/participants",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ParticipantResponse> addNewParticipant(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @Valid @RequestBody ParticipantDetailRequest participant,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    participant.setSiteId(siteId);
    ParticipantResponse participantResponse =
        siteService.addNewParticipant(participant, userId, auditRequest);
    logger.exit(String.format(STATUS_LOG, participantResponse.getHttpStatusCode()));
    return ResponseEntity.status(participantResponse.getHttpStatusCode()).body(participantResponse);
  }

  @PostMapping("/sites/{siteId}/participants/invite")
  public ResponseEntity<InviteParticipantResponse> inviteParticipants(
      @Valid @RequestBody InviteParticipantRequest inviteParticipantRequest,
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    inviteParticipantRequest.setSiteId(siteId);
    inviteParticipantRequest.setUserId(userId);
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);
    InviteParticipantResponse inviteParticipantResponse =
        siteService.inviteParticipants(inviteParticipantRequest, auditRequest);

    logger.exit(String.format(STATUS_LOG, inviteParticipantResponse.getHttpStatusCode()));
    return ResponseEntity.status(inviteParticipantResponse.getHttpStatusCode())
        .body(inviteParticipantResponse);
  }

  @GetMapping("/sites")
  public ResponseEntity<SiteDetailsResponse> getSites(
      @RequestHeader(name = USER_ID_HEADER) String userId, HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());

    SiteDetailsResponse siteDetails = siteService.getSites(userId);

    logger.exit(String.format(STATUS_LOG, siteDetails.getHttpStatusCode()));
    return ResponseEntity.status(siteDetails.getHttpStatusCode()).body(siteDetails);
  }

  @GetMapping("/sites/{participantRegistrySiteId}/participant")
  public ResponseEntity<ParticipantDetailsResponse> getParticipantDetails(
      @PathVariable String participantRegistrySiteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    ParticipantDetailsResponse participantDetails =
        siteService.getParticipantDetails(participantRegistrySiteId, userId, auditRequest);

    logger.exit(String.format(STATUS_LOG, participantDetails.getHttpStatusCode()));
    return ResponseEntity.status(participantDetails.getHttpStatusCode()).body(participantDetails);
  }

  @GetMapping(value = "/sites/{siteId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ParticipantRegistryResponse> getSiteParticipant(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @RequestParam(name = "onboardingStatus", required = false) String onboardingStatus,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    if (StringUtils.isNotEmpty(onboardingStatus)
        && OnboardingStatus.fromCode(onboardingStatus) == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ParticipantRegistryResponse(ErrorCode.INVALID_ONBOARDING_STATUS));
    }

    ParticipantRegistryResponse participants =
        siteService.getParticipants(userId, siteId, onboardingStatus, auditRequest);
    logger.exit(String.format(STATUS_LOG, participants.getHttpStatusCode()));
    return ResponseEntity.status(participants.getHttpStatusCode()).body(participants);
  }

  @PostMapping(
      value = "/sites/{siteId}/participants/import",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ImportParticipantResponse> importParticipants(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @RequestParam("file") MultipartFile inputFile,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    ImportParticipantResponse participants =
        siteService.importParticipants(userId, siteId, inputFile, auditRequest);
    logger.exit(String.format(STATUS_LOG, participants.getHttpStatusCode()));
    return ResponseEntity.status(participants.getHttpStatusCode()).body(participants);
  }

  @CrossOrigin(maxAge = 3600)
  @PatchMapping("/sites/{siteId}/participants/status")
  public ResponseEntity<ParticipantStatusResponse> updateOnboardingStatus(
      @PathVariable String siteId,
      @RequestHeader(name = USER_ID_HEADER) String userId,
      @Valid @RequestBody ParticipantStatusRequest participantStatusRequest,
      HttpServletRequest request) {
    logger.entry(BEGIN_REQUEST_LOG, request.getRequestURI());
    AuditLogEventRequest auditRequest = AuditEventMapper.fromHttpServletRequest(request);

    participantStatusRequest.setSiteId(siteId);
    participantStatusRequest.setUserId(userId);
    ParticipantStatusResponse response =
        siteService.updateOnboardingStatus(participantStatusRequest, auditRequest);

    logger.exit(String.format(STATUS_LOG, response.getHttpStatusCode()));
    return ResponseEntity.status(response.getHttpStatusCode()).body(response);
  }
}
