/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.ACCOUNT_UPDATE_EMAIL_FAILED;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.ACCOUNT_UPDATE_EMAIL_SENT;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.NEW_USER_CREATED;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.NEW_USER_INVITATION_EMAIL_FAILED;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.NEW_USER_INVITATION_EMAIL_SENT;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.USER_RECORD_UPDATED;
import static com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent.USER_REGISTRY_VIEWED;

import com.google.cloud.healthcare.fdamystudies.beans.AdminUserResponse;
import com.google.cloud.healthcare.fdamystudies.beans.AppPermissionDetails;
import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.GetAdminDetailsResponse;
import com.google.cloud.healthcare.fdamystudies.beans.GetUsersResponse;
import com.google.cloud.healthcare.fdamystudies.beans.SitePermissionDetails;
import com.google.cloud.healthcare.fdamystudies.beans.StudyPermissionDetails;
import com.google.cloud.healthcare.fdamystudies.beans.User;
import com.google.cloud.healthcare.fdamystudies.beans.UserAppDetails;
import com.google.cloud.healthcare.fdamystudies.beans.UserAppPermissionRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserSiteDetails;
import com.google.cloud.healthcare.fdamystudies.beans.UserSitePermissionRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserStudyDetails;
import com.google.cloud.healthcare.fdamystudies.beans.UserStudyPermissionRequest;
import com.google.cloud.healthcare.fdamystudies.common.CommonConstants;
import com.google.cloud.healthcare.fdamystudies.common.EmailTemplate;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerAuditLogHelper;
import com.google.cloud.healthcare.fdamystudies.common.ParticipantManagerEvent;
import com.google.cloud.healthcare.fdamystudies.common.Permission;
import com.google.cloud.healthcare.fdamystudies.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.exceptions.ErrorCodeException;
import com.google.cloud.healthcare.fdamystudies.mapper.UserMapper;
import com.google.cloud.healthcare.fdamystudies.model.AppStudySiteInfo;
import com.google.cloud.healthcare.fdamystudies.model.UserAccountEmailSchedulerTaskEntity;
import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;
import com.google.cloud.healthcare.fdamystudies.repository.AppPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.AppRepository;
import com.google.cloud.healthcare.fdamystudies.repository.SitePermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.StudyPermissionRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserAccountEmailSchedulerTaskRepository;
import com.google.cloud.healthcare.fdamystudies.repository.UserRegAdminRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageUserServiceImpl implements ManageUserService {

  private XLogger logger = XLoggerFactory.getXLogger(ManageUserServiceImpl.class.getName());

  @Autowired private UserRegAdminRepository userAdminRepository;

  @Autowired private AppRepository appRepository;

  @Autowired private AppPermissionRepository appPermissionRepository;

  @Autowired private StudyPermissionRepository studyPermissionRepository;

  @Autowired private SitePermissionRepository sitePermissionRepository;

  @Autowired private AppPropertyConfig appConfig;

  @Autowired private EmailService emailService;

  @Autowired private ParticipantManagerAuditLogHelper participantManagerHelper;

  @Autowired
  private UserAccountEmailSchedulerTaskRepository userAccountEmailSchedulerTaskRepository;

  @Autowired private EntityManager entityManger;

  @Override
  @Transactional
  public AdminUserResponse createUser(UserRequest user, AuditLogEventRequest auditRequest) {
    logger.entry(String.format("createUser() with isSuperAdmin=%b", user.isSuperAdmin()));
    ErrorCode errorCode = validateUserRequest(user);
    if (errorCode != null) {
      throw new ErrorCodeException(errorCode);
    }

    AdminUserResponse userResponse =
        user.isSuperAdmin()
            ? saveSuperAdminDetails(user, auditRequest)
            : saveAdminDetails(user, auditRequest);

    String accessLevel = user.isSuperAdmin() ? CommonConstants.SUPER_ADMIN : CommonConstants.ADMIN;
    if (userResponse.getUserId() != null) {
      Map<String, String> map = new HashMap<>();
      map.put(CommonConstants.NEW_USER_ID, userResponse.getUserId());
      map.put("new_user_access_level", accessLevel);
      participantManagerHelper.logEvent(NEW_USER_CREATED, auditRequest, map);
    }

    logger.exit(String.format(CommonConstants.STATUS_LOG, userResponse.getHttpStatusCode()));
    return userResponse;
  }

  private ErrorCode validateUserRequest(UserRequest user) {
    logger.entry("validateUserRequest()");
    Optional<UserRegAdminEntity> optAdminDetails =
        userAdminRepository.findById(user.getSuperAdminUserId());

    UserRegAdminEntity admin =
        optAdminDetails.orElseThrow(() -> new ErrorCodeException(ErrorCode.USER_NOT_FOUND));
    if (!admin.isSuperAdmin()) {
      throw new ErrorCodeException(ErrorCode.NOT_SUPER_ADMIN_ACCESS);
    }

    Optional<UserRegAdminEntity> optUsers = userAdminRepository.findByEmail(user.getEmail());
    logger.exit("Successfully validated user request");
    return optUsers.isPresent() ? ErrorCode.EMAIL_EXISTS : null;
  }

  private void addSelectedAppStudySiteIds(
      List<UserAppPermissionRequest> apps,
      List<AppPermissionDetails> appPermissions,
      List<StudyPermissionDetails> studyPermissions,
      List<SitePermissionDetails> sitePermissions) {

    Predicate<UserAppPermissionRequest> appPredicate = app -> app.isSelected();
    Predicate<UserStudyPermissionRequest> studyPredicate = study -> study.isSelected();
    Predicate<UserSitePermissionRequest> sitePredicate = site -> site.isSelected();

    if (apps != null) {
      List<UserAppPermissionRequest> selectedApps =
          (List<UserAppPermissionRequest>)
              CollectionUtils.emptyIfNull(
                  apps.stream().filter(appPredicate).collect(Collectors.toList()));

      for (UserAppPermissionRequest appRequest : selectedApps) {
        AppPermissionDetails appPermissionDetails = new AppPermissionDetails();
        appPermissionDetails.setAppId(appRequest.getId());
        appPermissionDetails.setEdit(appRequest.getPermission());
        appPermissions.add(appPermissionDetails);
      }

      for (UserAppPermissionRequest appPermission : apps) {
        List<UserStudyPermissionRequest> selectedStudies =
            CollectionUtils.emptyIfNull(appPermission.getStudies())
                .stream()
                .filter(studyPredicate)
                .collect(Collectors.toList());

        for (UserStudyPermissionRequest studyRequest : selectedStudies) {
          StudyPermissionDetails studyPermissionDetails = new StudyPermissionDetails();
          studyPermissionDetails.setAppId(appPermission.getId());
          studyPermissionDetails.setStudyId(studyRequest.getStudyId());
          studyPermissionDetails.setEdit(studyRequest.getPermission());
          studyPermissions.add(studyPermissionDetails);
        }

        if (appPermission.getStudies() != null) {
          for (UserStudyPermissionRequest studyPermission : appPermission.getStudies()) {
            List<UserSitePermissionRequest> selectedSites =
                CollectionUtils.emptyIfNull(studyPermission.getSites())
                    .stream()
                    .filter(sitePredicate)
                    .collect(Collectors.toList());

            for (UserSitePermissionRequest siteRequest : selectedSites) {
              SitePermissionDetails sitePermissionDetails = new SitePermissionDetails();
              sitePermissionDetails.setAppId(appPermission.getId());
              sitePermissionDetails.setStudyId(studyPermission.getStudyId());
              sitePermissionDetails.setSiteId(siteRequest.getSiteId());
              sitePermissionDetails.setCanEdit(siteRequest.getPermission());
              sitePermissions.add(sitePermissionDetails);
            }
          }
        }
      }
    }
  }

  private AdminUserResponse saveAdminDetails(UserRequest user, AuditLogEventRequest auditRequest) {
    logger.entry("saveAdminDetails()");
    List<AppPermissionDetails> appPermissions = new ArrayList<>();
    List<StudyPermissionDetails> studyPermissions = new ArrayList<>();
    List<SitePermissionDetails> sitePermissions = new ArrayList<>();
    if (user.getApps() != null && !user.getApps().isEmpty()) {
      addSelectedAppStudySiteIds(user.getApps(), appPermissions, studyPermissions, sitePermissions);
    }

    if (user.getManageLocations() == Permission.NO_PERMISSION.value()
        && appPermissions.isEmpty()
        && studyPermissions.isEmpty()
        && sitePermissions.isEmpty()) {
      throw new ErrorCodeException(ErrorCode.PERMISSION_MISSING);
    }

    UserRegAdminEntity adminDetails =
        UserMapper.fromUserRequest(user, Long.valueOf(appConfig.getSecurityCodeExpireDate()));
    adminDetails = userAdminRepository.saveAndFlush(adminDetails);

    if (user.getApps() != null) {
      saveAppLevelPermissions(user, adminDetails, appPermissions);
      saveStudyLevelPermissions(user, adminDetails, studyPermissions);
      saveSiteLevelPermissions(user, adminDetails, sitePermissions);
    }

    UserAccountEmailSchedulerTaskEntity emailTaskEntity =
        UserMapper.toUserAccountEmailSchedulerTaskEntity(
            auditRequest, adminDetails, EmailTemplate.ACCOUNT_CREATED_EMAIL_TEMPLATE);
    userAccountEmailSchedulerTaskRepository.saveAndFlush(emailTaskEntity);

    logger.exit("Successfully saved admin details.");
    return new AdminUserResponse(MessageCode.ADD_NEW_USER_SUCCESS, adminDetails.getId());
  }

  // SQL Injection? No. No need to sanitize appId, userId values as these fields have FK
  // constraints defined on the table
  private void saveAppLevelPermissions(
      UserRequest user,
      UserRegAdminEntity adminDetails,
      List<AppPermissionDetails> appPermissions) {
    logger.entry("saveAppLevelPermissions()");
    if (CollectionUtils.isEmpty(appPermissions)) {
      return;
    }

    StringBuilder sqlInsertBuilder =
        new StringBuilder(
            "insert into app_permissions (id, ur_admin_user_id, app_info_id, edit, created_time, created_by) values");
    appPermissions.forEach(
        (AppPermissionDetails appPermssion) -> {
          sqlInsertBuilder.append("(");
          sqlInsertBuilder.append("'" + IdGenerator.id() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + adminDetails.getId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + appPermssion.getAppId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + appPermssion.getEdit() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + new Timestamp(Instant.now().toEpochMilli()) + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + user.getSuperAdminUserId() + "'");
          sqlInsertBuilder.append(")");
          sqlInsertBuilder.append(", ");
        });
    sqlInsertBuilder.deleteCharAt(sqlInsertBuilder.lastIndexOf(","));

    entityManger.createNativeQuery(sqlInsertBuilder.toString()).executeUpdate();
    logger.exit("Successfully saved app level permissions");
  }

  // SQL Injection? No. No need to sanitize appId,studyId, userId values as these fields have FK
  // constraints defined on the table
  private void saveStudyLevelPermissions(
      UserRequest user,
      UserRegAdminEntity adminDetails,
      List<StudyPermissionDetails> studyPermissions) {
    logger.entry("saveStudyLevelPermissions()");
    if (CollectionUtils.isEmpty(studyPermissions)) {
      return;
    }

    StringBuilder sqlInsertBuilder =
        new StringBuilder(
            "insert into study_permissions (id, ur_admin_user_id, app_info_id, study_id, edit, created_time, created_by) values");
    studyPermissions.forEach(
        (StudyPermissionDetails studyPermssion) -> {
          sqlInsertBuilder.append("(");
          sqlInsertBuilder.append("'" + IdGenerator.id() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + adminDetails.getId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + studyPermssion.getAppId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + studyPermssion.getStudyId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + studyPermssion.getEdit() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + new Timestamp(Instant.now().toEpochMilli()) + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + user.getSuperAdminUserId() + "'");
          sqlInsertBuilder.append(")");
          sqlInsertBuilder.append(", ");
        });
    sqlInsertBuilder.deleteCharAt(sqlInsertBuilder.lastIndexOf(","));

    entityManger.createNativeQuery(sqlInsertBuilder.toString()).executeUpdate();
    logger.exit("Successfully saved study level permissions");
  }

  // SQL Injection? No. No need to sanitize appId, studyId, siteId, userId values as these fields
  // have FK constraints defined on the table
  private void saveSiteLevelPermissions(
      UserRequest user,
      UserRegAdminEntity adminDetails,
      List<SitePermissionDetails> sitePermissions) {
    logger.entry("saveSiteLevelPermissions()");
    if (CollectionUtils.isEmpty(sitePermissions)) {
      return;
    }

    StringBuilder sqlInsertBuilder =
        new StringBuilder(
            "insert into sites_permissions (id, ur_admin_user_id, app_info_id, study_id, site_id, edit, created_time, created_by) values");
    sitePermissions.forEach(
        (SitePermissionDetails sitePermssion) -> {
          sqlInsertBuilder.append("(");
          sqlInsertBuilder.append("'" + IdGenerator.id() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + adminDetails.getId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + sitePermssion.getAppId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + sitePermssion.getStudyId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + sitePermssion.getSiteId() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + sitePermssion.getCanEdit() + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + new Timestamp(Instant.now().toEpochMilli()) + "'");
          sqlInsertBuilder.append(",");
          sqlInsertBuilder.append("'" + user.getSuperAdminUserId() + "'");
          sqlInsertBuilder.append(")");
          sqlInsertBuilder.append(", ");
        });

    sqlInsertBuilder.deleteCharAt(sqlInsertBuilder.lastIndexOf(","));

    entityManger.createNativeQuery(sqlInsertBuilder.toString()).executeUpdate();
    logger.exit("Successfully saved site level permissions");
  }

  private AdminUserResponse saveSuperAdminDetails(
      UserRequest user, AuditLogEventRequest auditRequest) {
    logger.entry("saveSuperAdminDetails()");
    UserRegAdminEntity superAdminDetails =
        UserMapper.fromUserRequest(user, Long.valueOf(appConfig.getSecurityCodeExpireDate()));

    superAdminDetails = userAdminRepository.saveAndFlush(superAdminDetails);

    UserAccountEmailSchedulerTaskEntity emailTaskEntity =
        UserMapper.toUserAccountEmailSchedulerTaskEntity(
            auditRequest, superAdminDetails, EmailTemplate.ACCOUNT_CREATED_EMAIL_TEMPLATE);
    userAccountEmailSchedulerTaskRepository.saveAndFlush(emailTaskEntity);

    logger.exit(String.format(CommonConstants.MESSAGE_CODE_LOG, MessageCode.ADD_NEW_USER_SUCCESS));
    return new AdminUserResponse(MessageCode.ADD_NEW_USER_SUCCESS, superAdminDetails.getId());
  }

  @Override
  @Transactional
  public AdminUserResponse updateUser(
      UserRequest user, String adminUserId, AuditLogEventRequest auditRequest) {
    logger.entry(String.format("updateUser() with isSuperAdmin=%b", user.isSuperAdmin()));
    validateUpdateUserRequest(user);

    AdminUserResponse userResponse =
        user.isSuperAdmin()
            ? updateSuperAdminDetails(user, auditRequest)
            : updateAdminDetails(user, auditRequest);
    String accessLevel = user.isSuperAdmin() ? CommonConstants.SUPER_ADMIN : CommonConstants.ADMIN;
    if (MessageCode.UPDATE_USER_SUCCESS.getMessage().equals(userResponse.getMessage())) {
      Map<String, String> map = new HashedMap<>();
      map.put(CommonConstants.EDITED_USER_ID, user.getId());
      map.put("edited_user_access_level", accessLevel);
      participantManagerHelper.logEvent(USER_RECORD_UPDATED, auditRequest, map);
    }

    logger.exit(String.format(CommonConstants.STATUS_LOG, userResponse.getHttpStatusCode()));
    return userResponse;
  }

  private void validateUpdateUserRequest(UserRequest user) {
    logger.entry("validateUpdateUserRequest()");
    Optional<UserRegAdminEntity> optSuperAdmin =
        userAdminRepository.findById(user.getSuperAdminUserId());
    UserRegAdminEntity admin =
        optSuperAdmin.orElseThrow(() -> new ErrorCodeException(ErrorCode.USER_NOT_FOUND));
    if (!admin.isSuperAdmin()) {
      throw new ErrorCodeException(ErrorCode.NOT_SUPER_ADMIN_ACCESS);
    }

    logger.exit("Successfully validated user request");
  }

  private AdminUserResponse updateSuperAdminDetails(
      UserRequest user, AuditLogEventRequest auditRequest) {
    logger.entry("updateSuperAdminDetails()");
    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(user.getId());

    if (!optAdminDetails.isPresent()) {
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }

    UserRegAdminEntity adminDetails = optAdminDetails.get();
    adminDetails = UserMapper.fromUpdateUserRequest(user, adminDetails);

    userAdminRepository.saveAndFlush(adminDetails);

    deleteAppStudySiteLevelPermissions(user.getId());

    UserAccountEmailSchedulerTaskEntity adminRecordToSendEmail =
        UserMapper.toUserAccountEmailSchedulerTaskEntity(
            auditRequest, adminDetails, EmailTemplate.ACCOUNT_UPDATED_EMAIL_TEMPLATE);
    userAccountEmailSchedulerTaskRepository.saveAndFlush(adminRecordToSendEmail);

    logger.exit(String.format(CommonConstants.MESSAGE_CODE_LOG, MessageCode.UPDATE_USER_SUCCESS));
    return new AdminUserResponse(MessageCode.UPDATE_USER_SUCCESS, adminDetails.getId());
  }

  private AdminUserResponse updateAdminDetails(
      UserRequest user, AuditLogEventRequest auditRequest) {
    logger.entry("updateAdminDetails()");
    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(user.getId());
    UserRegAdminEntity adminDetails =
        optAdminDetails.orElseThrow(() -> new ErrorCodeException(ErrorCode.USER_NOT_FOUND));

    List<AppPermissionDetails> appPermissions = new ArrayList<>();
    List<StudyPermissionDetails> studyPermissions = new ArrayList<>();
    List<SitePermissionDetails> sitePermissions = new ArrayList<>();
    if (user.getApps() != null && !user.getApps().isEmpty()) {
      addSelectedAppStudySiteIds(user.getApps(), appPermissions, studyPermissions, sitePermissions);
    }

    if (user.getManageLocations() == Permission.NO_PERMISSION.value()
        && appPermissions.isEmpty()
        && studyPermissions.isEmpty()
        && sitePermissions.isEmpty()) {
      throw new ErrorCodeException(ErrorCode.PERMISSION_MISSING);
    }

    adminDetails = UserMapper.fromUpdateUserRequest(user, adminDetails);
    userAdminRepository.saveAndFlush(adminDetails);

    deleteAppStudySiteLevelPermissions(user.getId());

    if (CollectionUtils.isNotEmpty(user.getApps())) {
      saveAppLevelPermissions(user, adminDetails, appPermissions);
      saveStudyLevelPermissions(user, adminDetails, studyPermissions);
      saveSiteLevelPermissions(user, adminDetails, sitePermissions);
    }

    UserAccountEmailSchedulerTaskEntity adminRecordToSendEmail =
        UserMapper.toUserAccountEmailSchedulerTaskEntity(
            auditRequest, adminDetails, EmailTemplate.ACCOUNT_UPDATED_EMAIL_TEMPLATE);
    userAccountEmailSchedulerTaskRepository.saveAndFlush(adminRecordToSendEmail);

    logger.exit("Successfully updated admin details.");
    return new AdminUserResponse(MessageCode.UPDATE_USER_SUCCESS, adminDetails.getId());
  }

  private void deleteAppStudySiteLevelPermissions(String userId) {
    logger.entry("deleteAllPermissions()");
    sitePermissionRepository.deleteByAdminUserId(userId);
    studyPermissionRepository.deleteByAdminUserId(userId);
    appPermissionRepository.deleteByAdminUserId(userId);
    logger.exit("Successfully deleted all the assigned permissions.");
  }

  @Override
  public GetAdminDetailsResponse getAdminDetails(
      String signedInUserId, String adminId, boolean includeUnselected) {
    logger.entry("getAdminDetails()");
    validateSignedInUser(signedInUserId);

    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(adminId);

    UserRegAdminEntity adminDetails =
        optAdminDetails.orElseThrow(() -> new ErrorCodeException(ErrorCode.ADMIN_NOT_FOUND));

    User user = UserMapper.prepareUserInfo(adminDetails);
    if (adminDetails.isSuperAdmin()) {
      logger.exit(String.format("superadmin=%b, status=%s", user.isSuperAdmin(), user.getStatus()));
      return new GetAdminDetailsResponse(MessageCode.GET_ADMIN_DETAILS_SUCCESS, user);
    }

    List<AppStudySiteInfo> selectedAppsStudiesSitesInfoList =
        appRepository.findAppsStudiesSitesByUserId(adminId);

    List<String> selectedAppIds = new ArrayList<>();
    selectedAppsStudiesSitesInfoList.forEach(
        p -> {
          if (!selectedAppIds.contains(p.getAppId())) {
            selectedAppIds.add(p.getAppId());
          }
        });

    if (selectedAppIds.isEmpty()) {
      logger.exit(String.format("superadmin=%b, status=%s", user.isSuperAdmin(), user.getStatus()));
      return new GetAdminDetailsResponse(MessageCode.GET_ADMIN_DETAILS_SUCCESS, user);
    }

    Map<String, UserAppDetails> unselectedAppsMap = new HashMap<>();
    Map<String, UserStudyDetails> unselectedStudiesMap = new HashMap<>();
    Map<String, UserSiteDetails> unselectedSitesMap = new HashMap<>();

    if (includeUnselected) {
      putUnselectedAppsStudiesSites(
          adminId, selectedAppIds, unselectedAppsMap, unselectedStudiesMap, unselectedSitesMap);
    }

    Map<String, UserAppDetails> appsMap = new HashMap<>();
    Map<String, UserStudyDetails> studiesMap = new HashMap<>();
    Map<String, UserSiteDetails> sitesMap = new HashMap<>();
    for (AppStudySiteInfo app : selectedAppsStudiesSitesInfoList) {
      UserAppDetails appDetails = null;
      if (!appsMap.containsKey(app.getAppId())) {
        appDetails = UserMapper.toUserAppDetails(app);
        if (unselectedAppsMap.containsKey(app.getAppId())) {
          UserAppDetails unselectedAppsDetails = unselectedAppsMap.get(app.getAppId());
          appDetails.getStudies().addAll(unselectedAppsDetails.getStudies());
          appDetails.setTotalSitesCount(unselectedAppsDetails.getTotalSitesCount());
        }

        appsMap.put(app.getAppId(), appDetails);
      }

      appDetails = appsMap.get(app.getAppId());

      UserStudyDetails userStudyDetails = null;
      if (!studiesMap.containsKey(app.getAppStudyIdKey())) {
        userStudyDetails = UserMapper.toUserStudyDetails(app);
        studiesMap.put(app.getAppStudyIdKey(), userStudyDetails);
        // add all unselected sites
        if (unselectedStudiesMap.containsKey(app.getAppStudyIdKey())) {
          userStudyDetails
              .getSites()
              .addAll(unselectedStudiesMap.get(app.getAppStudyIdKey()).getSites());
        }

        // replace unselected study with selected study
        appDetails
            .getStudies()
            .removeIf(study -> StringUtils.equals(study.getStudyId(), app.getStudyId()));
        appDetails.getStudies().add(userStudyDetails);

        if (userStudyDetails.isSelected()) {
          appDetails.setSelectedStudiesCount(appDetails.getSelectedStudiesCount() + 1);
        }
      }
      userStudyDetails = studiesMap.get(app.getAppStudyIdKey());

      if (StringUtils.isNotEmpty(app.getLocationName())
          && userStudyDetails != null
          && !sitesMap.containsKey(app.getAppStudySiteIdKey())) {
        UserSiteDetails userSiteDetails = UserMapper.toUserSiteDetails(app);
        sitesMap.put(app.getAppStudySiteIdKey(), userSiteDetails);
        // replace unselected site with selected site
        userStudyDetails
            .getSites()
            .removeIf(site -> StringUtils.equals(site.getSiteId(), app.getSiteId()));
        userStudyDetails.getSites().add(userSiteDetails);

        if (userSiteDetails.isSelected()) {
          appDetails.setSelectedSitesCount(appDetails.getSelectedSitesCount() + 1);
          userStudyDetails.setSelectedSitesCount(userStudyDetails.getSelectedSitesCount() + 1);
        }
        userStudyDetails.setTotalSitesCount(userStudyDetails.getSites().size());
        appDetails.setTotalSitesCount(appDetails.getTotalSitesCount() + 1);

        sortUserSites(userStudyDetails);
      }

      sortUserStudies(appDetails);

      appDetails.setTotalStudiesCount(appDetails.getStudies().size());
    }

    List<UserAppDetails> apps = appsMap.values().stream().collect(Collectors.toList());
    List<UserAppDetails> sortedApps =
        apps.stream()
            .sorted(Comparator.comparing(UserAppDetails::getName))
            .collect(Collectors.toList());
    user.getApps().addAll(sortedApps);

    logger.exit(
        String.format(
            "total apps=%d, superadmin=%b, status=%s",
            user.getApps().size(), user.isSuperAdmin(), user.getStatus()));
    return new GetAdminDetailsResponse(MessageCode.GET_ADMIN_DETAILS_SUCCESS, user);
  }

  private void sortUserSites(UserStudyDetails userStudyDetails) {
    List<UserSiteDetails> sortedSites =
        userStudyDetails
            .getSites()
            .stream()
            .sorted(Comparator.comparing(UserSiteDetails::getLocationName))
            .collect(Collectors.toList());
    userStudyDetails.getSites().clear();
    userStudyDetails.getSites().addAll(sortedSites);
  }

  private void sortUserStudies(UserAppDetails appDetails) {
    List<UserStudyDetails> sortedStudies =
        appDetails
            .getStudies()
            .stream()
            .sorted(Comparator.comparing(UserStudyDetails::getStudyName))
            .collect(Collectors.toList());
    appDetails.getStudies().clear();
    appDetails.getStudies().addAll(sortedStudies);
  }

  private void putUnselectedAppsStudiesSites(
      String adminId,
      List<String> selectedAppIds,
      Map<String, UserAppDetails> unselectedAppsMap,
      Map<String, UserStudyDetails> unselectedStudiesMap,
      Map<String, UserSiteDetails> unselectedSitesMap) {

    List<AppStudySiteInfo> unselectedAppsStudiesSitesInfoList =
        appRepository.findUnSelectedAppsStudiesSites(selectedAppIds, adminId);

    for (AppStudySiteInfo app : unselectedAppsStudiesSitesInfoList) {
      UserAppDetails appDetails = null;
      if (!unselectedAppsMap.containsKey(app.getAppId())) {
        appDetails = UserMapper.toUserAppDetails(app);
        unselectedAppsMap.put(app.getAppId(), appDetails);
      }

      appDetails = unselectedAppsMap.get(app.getAppId());
      UserStudyDetails userStudyDetails = null;
      if (!unselectedStudiesMap.containsKey(app.getAppStudyIdKey())) {
        userStudyDetails = UserMapper.toUserStudyDetails(app);
        unselectedStudiesMap.put(app.getAppStudyIdKey(), userStudyDetails);
        appDetails.getStudies().add(userStudyDetails);
      }

      userStudyDetails = unselectedStudiesMap.get(app.getAppStudyIdKey());

      if (userStudyDetails != null
          && StringUtils.isNotEmpty(app.getLocationName())
          && !unselectedSitesMap.containsKey(app.getAppStudySiteIdKey())) {
        UserSiteDetails userSiteDetails = UserMapper.toUserSiteDetails(app);
        unselectedSitesMap.put(app.getAppStudySiteIdKey(), userSiteDetails);
        userStudyDetails.getSites().add(userSiteDetails);
        appDetails.setTotalSitesCount(appDetails.getTotalSitesCount() + 1);
        userStudyDetails.setTotalSitesCount(userStudyDetails.getSites().size());
      }
    }
  }

  private void validateSignedInUser(String adminUserId) {
    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(adminUserId);
    UserRegAdminEntity user =
        optAdminDetails.orElseThrow(() -> new ErrorCodeException(ErrorCode.USER_NOT_FOUND));

    if (!user.isSuperAdmin()) {
      throw new ErrorCodeException(ErrorCode.NOT_SUPER_ADMIN_ACCESS);
    }
  }

  @Override
  public GetUsersResponse getUsers(
      String superAdminUserId, Integer page, Integer limit, AuditLogEventRequest auditRequest) {
    logger.entry("getUsers()");
    validateSignedInUser(superAdminUserId);

    List<User> users = new ArrayList<>();
    List<UserRegAdminEntity> adminList = null;
    if (page != null && limit != null) {
      Page<UserRegAdminEntity> adminPage =
          userAdminRepository.findAll(PageRequest.of(page, limit, Sort.by("created").descending()));
      adminList = (List<UserRegAdminEntity>) CollectionUtils.emptyIfNull(adminPage.getContent());
    } else {
      adminList = userAdminRepository.findAll();
    }

    adminList
        .stream()
        .map(admin -> users.add(UserMapper.prepareUserInfo(admin)))
        .collect(Collectors.toList());

    participantManagerHelper.logEvent(USER_REGISTRY_VIEWED, auditRequest);
    logger.exit(String.format("total users=%d", adminList.size()));
    return new GetUsersResponse(MessageCode.GET_USERS_SUCCESS, users, userAdminRepository.count());
  }

  @Override
  @Transactional
  public AdminUserResponse sendInvitation(String userId, String superAdminUserId) {

    validateInviteRequest(superAdminUserId);

    Optional<UserRegAdminEntity> optUser = userAdminRepository.findById(userId);
    UserRegAdminEntity user =
        optUser.orElseThrow(() -> new ErrorCodeException(ErrorCode.USER_NOT_FOUND));

    /*Timestamp now = new Timestamp(Instant.now().toEpochMilli());
    if (now.before(user.getSecurityCodeExpireDate())) {
      logger.info("Valid security code found, skip send invite email");
      return new AdminUserResponse(MessageCode.INVITATION_SENT_SUCCESSFULLY, user.getId());
    }*/

    user.setSecurityCode(IdGenerator.id());
    user.setSecurityCodeExpireDate(
        new Timestamp(
            Instant.now()
                .plus(Long.valueOf(appConfig.getSecurityCodeExpireDate()), ChronoUnit.MINUTES)
                .toEpochMilli()));
    user = userAdminRepository.saveAndFlush(user);

    UserAccountEmailSchedulerTaskEntity emailTaskEntity =
        UserMapper.toUserAccountEmailSchedulerTaskEntity(
            null, user, EmailTemplate.ACCOUNT_CREATED_EMAIL_TEMPLATE);
    userAccountEmailSchedulerTaskRepository.saveAndFlush(emailTaskEntity);

    return new AdminUserResponse(MessageCode.INVITATION_SENT_SUCCESSFULLY, user.getId());
  }

  private void validateInviteRequest(String superAdminUserId) {
    Optional<UserRegAdminEntity> optAdminDetails = userAdminRepository.findById(superAdminUserId);

    UserRegAdminEntity loggedInUserDetails =
        optAdminDetails.orElseThrow(() -> new ErrorCodeException(ErrorCode.USER_NOT_FOUND));

    if (!loggedInUserDetails.isSuperAdmin()) {
      logger.error("Signed in user is not having super admin privileges");
      throw new ErrorCodeException(ErrorCode.NOT_SUPER_ADMIN_ACCESS);
    }
  }

  @Override
  @Transactional
  public void sendUserEmail() {
    List<UserAccountEmailSchedulerTaskEntity> listOfAdminsToSendEmail =
        userAccountEmailSchedulerTaskRepository.findAllWithStatusZero();

    for (UserAccountEmailSchedulerTaskEntity adminRecordToSendEmail : listOfAdminsToSendEmail) {
      int updatedRows =
          userAccountEmailSchedulerTaskRepository.updateStatus(
              adminRecordToSendEmail.getUserId(), 1);

      if (updatedRows == 0) {
        // this record may be taken by another service instance
        continue;
      }

      Optional<UserRegAdminEntity> adminOpt =
          userAdminRepository.findById(adminRecordToSendEmail.getUserId());

      if (!adminOpt.isPresent()) {
        logger.warn(
            "Admin not found for invitation. So deleting this record from new admin email service table");
        userAccountEmailSchedulerTaskRepository.deleteByUserId(adminRecordToSendEmail.getUserId());
        continue;
      }

      UserRegAdminEntity admin = adminOpt.get();

      EmailResponse emailResponse = sendAccountCreatedOrUpdatedEmail(adminRecordToSendEmail, admin);

      // Post success or failed audit log event for sending email
      ParticipantManagerEvent auditEnum = null;
      if (MessageCode.EMAIL_ACCEPTED_BY_MAIL_SERVER
          .getMessage()
          .equals(emailResponse.getMessage())) {
        auditEnum =
            EmailTemplate.ACCOUNT_CREATED_EMAIL_TEMPLATE
                    .getTemplate()
                    .equals(adminRecordToSendEmail.getEmailTemplateType())
                ? NEW_USER_INVITATION_EMAIL_SENT
                : ACCOUNT_UPDATE_EMAIL_SENT;
        userAccountEmailSchedulerTaskRepository.deleteByUserId(adminRecordToSendEmail.getUserId());
      } else {
        auditEnum =
            EmailTemplate.ACCOUNT_CREATED_EMAIL_TEMPLATE
                    .getTemplate()
                    .equals(adminRecordToSendEmail.getEmailTemplateType())
                ? NEW_USER_INVITATION_EMAIL_FAILED
                : ACCOUNT_UPDATE_EMAIL_FAILED;
        userAccountEmailSchedulerTaskRepository.updateStatus(adminRecordToSendEmail.getUserId(), 0);
      }

      if (StringUtils.isNotEmpty(adminRecordToSendEmail.getAppId())
          && StringUtils.isNotEmpty(adminRecordToSendEmail.getSource())) {
        AuditLogEventRequest auditRequest = prepareAuditlogRequest(adminRecordToSendEmail);
        Map<String, String> map = new HashMap<>();
        map.put(CommonConstants.NEW_USER_ID, admin.getId());
        map.put(CommonConstants.EDITED_USER_ID, admin.getId());
        participantManagerHelper.logEvent(auditEnum, auditRequest, map);
      }
    }
  }

  private AuditLogEventRequest prepareAuditlogRequest(
      UserAccountEmailSchedulerTaskEntity adminRecordToSendEmail) {
    AuditLogEventRequest auditRequest = new AuditLogEventRequest();
    auditRequest.setAppId(adminRecordToSendEmail.getAppId());
    auditRequest.setAppVersion(adminRecordToSendEmail.getAppVersion());
    auditRequest.setCorrelationId(adminRecordToSendEmail.getCorrelationId());
    auditRequest.setSource(adminRecordToSendEmail.getSource());
    auditRequest.setMobilePlatform(adminRecordToSendEmail.getMobilePlatform());
    auditRequest.setUserId(adminRecordToSendEmail.getCreatedBy());
    return auditRequest;
  }

  private EmailResponse sendAccountCreatedOrUpdatedEmail(
      UserAccountEmailSchedulerTaskEntity adminRecordToSendEmail, UserRegAdminEntity admin) {
    Map<String, String> templateArgs = new HashMap<>();
    templateArgs.put("ORG_NAME", appConfig.getOrgName());
    templateArgs.put("FIRST_NAME", admin.getFirstName());
    templateArgs.put("CONTACT_EMAIL_ADDRESS", appConfig.getContactEmail());

    EmailRequest emailRequest = null;
    if (EmailTemplate.ACCOUNT_UPDATED_EMAIL_TEMPLATE
        .getTemplate()
        .equals(adminRecordToSendEmail.getEmailTemplateType())) {
      emailRequest =
          new EmailRequest(
              appConfig.getFromEmail(),
              new String[] {admin.getEmail()},
              null,
              null,
              appConfig.getUpdateUserSubject(),
              appConfig.getUpdateUserBody(),
              templateArgs);
    } else {
      templateArgs.put("ACTIVATION_LINK", appConfig.getUserDetailsLink() + admin.getSecurityCode());
      emailRequest =
          new EmailRequest(
              appConfig.getFromEmail(),
              new String[] {admin.getEmail()},
              null,
              null,
              appConfig.getRegisterUserSubject(),
              appConfig.getRegisterUserBody(),
              templateArgs);
    }
    return emailService.sendMimeMail(emailRequest);
  }
}
