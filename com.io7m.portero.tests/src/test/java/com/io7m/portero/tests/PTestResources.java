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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Objects;
import java.util.Properties;

public final class PTestResources
{
  private PTestResources()
  {

  }

  public static byte[] resourceText(
    final String name)
    throws IOException
  {
    Objects.requireNonNull(name, "name");

    final var file =
      String.format("/com/io7m/portero/tests/%s", name);
    final var url =
      PMatrixJSONTest.class.getResource(file);

    if (url == null) {
      throw new NoSuchFileException(file);
    }

    try (var stream = url.openStream()) {
      return stream.readAllBytes();
    }
  }

  public static Properties resourceProperties(
    final String name)
    throws IOException
  {
    Objects.requireNonNull(name, "name");

    final var file =
      String.format("/com/io7m/portero/tests/%s", name);
    final var url =
      PMatrixJSONTest.class.getResource(file);

    if (url == null) {
      throw new NoSuchFileException(file);
    }

    try (var stream = url.openStream()) {
      final var properties = new Properties();
      properties.load(stream);
      return properties;
    }
  }
}
