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

package com.io7m.portero.tests;

import com.io7m.portero.server.internal.PMatrixClient;
import com.io7m.portero.server.internal.PMatrixJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;

public final class PRegisterDemo
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PRegisterDemo.class);

  private PRegisterDemo()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var httpClient =
      HttpClient.newHttpClient();
    final var client =
      PMatrixClient.create(httpClient, URI.create("http://10.2.250.28/"));

    final var request = new PMatrixJSON.PRegisterUsernamePasswordRequest();
    request.username = "admin";
    request.password = "ChCUc6w8WDP3k3BXVQ==";

    final var response = client.register(request);
    LOG.debug("response: {}", response);
  }
}
