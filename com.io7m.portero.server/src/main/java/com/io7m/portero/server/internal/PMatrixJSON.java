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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.StringJoiner;

// CHECKSTYLE:OFF

public final class PMatrixJSON
{
  private PMatrixJSON()
  {

  }

  /**
   * The base type of JSON objects.
   */

  public interface PMatrixJSONObjectType
  {

  }

  /**
   * The base type of JSON responses.
   */

  public interface PMatrixJSONResponseType extends PMatrixJSONObjectType
  {

  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PError
    implements PMatrixJSONResponseType
  {
    @JsonProperty(required = true, value = "errcode")
    public String errorCode;
    @JsonProperty(required = true, value = "error")
    public String errorMessage;

    public PError()
    {

    }

    @Override
    public String toString()
    {
      return new StringJoiner(
        ", ",
        PError.class.getSimpleName() + "[",
        "]")
        .add("errorCode='" + this.errorCode + "'")
        .add("errorMessage='" + this.errorMessage + "'")
        .toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PRegisterAuthDummy
    implements PMatrixJSONObjectType
  {
    @JsonProperty(required = true, value = "type")
    public String type = "m.login.dummy";

    public PRegisterAuthDummy()
    {

    }

    @Override
    public String toString()
    {
      return new StringJoiner(
        ", ",
        PRegisterAuthDummy.class.getSimpleName() + "[",
        "]")
        .add("type='" + this.type + "'")
        .toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PRegisterUsernamePasswordRequest
    implements PMatrixJSONObjectType
  {
    @JsonProperty(required = true, value = "username")
    public String username;
    @JsonProperty(required = true, value = "password")
    public String password;
    @JsonProperty(required = true, value = "auth")
    public PRegisterAuthDummy auth = new PRegisterAuthDummy();

    public PRegisterUsernamePasswordRequest()
    {

    }

    @Override
    public String toString()
    {
      return new StringJoiner(
        ", ",
        PRegisterUsernamePasswordRequest.class.getSimpleName() + "[",
        "]")
        .add("username='" + this.username + "'")
        .add("password='" + this.password + "'")
        .add("auth=" + this.auth)
        .toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PLoginUsernamePasswordRequest
    implements PMatrixJSONObjectType
  {
    @JsonProperty(required = true, value = "type")
    public final String type = "m.login.password";
    @JsonProperty(required = true, value = "user")
    public String user;
    @JsonProperty(required = true, value = "password")
    public String password;

    public PLoginUsernamePasswordRequest()
    {

    }

    @Override
    public String toString()
    {
      return new StringJoiner(
        ", ",
        PLoginUsernamePasswordRequest.class.getSimpleName() + "[",
        "]")
        .add("user='" + this.user + "'")
        .add("password='" + this.password + "'")
        .add("type='" + this.type + "'")
        .toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PLoginResponse
    implements PMatrixJSONResponseType
  {
    @JsonProperty(required = true, value = "access_token")
    public String accessToken;
    @JsonProperty(required = true, value = "home_server")
    public String homeServer;
    @JsonProperty(required = true, value = "user_id")
    public String userId;

    public PLoginResponse()
    {

    }

    @Override
    public String toString()
    {
      return new StringJoiner(
        ", ",
        PRegisterResponse.class.getSimpleName() + "[",
        "]")
        .add("accessToken='" + this.accessToken + "'")
        .add("homeServer='" + this.homeServer + "'")
        .add("userId='" + this.userId + "'")
        .toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PRegisterResponse
    implements PMatrixJSONResponseType
  {
    @JsonProperty(required = true, value = "access_token")
    public String accessToken;
    @JsonProperty(required = true, value = "home_server")
    public String homeServer;
    @JsonProperty(required = true, value = "user_id")
    public String userId;

    public PRegisterResponse()
    {

    }

    @Override
    public String toString()
    {
      return new StringJoiner(
        ", ",
        PRegisterResponse.class.getSimpleName() + "[",
        "]")
        .add("accessToken='" + this.accessToken + "'")
        .add("homeServer='" + this.homeServer + "'")
        .add("userId='" + this.userId + "'")
        .toString();
    }
  }
}
