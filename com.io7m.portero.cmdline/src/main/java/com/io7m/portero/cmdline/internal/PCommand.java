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

package com.io7m.portero.cmdline.internal;

import com.beust.jcommander.Parameter;
import com.io7m.claypot.core.CLPAbstractCommand;
import com.io7m.claypot.core.CLPCommandContextType;
import com.io7m.portero.server.PServerConfiguration;
import com.io7m.portero.server.PServerConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * The base type of commands.
 */

public abstract class PCommand extends CLPAbstractCommand
{
  private static final Logger LOG =
    LoggerFactory.getLogger(PCommand.class);

  @Parameter(
    required = true,
    description = "The configuration file",
    names = "--configuration-file")
  private Path configurationFile;

  private final PCommandStrings strings;

  PCommand(
    final CLPCommandContextType inContext)
  {
    super(inContext);

    try {
      this.strings = new PCommandStrings(Locale.getDefault());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected final PCommandStrings commandStrings()
  {
    return this.strings;
  }

  protected abstract Status executeCommand(
    PServerConfiguration configuration)
    throws Exception;

  @Override
  protected final Status executeActual()
    throws Exception
  {
    return this.executeCommand(parseConfigurationFile(this.configurationFile));
  }

  private static PServerConfiguration parseConfigurationFile(
    final Path target)
    throws Exception
  {
    try (var stream = Files.newInputStream(target)) {
      return PServerConfigurations.ofStream(stream);
    } catch (final Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("error: ", e);
      }
      LOG.error(
        "failed to parse configuration file: {}: {}",
        target,
        e.getMessage()
      );
      Stream.of(e.getSuppressed()).forEach(error -> {
        LOG.error("  {}", error.getMessage());
      });
      throw e;
    }
  }
}
