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

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.io7m.portero.server.internal.PMatrixJSON.PAdminCreateUserResponse;
import static com.io7m.portero.server.internal.PMatrixJSON.PAdminNonce;
import static com.io7m.portero.server.internal.PMatrixJSON.PError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  public void testNonceError()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.mockServer
      .when(request("/_synapse/admin/v1/register"))
      .respond(
        response()
          .withStatusCode(400)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-error-0.json")));

    final var error = (PError) this.client.nonce();
    assertEquals("M_BAD_JSON", error.errorCode);
    assertEquals("Bad JSON", error.errorMessage);
  }

  @Test
  public void testNonceOK()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.mockServer
      .when(request("/_synapse/admin/v1/register"))
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-nonce-0.json")));

    final var nonce = (PAdminNonce) this.client.nonce();
    assertEquals(
      "69b5b3da2e2e04b3a7f9426a13bba18b464217ba60117fd0541e9b90f1265083",
      nonce.nonce);
  }

  @Test
  public void testNonceErrorFailure()
  {
    this.mockServer.stop();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));
    this.mockServer.close();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));

    assertThrows(ConnectException.class, () -> this.client.nonce());
  }

  @Test
  public void testRegisterError()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.mockServer
      .when(request("/_synapse/admin/v1/register"))
      .respond(
        response()
          .withStatusCode(400)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-error-0.json")));

    final var error = (PError) this.client.register(
      "abcd",
      "nonce",
      "user",
      "password");
    assertEquals("M_BAD_JSON", error.errorCode);
    assertEquals("Bad JSON", error.errorMessage);
  }

  @Test
  public void testRegisterOK()
    throws Exception
  {
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.mockServer
      .when(request("/_synapse/admin/v1/register"))
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-create-user-0.json")));

    final var user = (PAdminCreateUserResponse) this.client.register(
      "abcd",
      "nonce",
      "user",
      "password");
    assertEquals("@admin:example.com", user.userId);
    assertEquals("4a3617c1b42bce4e983499630cfb2d91", user.accessToken);
    assertEquals("VSSVPWMCLG", user.deviceId);
    assertEquals("example.com", user.homeServer);
  }

  @Test
  public void testRegisterErrorFailure()
  {
    this.mockServer.stop();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));
    this.mockServer.close();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));

    assertThrows(
      ConnectException.class,
      () -> this.client.register("abcd",
                                 "nonce",
                                 "user",
                                 "password"));
  }
}
