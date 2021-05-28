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

import com.beust.jcommander.Parameters;
import com.io7m.claypot.core.CLPCommandContextType;
import com.io7m.portero.server.PServerConfiguration;
import com.io7m.portero.server.PServers;

/**
 * The "server" command.
 */

@Parameters(commandDescription = "Start an invite server")
public final class PCommandServer extends PCommand
{
  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public PCommandServer(
    final CLPCommandContextType inContext)
  {
    super(inContext);
  }

  @Override
  protected Status executeCommand(
    final PServerConfiguration configuration)
    throws Exception
  {
    try (var server = PServers.createServer(configuration)) {
      server.start();
      while (true) {
        Thread.sleep(1_000L);
      }
    }
  }

  @Override
  public String extendedHelp()
  {
    return this.commandStrings().format("server.help");
  }

  @Override
  public String name()
  {
    return "server";
  }
}
