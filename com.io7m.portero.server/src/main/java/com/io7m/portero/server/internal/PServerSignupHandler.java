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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * A handler that displays a form to finish a signup request.
 */

public final class PServerSignupHandler extends AbstractHandler
{
  private final PServerPages pages;
  private final PServerController controller;
  private final PServerConfiguration configuration;

  PServerSignupHandler(
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
    final var token = request.getParameter("token");
    if (token == null || token.isBlank()) {
      this.pages.sendPage(
        response,
        400,
        this.pages.errorPage(this.configuration, 400, "Missing token.")
      );
      baseRequest.setHandled(true);
      return;
    }

    this.pages.sendPage(
      response,
      200,
      this.pages.signupPage(this.configuration, token)
    );
    baseRequest.setHandled(true);
  }
}
