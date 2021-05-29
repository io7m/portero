/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */

package com.io7m.portero.server.internal;

import com.io7m.portero.server.PServerConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * A handler that completes a signup request.
 */

public final class PServerSignupCompleteHandler extends AbstractHandler
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PServerSignupCompleteHandler.class);

  private final PServerPages pages;
  private final PServerController controller;
  private final PServerConfiguration configuration;

  PServerSignupCompleteHandler(
    final PServerPages inPages,
    final PServerController inController,
    final PServerConfiguration inConfiguration)
  {
    this.pages =
      Objects.requireNonNull(inPages, "pages");
    this.controller =
      Objects.requireNonNull(inController, "controller");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public void handle(
    final String target,
    final Request baseRequest,
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws IOException
  {
    final var parameters = new MultiMap<String>();
    baseRequest.extractFormParameters(parameters);

    final var token =
      parameters.getValue("token", 0);
    final var user =
      parameters.getValue("user_name", 0);
    final var email =
      parameters.getValue("email", 0);
    final var password =
      parameters.getValue("password", 0);
    final var passwordConfirm =
      parameters.getValue("password_confirm", 0);

    try {
      this.checkParameter(response, "token", token);
      this.checkParameter(response, "user", user);
      this.checkParameter(response, "email", email);
      this.checkParameter(response, "password", password);
      this.checkParameter(response, "password confirmation", passwordConfirm);
    } catch (final IllegalArgumentException e) {
      baseRequest.setHandled(true);
      return;
    }

    if (!password.equals(passwordConfirm)) {
      this.pages.sendPage(
        response,
        400,
        this.pages.errorPage(
          this.configuration,
          400,
          "Password confirmation does not match the password.")
      );
      baseRequest.setHandled(true);
      return;
    }

    try {
      LOG.info("processing invite for token {}, user {}", token, user);

      this.controller.processInvite(
        PInviteRequest.builder()
          .setToken(token)
          .setPassword(password)
          .setUserName(user)
          .setRegistrationSharedSecret(this.configuration.matrixServerAdminRegistrationSecret())
          .build()
      );
      this.pages.sendPage(
        response,
        200,
        this.pages.successPage(this.configuration)
      );
    } catch (final PServerControllerException | InterruptedException e) {
      this.pages.sendPage(
        response,
        400,
        this.pages.errorPage(this.configuration, 400, e.getMessage())
      );
    }

    baseRequest.setHandled(true);
  }

  private void checkParameter(
    final HttpServletResponse response,
    final String name,
    final String value)
    throws IOException
  {
    if (value == null || value.isBlank()) {
      this.pages.sendPage(
        response,
        400,
        this.pages.errorPage(
          this.configuration,
          400,
          String.format("Missing or invalid %s.", name))
      );
      throw new IllegalArgumentException();
    }
  }
}
