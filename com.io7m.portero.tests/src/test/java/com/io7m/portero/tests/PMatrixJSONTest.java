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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io7m.portero.server.internal.PMatrixJSON;
import com.io7m.portero.server.internal.PMatrixObjectMappers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class PMatrixJSONTest
{
  private ObjectMapper mapper;

  @BeforeEach
  public void setup()
  {
    this.mapper = PMatrixObjectMappers.createObjectMapper();
  }

  @Test
  public void testMatrixRegister0()
    throws Exception
  {
    final var object =
      this.mapper.readValue(
        PTestResources.resourceText("matrix-register-0.json"),
        PMatrixJSON.PRegisterUsernamePasswordRequest.class
      );

    Assertions.assertEquals("example", object.username);
    Assertions.assertEquals("wordpass", object.password);
    Assertions.assertEquals("m.login.dummy", object.auth.type);
  }

  @Test
  public void testError0()
    throws Exception
  {
    final var object =
      this.mapper.readValue(
        PTestResources.resourceText("matrix-error-0.json"),
        PMatrixJSON.PError.class
      );

    Assertions.assertEquals("M_BAD_JSON", object.errorCode);
    Assertions.assertEquals("Bad JSON", object.errorMessage);
  }
}
