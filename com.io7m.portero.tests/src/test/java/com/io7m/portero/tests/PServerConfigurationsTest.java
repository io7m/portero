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

import com.io7m.portero.server.PServerConfiguration;
import com.io7m.portero.server.PServerConfigurations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PServerConfigurationsTest
{
  @Test
  public void testOK()
    throws Exception
  {
    final var properties =
      PTestResources.resourceProperties("server.properties");
    final var configuration =
      PServerConfigurations.ofProperties(properties);

    final var expected =
      PServerConfiguration.builder()
        .setServerTitle("chat.example.com")
        .setServerThreadCount(4)
        .setPublicURI(URI.create("https://invite.example.com/"))
        .setBindPublicAddress(InetAddress.getByName("127.0.0.1"))
        .setBindPublicPort(20000)
        .setBindPrivateAddress(InetAddress.getByName("127.0.0.2"))
        .setBindPrivatePort(20001)
        .setMatrixServerAdminConnectionURI(URI.create("http://127.0.0.1:10000/"))
        .setMatrixServerAdminRegistrationSecret("aRatherLongSharedSecret")
        .setMatrixServerPublicURI(URI.create("https://chat.example.com"))
        .build();

    Assertions.assertEquals(expected, configuration);
  }

  @Test
  public void testMissing()
    throws Exception
  {
    final var properties =
      PTestResources.resourceProperties("empty.properties");

    final var exception =
      Assertions.assertThrows(Exception.class, () -> {
        PServerConfigurations.ofProperties(properties);
      });

    final var errors =
      Stream.of(exception.getSuppressed())
        .map(Throwable::getMessage)
        .collect(Collectors.toList());

    Assertions.assertEquals(
      List.of(
        "Key not found in properties: matrix.adminSharedSecret",
        "Key not found in properties: matrix.publicURL",
        "Key not found in properties: server.publicPort",
        "Key not found in properties: server.publicAddress",
        "Key not found in properties: server.privatePort",
        "Key not found in properties: server.privateAddress",
        "Key not found in properties: server.title",
        "Key not found in properties: server.publicURL"
      ),
      errors
    );
  }

  @Test
  public void testGarbage()
    throws Exception
  {
    final var properties =
      PTestResources.resourceProperties("garbage0.properties");

    final var exception =
      Assertions.assertThrows(Exception.class, () -> {
        PServerConfigurations.ofProperties(properties);
      });

    final var errors =
      Stream.of(exception.getSuppressed())
        .map(Throwable::getMessage)
        .collect(Collectors.toList());

    Assertions.assertEquals(
      List.of(
        "Value for key matrix.publicURL (not a url of any kind) cannot be parsed as type URI",
        "Value for key server.publicPort (x) cannot be parsed as type Integer",
        "Value for key server.privatePort (10000000000000) cannot be parsed as type int",
        "Value for key server.tokenExpiration (not a duration) cannot be parsed as type Duration",
        "Key not found in properties: server.publicURL"
      ),
      errors
    );
  }
}
