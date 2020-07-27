/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.repository;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.google.cloud.healthcare.fdamystudies.model.UserRegAdminEntity;

@Repository
@ConditionalOnProperty(
    value = "participant.manager.repository.enabled",
    havingValue = "true",
    matchIfMissing = false)
public interface UserRegAdminRepository extends JpaRepository<UserRegAdminEntity, String> {

  @Query("SELECT user from UserRegAdminEntity user where user.urAdminAuthId=:urAdminAuthId")
  public Optional<UserRegAdminEntity> findByUrAdminAuthId(String urAdminAuthId);

  @Query("SELECT user from UserRegAdminEntity user where user.securityCode=:securityCode")
  public Optional<UserRegAdminEntity> findBySecurityCode(String securityCode);

  @Query("SELECT ua FROM UserRegAdminEntity ua WHERE ua.id = :userId")
  Optional<UserRegAdminEntity> findByUserRegAdminId(String userId);

  public Optional<UserRegAdminEntity> findByEmail(String email);
}
