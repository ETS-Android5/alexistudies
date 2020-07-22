/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.service;

import java.util.Calendar;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.google.cloud.healthcare.fdamystudies.beans.EmailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailResponse;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.PlaceholderReplacer;

@Service
@ConditionalOnProperty(
    value = "commonservice.email.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class EmailServiceImpl implements EmailService {

  private XLogger logger = XLoggerFactory.getXLogger(EmailServiceImpl.class.getName());

  @Autowired private JavaMailSender emailSender;

  @Override
  public EmailResponse sendSimpleMail(EmailRequest emailRequest) {
    logger.entry("Begin sendSimpleMail()");
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(emailRequest.getFrom());
      message.setBcc(emailRequest.getBcc());
      message.setCc(emailRequest.getCc());
      message.setTo(emailRequest.getTo());
      message.setSubject(getSubject(emailRequest));
      message.setText(getBodyContent(emailRequest));
      message.setSentDate(Calendar.getInstance().getTime());
      emailSender.send(message);
      logger.exit(String.format("status=%d", HttpStatus.ACCEPTED.value()));
      return new EmailResponse(MessageCode.EMAIL_ACCEPTED_BY_MAIL_SERVER);
    } catch (Exception e) {
      logger.error("sendSimpleMail() failed with an exception.", e);
      return new EmailResponse(ErrorCode.EMAIL_SEND_FAILED_EXCEPTION);
    }
  }

  @Override
  public EmailResponse sendMimeMail(EmailRequest emailRequest) {
    logger.entry("Begin sendSimpleMail()");
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(emailRequest.getFrom());
      helper.setTo(emailRequest.getTo());

      if (ArrayUtils.isNotEmpty(emailRequest.getCc())) {
        helper.setCc(emailRequest.getCc());
      }

      if (ArrayUtils.isNotEmpty(emailRequest.getBcc())) {
        helper.setBcc(emailRequest.getBcc());
      }

      message.setSubject(getSubject(emailRequest));
      message.setText(getBodyContent(emailRequest), "utf-8", "html");
      message.setSentDate(Calendar.getInstance().getTime());
      emailSender.send(message);
      logger.exit(String.format("status=%d", HttpStatus.ACCEPTED.value()));
      return new EmailResponse(MessageCode.EMAIL_ACCEPTED_BY_MAIL_SERVER);
    } catch (Exception e) {
      logger.error("sendSimpleMail() failed with an exception.", e);
      return new EmailResponse(ErrorCode.EMAIL_SEND_FAILED_EXCEPTION);
    }
  }

  private String getSubject(EmailRequest emailRequest) {
    if (emailRequest.getTemplateArgs() != null) {
      return PlaceholderReplacer.replaceNamedPlaceholders(
          emailRequest.getSubject(), emailRequest.getTemplateArgs());
    }
    return emailRequest.getSubject();
  }

  private String getBodyContent(EmailRequest emailRequest) {
    if (emailRequest.getTemplateArgs() != null) {
      return PlaceholderReplacer.replaceNamedPlaceholders(
          emailRequest.getBody(), emailRequest.getTemplateArgs());
    }
    return emailRequest.getBody();
  }
}
