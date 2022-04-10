/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.portero.server.internal;

import com.io7m.jxtrand.vanilla.JXTAbstractStrings;
import com.io7m.portero.server.PServerConfiguration;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;

import static org.apache.commons.text.StringEscapeUtils.ESCAPE_XML10;

/**
 * Functions to generate server pages.
 */

public final class PServerPages extends JXTAbstractStrings
{
  /**
   * A page generator.
   *
   * @param locale The server locale
   *
   * @throws IOException On errors
   */

  public PServerPages(
    final Locale locale)
    throws IOException
  {
    super(
      locale,
      PServerPages.class,
      "/com/io7m/portero/server/internal",
      "Pages"
    );
  }

  /**
   * Generate a main page.
   *
   * @param configuration The server configuration
   *
   * @return A page
   */

  public String mainPage(
    final PServerConfiguration configuration)
  {
    return this.format(
      "mainPage",
      ESCAPE_XML10.translate(configuration.serverTitle())
    );
  }

  /**
   * Generate an error page.
   *
   * @param configuration The server configuration
   * @param code          The error code
   * @param message       The error message
   *
   * @return A page
   */

  public String errorPage(
    final PServerConfiguration configuration,
    final int code,
    final String message)
  {
    return this.format(
      "errorPage",
      ESCAPE_XML10.translate(configuration.serverTitle()),
      Integer.valueOf(code),
      ESCAPE_XML10.translate(message)
    );
  }

  /**
   * Generate a signup form.
   *
   * @param configuration The server configuration
   * @param token         The token
   *
   * @return A page
   */

  public String signupPage(
    final PServerConfiguration configuration,
    final String token)
  {
    return this.format(
      "signupPage",
      ESCAPE_XML10.translate(configuration.serverTitle()),
      ESCAPE_XML10.translate(token)
    );
  }

  /**
   * Generate a success page.
   *
   * @param configuration The server configuration
   *
   * @return A page
   */

  public String successPage(
    final PServerConfiguration configuration)
  {
    return this.format(
      "successPage",
      ESCAPE_XML10.translate(configuration.serverTitle()),
      configuration.matrixServerPublicURI().toString()
    );
  }

  /**
   * Send a page to a servlet response.
   *
   * @param response   The servlet response
   * @param statusCode The status code
   * @param text       The page text
   *
   * @throws IOException On I/O errors
   */

  public void sendPage(
    final HttpServletResponse response,
    final int statusCode,
    final String text)
    throws IOException
  {
    response.setContentType("application/xhtml+xml");
    response.setStatus(statusCode);

    try (var outputStream = response.getOutputStream()) {
      outputStream.println(text);
      outputStream.flush();
    }
  }
}
