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
 * A handler that generates an invite URL.
 */

public final class PServerInviteHandler extends AbstractHandler
{
  private final PServerPages pages;
  private final PServerController controller;
  private final PServerConfiguration configuration;

  PServerInviteHandler(
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
    final var token = this.controller.generateToken();
    response.setContentType("text/plain");

    try (var output = response.getOutputStream()) {
      output.println(
        this.configuration.publicURI()
          .resolve(String.format("/signup/?token=%s", token))
          .toString()
      );
      output.flush();
    }
    baseRequest.setHandled(true);
  }
}
