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
import com.io7m.portero.server.internal.PServerController;
import com.io7m.portero.server.internal.PServerStrings;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PServerControllerTest
{
  @Test
  public void testExpiration()
    throws Exception
  {
    final var controller =
      PServerController.create(
        new PServerStrings(Locale.getDefault()),
        Duration.ofSeconds(1L),
        PMatrixClient.create(
          HttpClient.newHttpClient(),
          URI.create("http://example.com/"))
      );

    controller.generateToken();
    assertEquals(1, controller.tokenCount());
    Thread.sleep(Duration.ofSeconds(2L).toMillis());
    assertEquals(0, controller.tokenCount());
  }
}
