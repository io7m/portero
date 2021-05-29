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

import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.io7m.portero.server.internal.PMatrixJSON.PError;

/**
 * The main server controller.
 */

public final class PServerController
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PServerController.class);

  private final SecureRandom rng;
  private final Map<String, String> tokens;
  private final PServerStrings strings;
  private final PMatrixClient client;

  private PServerController(
    final Duration inExpiry,
    final PServerStrings inStrings,
    final PMatrixClient inClient)
  {
    Objects.requireNonNull(inExpiry, "inExpiry");

    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.client =
      Objects.requireNonNull(inClient, "client");

    try {
      this.tokens =
        Collections.synchronizedMap(
          ExpiringMap.builder()
            .expiration(inExpiry.toSeconds(), TimeUnit.SECONDS)
            .expirationListener(PServerController::onExpired)
            .build()
        );

      this.rng = SecureRandom.getInstanceStrong();
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void onExpired(
    final String key,
    final String value)
  {
    LOG.info("token {} expired", key);
  }

  /**
   * Create a new server controller.
   *
   * @param inExpiry The expiration time for individual tokens
   * @param strings  String resources
   * @param client   The client
   *
   * @return A new server controller
   */

  public static PServerController create(
    final PServerStrings strings,
    final Duration inExpiry,
    final PMatrixClient client)
  {
    return new PServerController(inExpiry, strings, client);
  }

  /**
   * Generate a fresh token.
   *
   * @return The token
   */

  public String generateToken()
  {
    while (true) {
      final var data = new byte[32];
      this.rng.nextBytes(data);
      final var token = Hex.encodeHexString(data, true);
      if (!this.tokens.containsKey(token)) {
        this.tokens.put(token, token);
        LOG.info("generated new token {}", token);
        return token;
      }
    }
  }

  /**
   * Process the given invite request.
   *
   * @param request The request
   *
   * @throws PServerControllerException On errors
   * @throws InterruptedException       If the operation is interrupted
   */

  public void processInvite(
    final PInviteRequest request)
    throws PServerControllerException, InterruptedException
  {
    Objects.requireNonNull(request, "request");

    final var token = request.token();
    if (!this.tokens.containsKey(token)) {
      LOG.warn("nonexistent token: {}", token);
      throw new PServerControllerException(
        this.strings.format("errorTokenNonexistent"));
    }

    try {
      final var nonceResponse = this.client.nonce();
      if (nonceResponse instanceof PError) {
        final var error = (PError) nonceResponse;
        throw new PServerControllerException(
          this.strings.format(
            "errorServerRegister",
            error.errorCode,
            error.errorMessage)
        );
      }

      final var nonceR = (PMatrixJSON.PAdminNonce) nonceResponse;

      final var registerResponse =
        this.client.register(
          request.registrationSharedSecret(),
          nonceR.nonce,
          request.userName(),
          request.password()
        );

      if (registerResponse instanceof PError) {
        final var error = (PError) registerResponse;
        throw new PServerControllerException(
          this.strings.format(
            "errorServerRegister",
            error.errorCode,
            error.errorMessage)
        );
      }

      this.tokens.remove(token);
      LOG.info(
        "consumed token {} for user '{}' ({} tokens left)",
        token,
        request.userName(),
        Integer.valueOf(this.tokenCount())
      );
    } catch (final IOException e) {
      LOG.error("i/o error: ", e);
      throw new PServerControllerException(e);
    }
  }

  /**
   * @return The number of active tokens
   */

  public int tokenCount()
  {
    return this.tokens.size();
  }
}
