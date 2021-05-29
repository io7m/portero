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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;

/**
 * The server configuration.
 */

@ImmutablesStyleType
@Value.Immutable
public interface PServerConfigurationType
{
  /**
   * The URI of the Matrix server that will be used to interact with
   * the Admin API after authentication. For security reasons, this should be
   * a localhost address.
   *
   * @return The base URI of the Matrix server
   */

  URI matrixServerAdminConnectionURI();

  /**
   * @return The shared secret that will allow registration on the server
   */

  String matrixServerAdminRegistrationSecret();

  /**
   * The public URI of the Matrix server. This is the URI to which new users
   * will be redirected after a user registration is successful.
   *
   * @return The public URI of the Matrix server
   */

  URI matrixServerPublicURI();

  /**
   * The public URI of this server. This is the API with which external
   * users interact. This is, for example, the URL that will be shown
   * in generated invitation links.
   *
   * @return The public URI of this server
   */

  URI publicURI();

  /**
   * The address to which to bind the public part server. It is recommended to
   * bind the server to localhost and put it behind a reverse proxy providing
   * TLS.
   *
   * @return The address to which to bind
   */

  InetAddress bindPublicAddress();

  /**
   * The port to which to bind the public part of the server. It is recommended
   * to bind the server to localhost and put it behind a reverse proxy
   * providing TLS.
   *
   * @return The port to which to bind
   */

  int bindPublicPort();

  /**
   * The address to which to bind the private part of the server. It is
   * recommended to bind the private part of the server to localhost.
   *
   * @return The address to which to bind
   */

  InetAddress bindPrivateAddress();

  /**
   * The port to which to bind the private part of the server.
   *
   * @return The port to which to bind
   */

  int bindPrivatePort();

  /**
   * @return The number of threads used
   */

  @Value.Default
  default int serverThreadCount()
  {
    return 8;
  }

  /**
   * @return The title of server
   */

  String serverTitle();

  /**
   * @return The duration before a given token expires
   */

  @Value.Default
  default Duration serverTokenExpiry()
  {
    return Duration.ofHours(48L);
  }

  /**
   * @return The locale for string resources
   */

  @Value.Default
  default Locale locale()
  {
    return Locale.getDefault();
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    if (!this.matrixServerAdminConnectionURI().toString().endsWith("/")) {
      throw new IllegalArgumentException(
        "The Matrix server admin URI must end with /");
    }
  }
}
