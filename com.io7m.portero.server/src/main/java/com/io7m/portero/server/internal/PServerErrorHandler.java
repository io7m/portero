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
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

import java.io.IOException;
import java.util.Objects;

/**
 * A server error handler.
 */

public final class PServerErrorHandler extends ErrorHandler
{
  private final PServerPages pages;
  private final PServerConfiguration configuration;

  PServerErrorHandler(
    final PServerPages inPages,
    final PServerConfiguration inConfiguration)
  {
    this.pages =
      Objects.requireNonNull(inPages, "pages");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "inConfiguration");
  }

  @Override
  public void handle(
    final String target,
    final Request baseRequest,
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws IOException
  {
    final String message =
      (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    final int code =
      ((Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
        .intValue();

    this.pages.sendPage(
      response,
      code,
      this.pages.errorPage(this.configuration, code, message)
    );
  }
}
