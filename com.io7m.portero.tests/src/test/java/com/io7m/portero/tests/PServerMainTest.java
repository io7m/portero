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
import org.apache.commons.io.input.CharSequenceInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public final class PServerMainTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PServerMainTest.class);
  private PServerConfiguration config;
  private PServerMain server;
  private HttpClient client;
  private URI publicBaseUri;
  private URI privateBaseUri;
  private URI matrixBaseUri;
  private ClientAndServer mockServer;

  private static Document parseXML(
    final String text)
    throws Exception
  {
    final var builders = DocumentBuilderFactory.newInstance();
    builders.setFeature(
      "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
      false);
    builders.setFeature(
      "http://apache.org/xml/features/nonvalidating/load-external-dtd",
      false);
    builders.setFeature("http://xml.org/sax/features/namespaces", false);
    builders.setFeature("http://xml.org/sax/features/validation", false);
    builders.setFeature(FEATURE_SECURE_PROCESSING, true);
    builders.setNamespaceAware(true);
    builders.setValidating(false);
    builders.setXIncludeAware(false);

    final var builder = builders.newDocumentBuilder();
    try (var stream = new CharSequenceInputStream(text, UTF_8)) {
      return builder.parse(stream);
    }
  }

  private String generateToken()
    throws IOException, InterruptedException
  {
    final var request =
      HttpRequest.newBuilder(this.privateBaseUri)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals(
      "text/plain",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var tokenURI =
      response.body();
    final var tokenPieces =
      tokenURI.split("\\s+");
    final var tokenTrimmed =
      tokenPieces[0].substring(tokenURI.indexOf('=') + 1).trim();

    LOG.debug("generated token {}", tokenTrimmed);
    return tokenTrimmed;
  }

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.client = HttpClient.newHttpClient();

    this.publicBaseUri =
      URI.create("http://127.0.0.1:20000/");
    this.privateBaseUri =
      URI.create("http://127.0.0.1:20001/");
    this.matrixBaseUri =
      URI.create("http://127.0.0.1:10000/");

    this.mockServer = startClientAndServer(10000);
    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.config =
      PServerConfiguration.builder()
        .setMatrixServerAdminConnectionURI(this.matrixBaseUri)
        .setMatrixServerAdminRegistrationSecret(
          "b07b6614ecb96d689f835e4798f24e05b552d12aedfbda2ff54fb610cd2b0e29")
        .setBindPrivateAddress(InetAddress.getByName("127.0.0.1"))
        .setBindPrivatePort(20001)
        .setBindPublicAddress(InetAddress.getByName("127.0.0.1"))
        .setBindPublicPort(20000)
        .setServerTitle("chat.example.com")
        .setPublicURI(URI.create("http://invite.example.com"))
        .setMatrixServerPublicURI(URI.create("http://chat.example.com"))
        .build();

    this.server = PServerMain.create(this.config);
    this.server.start();
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    LOG.debug("tearing down");
    this.server.stop();

    this.mockServer.stop();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));
    this.mockServer.close();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));

    LOG.debug("tore down");
  }

  /**
   * The root URI returns something parseable as XML.
   *
   * @throws Exception On errors
   */

  @Test
  public void testRoot()
    throws Exception
  {
    final var request =
      HttpRequest.newBuilder(this.publicBaseUri)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals(
      "application/xhtml+xml",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
  }

  /**
   * The static CSS is returned successfully.
   *
   * @throws Exception On errors
   */

  @Test
  public void testStaticCSS()
    throws Exception
  {
    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("static/style.css"))
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals(
      "text/css",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var body = response.body();
    LOG.debug("received: {}", body);
    assertTrue(body.contains("#main"));
  }

  /**
   * The static URI fails to return missing files.
   *
   * @throws Exception On errors
   */

  @Test
  public void testStaticMissing()
    throws Exception
  {
    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("static/missing"))
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(404, response.statusCode());
    assertEquals(
      "application/xhtml+xml",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("404"));
  }

  /**
   * The generation URI provides a usable token.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInviteGeneration()
    throws Exception
  {
    final var request =
      HttpRequest.newBuilder(this.privateBaseUri)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals(
      "text/plain",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var body = response.body();
    LOG.debug("received: {}", body);
    assertTrue(body.startsWith("http://invite.example.com/signup/?token="));
  }

  /**
   * The signup page can't work without a token.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupTokenMissing()
    throws Exception
  {
    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup/"))
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());
    assertEquals(
      "application/xhtml+xml",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains("Missing token."));
  }

  /**
   * The signup page renders successfully when a token is provided.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupTokenPresent()
    throws Exception
  {
    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup/?token=abcd"))
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals(
      "application/xhtml+xml",
      response.headers().firstValue("Content-Type").orElseThrow());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
  }

  /**
   * The complete signup procedure works if the Matrix server says it worked.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupCompleteOK()
    throws Exception
  {
    final String token = this.generateToken();

    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));

    this.mockServer
      .when(request("/_synapse/admin/v1/register"))
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-nonce-0.json")));

    this.mockServer
      .when(request("/_synapse/admin/v1/register"))
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-create-user-0.json")));

    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=");
    bodyBuilder.append(token);
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("Registration successful"));
  }

  /**
   * The complete signup procedure fails if the Matrix server fails.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupCompleteMatrixServerUnreachable()
    throws Exception
  {
    final String token = this.generateToken();

    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));
    this.mockServer.stop();
    while (!this.mockServer.hasStopped()) {
      LOG.debug("waiting for mock server to stop");
    }
    this.mockServer.close();
    assertTrue(this.mockServer.hasStopped(100, 5L, TimeUnit.SECONDS));

    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=");
    bodyBuilder.append(token);
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("java.net.ConnectException"));
  }

  /**
   * The complete signup procedure fails if the Matrix server returns an error.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupCompleteMatrixServerError()
    throws Exception
  {
    final String token = this.generateToken();

    assertTrue(this.mockServer.hasStarted(100, 5L, TimeUnit.SECONDS));
    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(400)
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(PTestResources.resourceText("matrix-error-0.json")));

    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=");
    bodyBuilder.append(token);
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("Error 400"));
    assertTrue(body.contains(
      "The Matrix server returned an error: M_BAD_JSON: Bad JSON"));
  }

  /**
   * The signup completion page requires all data to be present.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupMissingToken()
    throws Exception
  {
    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains("Missing or invalid token."));
  }

  /**
   * The signup completion page requires all data to be present.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupMissingUser()
    throws Exception
  {
    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=28f9fbd7e9d5f118b50b05f6208c1c48");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains("Missing or invalid user."));
  }

  /**
   * The signup completion page requires all data to be present.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupMissingEmail()
    throws Exception
  {
    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=28f9fbd7e9d5f118b50b05f6208c1c48");
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains("Missing or invalid email."));
  }

  /**
   * The signup completion page requires all data to be present.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupMissingPassword()
    throws Exception
  {
    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=28f9fbd7e9d5f118b50b05f6208c1c48");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&password_confirm=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains("Missing or invalid password."));
  }

  /**
   * The signup completion page requires all data to be present.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupMissingPasswordConfirm()
    throws Exception
  {
    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=28f9fbd7e9d5f118b50b05f6208c1c48");
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains("Missing or invalid password confirmation."));
  }

  /**
   * The signup completion page requires a properly confirmed password.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSignupPasswordIncorrect()
    throws Exception
  {
    final var bodyBuilder = new StringBuilder(128);
    bodyBuilder.append("token=28f9fbd7e9d5f118b50b05f6208c1c48");
    bodyBuilder.append("&user_name=user");
    bodyBuilder.append("&email=user@example.com");
    bodyBuilder.append("&password=password");
    bodyBuilder.append("&password_confirm=password2");
    final var bodyBytes = bodyBuilder.toString().getBytes(UTF_8);

    final var send =
      HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

    final var request =
      HttpRequest.newBuilder(this.publicBaseUri.resolve("/signup-complete/"))
        .header("content-type", "application/x-www-form-urlencoded")
        .POST(send)
        .build();

    final var response =
      this.client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode());

    final var body = response.body();
    LOG.debug("received: {}", body);
    parseXML(body);
    assertTrue(body.contains("chat.example.com"));
    assertTrue(body.contains("400"));
    assertTrue(body.contains(
      "Password confirmation does not match the password."));
  }
}
