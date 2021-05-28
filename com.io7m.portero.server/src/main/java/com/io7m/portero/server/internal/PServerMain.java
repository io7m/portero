/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */

package com.io7m.portero.server.internal;

import com.io7m.portero.server.PServerConfiguration;
import com.io7m.portero.server.PServerType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Objects;

/**
 * The main server.
 */

public final class PServerMain implements PServerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PServerMain.class);

  private final Server serverPrivate;
  private final PServerConfiguration configuration;
  private final Server serverPublic;

  private PServerMain(
    final PServerConfiguration inConfiguration,
    final Server inServerPublic,
    final Server inServerPrivate)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.serverPrivate =
      Objects.requireNonNull(inServerPrivate, "server");
    this.serverPublic =
      Objects.requireNonNull(inServerPublic, "server");
  }

  /**
   * Create a server.
   *
   * @param configuration The server configuration
   *
   * @return A server instance
   *
   * @throws IOException On errors
   */

  public static PServerMain create(
    final PServerConfiguration configuration)
    throws IOException
  {
    Objects.requireNonNull(configuration, "configuration");

    final var publicThreadPool =
      new QueuedThreadPool(configuration.serverThreadCount(), 1);
    final var privateThreadPool =
      new QueuedThreadPool(configuration.serverThreadCount(), 1);

    final var strings =
      new PServerStrings(configuration.locale());
    final var httpClient =
      HttpClient.newHttpClient();
    final var client =
      PMatrixClient.create(httpClient, configuration.matrixServerAdminConnectionURI());
    final var publicServer =
      new Server(publicThreadPool);
    final var privateServer =
      new Server(privateThreadPool);
    final var controller =
      PServerController.create(strings, client);

    final var pages = new PServerPages(configuration.locale());
    final var httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    httpConfig.setSendXPoweredBy(false);

    createPublicConnectors(configuration, publicServer, httpConfig);
    createPublicHandlers(configuration, publicServer, controller, pages);
    createPrivateHandlers(configuration, privateServer, controller, pages);
    createPrivateConnectors(configuration, privateServer, httpConfig);

    return new PServerMain(
      configuration,
      publicServer,
      privateServer
    );
  }

  private static void createPrivateHandlers(
    final PServerConfiguration configuration,
    final Server server,
    final PServerController controller,
    final PServerPages pages)
  {
    server.setHandler(
      new PServerInviteHandler(pages, controller, configuration));
    server.setErrorHandler(
      new PServerErrorHandler(pages, configuration));
  }

  private static void createPublicHandlers(
    final PServerConfiguration configuration,
    final Server server,
    final PServerController controller,
    final PServerPages pages)
  {
    final var contextRoot = new ContextHandler("/");
    contextRoot.setHandler(new PServerRootHandler(pages, configuration));

    final var contextStatic = new ContextHandler("/static");
    contextStatic.setHandler(new PServerStaticHandler(pages, configuration));

    final var contextSignup = new ContextHandler("/signup");
    contextSignup.setHandler(
      new PServerSignupHandler(pages, controller, configuration));

    final var contextSignupComplete = new ContextHandler("/signup-complete");
    contextSignupComplete.setHandler(
      new PServerSignupCompleteHandler(pages, controller, configuration));

    final var contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[]{
      contextRoot,
      contextStatic,
      contextSignup,
      contextSignupComplete,
    });
    server.setErrorHandler(new PServerErrorHandler(pages, configuration));
    server.setHandler(contexts);
  }

  private static void createPrivateConnectors(
    final PServerConfiguration configuration,
    final Server server,
    final HttpConfiguration httpConfig)
  {
    final var httpConnectionFactory =
      new HttpConnectionFactory(httpConfig);
    final var baseConnector =
      new ServerConnector(server, httpConnectionFactory);

    final var bindAddress =
      configuration.bindPrivateAddress();
    final var bindPort =
      configuration.bindPrivatePort();

    baseConnector.setReuseAddress(true);
    baseConnector.setHost(bindAddress.getHostAddress());
    baseConnector.setPort(bindPort);

    for (final var connector : server.getConnectors()) {
      try {
        connector.stop();
      } catch (final Exception e) {
        LOG.error("could not close connector: ", e);
      }
    }

    server.setConnectors(new Connector[]{
      baseConnector,
    });
  }

  private static void createPublicConnectors(
    final PServerConfiguration configuration,
    final Server server,
    final HttpConfiguration httpConfig)
  {
    final var httpConnectionFactory =
      new HttpConnectionFactory(httpConfig);
    final var baseConnector =
      new ServerConnector(server, httpConnectionFactory);

    final var bindAddress =
      configuration.bindPublicAddress();
    final var bindPort =
      configuration.bindPublicPort();

    baseConnector.setReuseAddress(true);
    baseConnector.setHost(bindAddress.getHostAddress());
    baseConnector.setPort(bindPort);

    for (final var connector : server.getConnectors()) {
      try {
        connector.stop();
      } catch (final Exception e) {
        LOG.error("could not close connector: ", e);
      }
    }

    server.setConnectors(new Connector[]{
      baseConnector,
    });
  }

  @Override
  public void start()
    throws Exception
  {
    LOG.info(
      "private server starting on {}:{}",
      this.configuration.bindPrivateAddress(),
      Integer.valueOf(this.configuration.bindPrivatePort())
    );
    this.serverPrivate.start();

    LOG.info(
      "public server starting on {}:{}",
      this.configuration.bindPublicAddress(),
      Integer.valueOf(this.configuration.bindPublicPort())
    );
    this.serverPublic.start();
  }

  @Override
  public void stop()
    throws Exception
  {
    LOG.debug("stopping private server");
    this.serverPrivate.stop();
    LOG.debug("stopping public server");
    this.serverPublic.stop();
  }
}
