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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static com.io7m.portero.server.internal.PMatrixJSON.PAdminCreateUser;
import static com.io7m.portero.server.internal.PMatrixJSON.PAdminCreateUserResponse;
import static com.io7m.portero.server.internal.PMatrixJSON.PAdminNonce;
import static com.io7m.portero.server.internal.PMatrixJSON.PError;
import static com.io7m.portero.server.internal.PMatrixJSON.PMatrixJSONResponseType;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An extremely minimal Matrix client.
 */

public final class PMatrixClient
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PMatrixClient.class);

  private final HttpClient client;
  private final ObjectMapper objectMapper;
  private final URI serverBaseURI;

  private PMatrixClient(
    final HttpClient inClient,
    final ObjectMapper inObjectMapper,
    final URI inServerBaseURI)
  {
    this.client =
      Objects.requireNonNull(inClient, "client");
    this.objectMapper =
      Objects.requireNonNull(inObjectMapper, "inObjectMapper");
    this.serverBaseURI =
      Objects.requireNonNull(inServerBaseURI, "serverBaseURI");
  }

  private static String agent()
  {
    final var version =
      PMatrixClient.class.getPackage().getImplementationVersion();
    if (version == null) {
      return "com.io7m.portero.server/0.0.0";
    }
    return String.format("com.io7m.portero.server/%s", version);
  }

  /**
   * Create a new client.
   *
   * @param inClient        The underlying HTTP client
   * @param inServerBaseURI The server base URI
   *
   * @return A new client
   */

  public static PMatrixClient create(
    final HttpClient inClient,
    final URI inServerBaseURI)
  {
    return new PMatrixClient(
      inClient,
      PMatrixObjectMappers.createObjectMapper(),
      inServerBaseURI
    );
  }

  /**
   * Send a Synapse-specific request for a number-used-once via the Admin API.
   *
   * @return A response
   *
   * @throws IOException          On I/O errors
   * @throws InterruptedException If the operation is interrupted
   */

  public PMatrixJSONResponseType nonce()
    throws IOException, InterruptedException
  {
    final var targetURI =
      this.serverBaseURI.resolve("/_synapse/admin/v1/register");
    final var httpRequest =
      HttpRequest.newBuilder(targetURI)
        .build();
    final var response =
      this.client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

    final var statusCode =
      response.statusCode();
    final var contentType =
      response.headers().firstValue("content-type")
        .orElse("application/octet-stream");

    LOG.debug("{} status {}", targetURI, Integer.valueOf(statusCode));
    try (var stream = response.body()) {
      return this.parseResponse(
        statusCode,
        contentType,
        stream,
        PAdminNonce.class
      );
    }
  }

  /**
   * Send a Synapse-specific user registration request via the Admin API.
   *
   * @param sharedSecret The shared secret used to sign the request
   * @param nonce        The nonce
   * @param password     The password
   * @param userName     The user name
   *
   * @return A response
   *
   * @throws IOException          On I/O errors
   * @throws InterruptedException If the operation is interrupted
   */

  public PMatrixJSONResponseType register(
    final String sharedSecret,
    final String nonce,
    final String userName,
    final String password)
    throws IOException, InterruptedException
  {
    Objects.requireNonNull(sharedSecret, "sharedSecret");
    Objects.requireNonNull(userName, "userName");
    Objects.requireNonNull(password, "password");

    try {
      final var keyBytes =
        sharedSecret.getBytes(UTF_8);
      final var signingKey =
        new SecretKeySpec(keyBytes, "HmacSHA1");
      final var mac =
        Mac.getInstance("HmacSHA1");

      mac.init(signingKey);
      mac.update(nonce.getBytes(UTF_8));
      mac.update((byte) 0x0);
      mac.update(userName.getBytes(UTF_8));
      mac.update((byte) 0x0);
      mac.update(password.getBytes(UTF_8));
      mac.update((byte) 0x0);
      mac.update("notadmin".getBytes(UTF_8));

      final var digest =
        mac.doFinal();
      final var digestText =
        Hex.encodeHexString(digest, true);

      final var request = new PAdminCreateUser();
      request.username = userName;
      request.password = password;
      request.nonce = nonce;
      request.mac = digestText;

      final var targetURI =
        this.serverBaseURI.resolve("/_synapse/admin/v1/register");
      final var serialized =
        this.objectMapper.writeValueAsBytes(request);
      final var httpRequest =
        HttpRequest.newBuilder(targetURI)
          .POST(HttpRequest.BodyPublishers.ofByteArray(serialized))
          .header("User-Agent", agent())
          .build();
      final var response =
        this.client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

      final var statusCode =
        response.statusCode();
      final var contentType =
        response.headers().firstValue("content-type")
          .orElse("application/octet-stream");

      LOG.debug("{} status {}", targetURI, Integer.valueOf(statusCode));
      try (var stream = response.body()) {
        return this.parseResponse(
          statusCode,
          contentType,
          stream,
          PAdminCreateUserResponse.class
        );
      }
    } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }

  private PMatrixJSONResponseType parseResponse(
    final int statusCode,
    final String contentType,
    final InputStream stream,
    final Class<? extends PMatrixJSONResponseType> responseClass)
    throws IOException
  {
    if (!Objects.equals(contentType, "application/json")) {
      throw new IOException(String.format(
        "Server responded with an unexpected content type '%s'",
        contentType)
      );
    }

    final var data = stream.readAllBytes();
    // CHECKSTYLE:OFF
    final var text = new String(data, UTF_8);
    // CHECKSTYLE:ON
    LOG.trace("received: {}", text);

    if (statusCode >= 400) {
      final var error =
        this.objectMapper.readValue(text, PError.class);

      LOG.trace("error: {}", error);
      return error;
    }

    final var response =
      this.objectMapper.readValue(text, responseClass);

    LOG.trace("response: {}", response);
    return response;
  }
}
