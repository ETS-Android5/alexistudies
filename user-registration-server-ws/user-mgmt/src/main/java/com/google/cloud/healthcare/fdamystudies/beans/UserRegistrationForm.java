/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRegistrationForm {

  private String userId;
  private String firstName;
  private String lastName;
  @NotNull
  private String emailId;
  private String password;
  private boolean usePassCode;
  private boolean touchId;
  private boolean localNotification;
  private boolean remoteNotification;
  private boolean reminderFlag;
  private String auth;
  private int status;
  private boolean tempPassword;
  private LocalDateTime tempPasswordDate;

  public UserRegistrationForm(String userId, String emailId, String password) {
    super();
    this.userId = userId;
    this.emailId = emailId;
    this.password = password;
  }
}
