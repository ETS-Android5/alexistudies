/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.google.cloud.healthcare.fdamystudies.model.StudyEntity;

@Repository
@ConditionalOnProperty(
    value = "participant.manager.repository.enabled",
    havingValue = "true",
    matchIfMissing = false)
public interface StudyRepository extends JpaRepository<StudyEntity, String> {

  @Query(
      value =
          "SELECT s.location_id, GROUP_CONCAT(DISTINCT si.name SEPARATOR ',') from sites s, study_info si where s.study_id=si.id AND s.location_id in (:locationIds) GROUP BY s.location_id",
      nativeQuery = true)
  public List<Object[]> getStudiesForLocations(List<String> locationIds);
}
