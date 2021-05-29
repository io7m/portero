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

package com.io7m.portero.server;

import com.io7m.jdeferthrow.core.ExceptionTracker;
import com.io7m.jproperties.JProperties;

import java.io.InputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;

/**
 * Functions to parse server configuration files.
 */

public final class PServerConfigurations
{
  private PServerConfigurations()
  {

  }

  /**
   * Load configuration data from the given properties.
   *
   * @param properties The properties file
   *
   * @return A configuration
   *
   * @throws Exception On errors
   */

  public static PServerConfiguration ofProperties(
    final Properties properties)
    throws Exception
  {
    Objects.requireNonNull(properties, "properties");

    final var builder = PServerConfiguration.builder();
    final var tracker = new ExceptionTracker<Exception>();

    readMatrixProperties(properties, builder, tracker);
    readServerProperties(properties, builder, tracker);

    tracker.throwIfNecessary();
    return builder.build();
  }

  /**
   * Load configuration data from the given stream.
   *
   * @param stream The stream
   *
   * @return A configuration
   *
   * @throws Exception On errors
   */

  public static PServerConfiguration ofStream(
    final InputStream stream)
    throws Exception
  {
    Objects.requireNonNull(stream, "stream");

    final var properties = new Properties();
    properties.load(stream);
    return ofProperties(properties);
  }

  private static void readServerProperties(
    final Properties props,
    final PServerConfiguration.Builder config,
    final ExceptionTracker<Exception> tracker)
  {
    tracker.catching(() -> {
      config.setBindPublicPort(
        JProperties.getInteger(props, "server.publicPort"));
    });

    tracker.catching(() -> {
      config.setBindPublicAddress(
        InetAddress.getByName(
          JProperties.getString(props, "server.publicAddress"))
      );
    });

    tracker.catching(() -> {
      config.setBindPrivatePort(
        JProperties.getInteger(props, "server.privatePort")
      );
    });

    tracker.catching(() -> {
      config.setBindPrivateAddress(
        InetAddress.getByName(
          JProperties.getString(props, "server.privateAddress"))
      );
    });

    tracker.catching(() -> {
      config.setServerTitle(
        JProperties.getString(props, "server.title")
      );
    });

    tracker.catching(() -> {
      config.setServerTokenExpiry(
        JProperties.getDurationWithDefault(
          props,
          "server.tokenExpiration",
          Duration.of(48L, ChronoUnit.HOURS))
      );
    });

    tracker.catching(() -> {
      config.setPublicURI(JProperties.getURI(props, "server.publicURL"));
    });

    tracker.catching(() -> {
      config.setServerThreadCount(
        JProperties.getIntegerWithDefault(props, "server.threadCount", 4)
      );
    });
  }

  private static void readMatrixProperties(
    final Properties properties,
    final PServerConfiguration.Builder builder,
    final ExceptionTracker<Exception> tracker)
  {
    tracker.catching(() -> {
      builder.setMatrixServerAdminConnectionURI(
        JProperties.getURI(properties, "matrix.adminURL")
      );
    });

    tracker.catching(() -> {
      builder.setMatrixServerAdminRegistrationSecret(
        JProperties.getString(properties, "matrix.adminSharedSecret")
      );
    });

    tracker.catching(() -> {
      builder.setMatrixServerPublicURI(
        JProperties.getURI(properties, "matrix.publicURL")
      );
    });
  }
}
