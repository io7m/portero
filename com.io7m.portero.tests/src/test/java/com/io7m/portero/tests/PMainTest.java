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

import com.io7m.portero.cmdline.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public final class PMainTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PMainTest.class);

  private PrintStream err;
  private PrintStream out;
  private PrintStream errOld;
  private PrintStream outOld;
  private ByteArrayOutputStream errBase;
  private ByteArrayOutputStream outBase;
  private String errText;
  private String outText;
  private ClientAndServer mockServer;

  @BeforeEach
  public void setup()
  {
    this.mockServer = startClientAndServer(10000);
    while (!this.mockServer.hasStarted()) {
      LOG.debug("waiting for mockserver to start");
    }

    this.errBase = new ByteArrayOutputStream();
    this.err = new PrintStream(this.errBase);
    this.outBase = new ByteArrayOutputStream();
    this.out = new PrintStream(this.outBase);
    this.errOld = System.err;
    this.outOld = System.out;

    System.setErr(this.err);
    System.setOut(this.out);
  }

  @AfterEach
  public void tearDown()
  {
    System.setErr(this.errOld);
    System.setOut(this.outOld);

    this.mockServer.stop();
    while (!this.mockServer.hasStopped()) {
      LOG.debug("waiting for mockserver to stop");
    }
  }

  @Test
  public void testNoArguments()
  {
    this.execute(new String[]{}, 1);
    Assertions.assertTrue(this.errText.contains("Usage: portero"));
    Assertions.assertTrue(this.outText.isEmpty());
  }

  @Test
  public void testHelp()
  {
    this.execute(new String[]{"help"}, 0);
    Assertions.assertTrue(this.errText.contains("Commands:"));
    Assertions.assertTrue(this.outText.isEmpty());
  }

  @Test
  public void testHelpHelp()
  {
    this.execute(new String[]{"help", "help"}, 0);
    Assertions.assertTrue(this.errText.contains("Usage: help [options] command"));
    Assertions.assertTrue(this.outText.isEmpty());
  }

  @Test
  public void testHelpInvite()
  {
    this.execute(new String[]{"help", "invite"}, 0);
    Assertions.assertTrue(this.errText.contains(
      "Generate a new invite URL for a user."));
    Assertions.assertTrue(this.outText.isEmpty());
  }

  @Test
  public void testHelpVersion()
  {
    this.execute(new String[]{"help", "version"}, 0);
    Assertions.assertTrue(this.errText.contains("Usage: version [options]"));
    Assertions.assertTrue(this.outText.isEmpty());
  }

  @Test
  public void testHelpServer()
  {
    this.execute(new String[]{"help", "server"}, 0);
    Assertions.assertTrue(this.errText.contains("Usage: server [options]"));
    Assertions.assertTrue(this.outText.isEmpty());
  }

  @Test
  public void testVersion()
  {
    this.execute(new String[]{"version"}, 0);
    Assertions.assertTrue(this.outText.contains("com.io7m.portero "));
  }

  @Test
  public void testServer()
    throws IOException
  {
    final var configFile =
      writeStandardConfigurationFile(20000, 20001);

    try {
      Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5L), () -> {
        this.execute(new String[]{
          "server",
          "--configuration-file",
          configFile.toString()
        }, 0);
      });
    } catch (final AssertionFailedError e) {
      if (!e.getMessage().contains("execution timed out")) {
        Assertions.fail();
      }
    }
  }

  @Test
  public void testInvite()
    throws IOException
  {
    final var configFile =
      writeStandardConfigurationFile(10000, 10001);

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(200)
          .withContentType(MediaType.TEXT_PLAIN)
          .withBody("https://result/?token=abcd"));

    this.execute(new String[]{
      "invite",
      "--configuration-file",
      configFile.toString()
    }, 0);
    Assertions.assertTrue(this.outText.contains("https://result/?token=abcd"));
  }

  private static Path writeStandardConfigurationFile(
    final int privatePort,
    final int publicPort)
    throws IOException
  {
    final var configFile =
      Files.createTempFile("server", "properties");

    try (var writer = Files.newBufferedWriter(configFile)) {
      List.of(
        "matrix.adminPassword = password",
        "matrix.adminURL = http://127.0.0.1:30000/",
        "matrix.adminUser = admin",
        "matrix.publicURL = https://chat.example.com",
        "server.privateAddress = 127.0.0.1",
        "server.privatePort = " + privatePort,
        "server.publicAddress = 127.0.0.1",
        "server.publicPort = " + publicPort,
        "server.publicURL = https://invite.example.com/",
        "server.threadCount = 4",
        "server.title = chat.example.com")
        .forEach(line -> {
          try {
            writer.write(line);
            writer.newLine();
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
          }
        });
      writer.flush();
    }
    return configFile;
  }

  @Test
  public void testInviteServerFails()
    throws IOException
  {
    final var configFile =
      writeStandardConfigurationFile(10000, 10001);

    this.mockServer
      .when(request())
      .respond(
        response()
          .withStatusCode(500)
          .withContentType(MediaType.TEXT_PLAIN)
          .withBody("NO!"));

    this.execute(new String[]{
      "invite",
      "--configuration-file",
      configFile.toString()
    }, 1);
    Assertions.assertTrue(this.errText.contains("NO!"));
  }

  private void execute(
    final String[] args,
    final int expected)
  {
    final var main = new Main(args);
    main.run();
    System.setErr(this.errOld);
    System.setOut(this.outOld);
    this.errText = this.errBase.toString(StandardCharsets.UTF_8);
    this.outText = this.outBase.toString(StandardCharsets.UTF_8);
    LOG.debug("received err: {}", this.errText);
    LOG.debug("received out: {}", this.outText);
    Assertions.assertEquals(expected, main.exitCode());
  }
}
