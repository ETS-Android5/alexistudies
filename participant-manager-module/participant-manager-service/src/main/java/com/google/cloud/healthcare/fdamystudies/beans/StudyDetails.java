/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StudyDetails {
  private String id;

  private String customId;

  private String name;

  private Long totalSitesCount;

  private List<Site> sites;

  private String type;

  private String appInfoId;

  private String appId;

  private Long invited;

  private Long enrolled;

  private Double enrollmentPercentage;

  private Integer studyPermission;
}
