/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConditionalOnProperty(
    value = "participant.manager.entities.enabled",
    havingValue = "true",
    matchIfMissing = false)
@Setter
@Getter
@Entity
@Table(name = "participant_study_info")
public class ParticipantStudyEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @ToString.Exclude
  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  @Column(name = "id", updatable = false, nullable = false)
  private String id;

  @Column(name = "participant_id", unique = true)
  private String participantId;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "study_info_id", insertable = true, updatable = false)
  private StudyEntity study;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "participant_registry_site_id", insertable = true, updatable = true)
  private ParticipantRegistrySiteEntity participantRegistrySite;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "site_id", insertable = true, updatable = true)
  private SiteEntity site;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "user_details_id", insertable = true, updatable = false)
  private UserDetailsEntity userDetails;

  @Column(name = "consent_status")
  private Boolean consentStatus;

  @Column(name = "status")
  private String status;

  @Column(name = "bookmark")
  private Boolean bookmark;

  @Column(name = "eligibility")
  private Boolean eligibility;

  @Column(
      name = "enrolled_date",
      insertable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp enrolledDate;

  @Column(name = "sharing")
  private String sharing;

  @Column(name = "completion")
  private Integer completion;

  @Column(name = "adherence")
  private Integer adherence;

  @Column(
      name = "withdrawal_date",
      insertable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp withdrawalDate;
}
