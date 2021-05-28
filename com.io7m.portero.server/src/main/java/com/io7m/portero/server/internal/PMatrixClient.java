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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.io7m.portero.server.internal.PMatrixJSON.PError;
import static com.io7m.portero.server.internal.PMatrixJSON.PLoginResponse;
import static com.io7m.portero.server.internal.PMatrixJSON.PLoginUsernamePasswordRequest;
import static com.io7m.portero.server.internal.PMatrixJSON.PMatrixJSONResponseType;
import static com.io7m.portero.server.internal.PMatrixJSON.PRegisterResponse;
import static com.io7m.portero.server.internal.PMatrixJSON.PRegisterUsernamePasswordRequest;

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
   * Send a user registration request.
   *
   * @param request The request
   *
   * @return A response
   *
   * @throws IOException          On I/O errors
   * @throws InterruptedException If the operation is interrupted
   */

  public PMatrixJSONResponseType register(
    final PRegisterUsernamePasswordRequest request)
    throws IOException, InterruptedException
  {
    Objects.requireNonNull(request, "request");

    LOG.trace("request: {}", request);

    final var targetURI =
      this.serverBaseURI.resolve("/_matrix/client/r0/register");
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
        PRegisterResponse.class
      );
    }
  }

  /**
   * Send a user login request.
   *
   * @param request The request
   *
   * @return A response
   *
   * @throws IOException          On I/O errors
   * @throws InterruptedException If the operation is interrupted
   */

  public PMatrixJSONResponseType login(
    final PLoginUsernamePasswordRequest request)
    throws IOException, InterruptedException
  {
    Objects.requireNonNull(request, "request");

    LOG.trace("request: {}", request);

    final var targetURI =
      this.serverBaseURI.resolve("/_matrix/client/r0/login");
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
        PLoginResponse.class
      );
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
    final var text = new String(data, StandardCharsets.UTF_8);
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
