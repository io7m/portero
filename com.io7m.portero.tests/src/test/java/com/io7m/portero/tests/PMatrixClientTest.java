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

import com.io7m.portero.server.internal.PMatrixClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.io7m.portero.server.internal.PMatrixJSON.PError;
import static com.io7m.portero.server.internal.PMatrixJSON.PLoginResponse;
import static com.io7m.portero.server.internal.PMatrixJSON.PLoginUsernamePasswordRequest;
import static com.io7m.portero.server.internal.PMatrixJSON.PRegisterResponse;
import static com.io7m.portero.server.internal.PMatrixJSON.PRegisterUsernamePasswordRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public final class PMatrixClientTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PMatrixClientTest.class);

  private static final Duration REQUEST_TIMEOUT =
    Duration.ofSeconds(5L);

  private HttpClient httpClient;
  private URI baseUri;
  private MockServerClient mockServer;
  private PMatrixClient client;

  @BeforeEach
  public void setup()
  {
    this.httpClient = HttpClient.newHttpClient();

    this.mockServer = startClientAndServer(20000);
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.baseUri =
      URI.create("http://127.0.0.1:20000/");
    this.client =
      PMatrixClient.create(this.httpClient, this.baseUri);
  }

  @AfterEach
  public void tearDown()
  {
    LOG.debug("tearing down");

    this.mockServer.stop();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));
    this.mockServer.close();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));
  }

  @Test
  public void testRegisterErrorBadContent()
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    final var request = new PRegisterUsernamePasswordRequest();
    request.username = "user";
    request.password = "password";

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(403)
          .withContentType(MediaType.TEXT_PLAIN)
          .withBody("What?"));

    final var ex =
      assertThrows(IOException.class, () -> {
        assertTimeout(REQUEST_TIMEOUT, () -> this.client.register(request));
      });

    assertEquals(
      "Server responded with an unexpected content type 'text/plain'",
      ex.getMessage());
  }

  @Test
  public void testRegisterError()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    final var request = new PRegisterUsernamePasswordRequest();
    request.username = "user";
    request.password = "password";

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(403)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-error-0.json")));

    final var response =
      (PError) assertTimeout(
        REQUEST_TIMEOUT, () -> this.client.register(request));

    assertEquals("M_BAD_JSON", response.errorCode);
    assertEquals("Bad JSON", response.errorMessage);
  }

  @Test
  public void testRegisterOK()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    final var request = new PRegisterUsernamePasswordRequest();
    request.username = "user";
    request.password = "password";

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText(
            "matrix-register-response-0.json")));

    final var response =
      (PRegisterResponse) assertTimeout(
        REQUEST_TIMEOUT, () -> this.client.register(request));

    assertEquals(
      "523aee45f8db404c05373a61bb95ad6d",
      response.accessToken);
    assertEquals("localhost", response.homeServer);
    assertEquals("@example:localhost", response.userId);
  }


  @Test
  public void testLoginError()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    final var request = new PLoginUsernamePasswordRequest();
    request.user = "user";
    request.password = "password";

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(403)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-error-0.json")));

    final var response =
      (PError) assertTimeout(
        REQUEST_TIMEOUT, () -> this.client.login(request));

    assertEquals("M_BAD_JSON", response.errorCode);
    assertEquals("Bad JSON", response.errorMessage);
  }

  @Test
  public void testLoginErrorBadContent()
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    final var request = new PLoginUsernamePasswordRequest();
    request.user = "user";
    request.password = "password";

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(403)
          .withContentType(MediaType.TEXT_PLAIN)
          .withBody("What?"));

    final var ex =
      assertThrows(IOException.class, () -> {
        assertTimeout(REQUEST_TIMEOUT, () -> this.client.login(request));
      });

    assertEquals(
      "Server responded with an unexpected content type 'text/plain'",
      ex.getMessage());
  }

  @Test
  public void testLoginOK()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    final var request = new PLoginUsernamePasswordRequest();
    request.user = "user";
    request.password = "password";

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText(
            "matrix-login-response-0.json")));

    final var response =
      (PLoginResponse) assertTimeout(
        REQUEST_TIMEOUT, () -> this.client.login(request));

    assertEquals(
      "4a3617c1b42bce4e983499630cfb2d91",
      response.accessToken);
    assertEquals("example.com", response.homeServer);
    assertEquals("@admin:example.com", response.userId);
  }
}
