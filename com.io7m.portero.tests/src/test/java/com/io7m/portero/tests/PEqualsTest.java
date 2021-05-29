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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public final class PEqualsTest
{
  @TestFactory
  public Stream<DynamicTest> testEquals()
  {
    return Stream.of(
      com.io7m.portero.server.internal.PInviteRequest.class,
      com.io7m.portero.server.PServerConfiguration.class)
      .map(this::testOf);
  }

  private DynamicTest testOf(
    final Class<? extends Object> aClass)
  {
    return DynamicTest.dynamicTest(
      String.format(
        "testEquals_%s",
        aClass.getCanonicalName()),
      () -> {
        EqualsVerifier.forClass(aClass)
          .suppress(Warning.NULL_FIELDS)
          .verify();
      });
  }
}
