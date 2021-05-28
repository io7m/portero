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
import com.io7m.portero.server.internal.PServerMain;

import java.net.InetAddress;
import java.net.URI;

public final class PServerMainDemo
{
  private PServerMainDemo()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var config =
      PServerConfiguration.builder()
        .setMatrixServerPublicURI(URI.create("http://chat.example.com"))
        .setMatrixServerAdminConnectionURI(URI.create("http://www.example.com"))
        .setMatrixServerAdminUser("admin")
        .setMatrixServerAdminPassword("password")
        .setBindPrivateAddress(InetAddress.getByName("127.0.0.1"))
        .setBindPrivatePort(20001)
        .setBindPublicAddress(InetAddress.getByName("127.0.0.1"))
        .setBindPublicPort(20000)
        .setServerTitle("chat.example.com")
        .setPublicURI(URI.create("http://invite.example.com"))
        .build();

    final var server = PServerMain.create(config);
    server.start();

    while (true) {
      Thread.sleep(1_000L);
    }
  }
}
