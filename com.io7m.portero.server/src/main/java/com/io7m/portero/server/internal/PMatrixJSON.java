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
  public static final class PAdminNonce
    implements PMatrixJSONResponseType
  {
    @JsonProperty(required = true, value = "nonce")
    public String nonce;

    public PAdminNonce()
    {

    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder("PAdminNonce{");
      sb.append("nonce='").append(this.nonce).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PAdminCreateUser
  {
    @JsonProperty(required = true, value = "admin")
    public final boolean admin = false;
    @JsonProperty(required = true, value = "nonce")
    public String nonce;
    @JsonProperty(required = true, value = "username")
    public String username;
    @JsonProperty(required = true, value = "password")
    public String password;
    @JsonProperty(required = true, value = "mac")
    public String mac;

    public PAdminCreateUser()
    {

    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder(
        "PAdminCreateUser{");
      sb.append("nonce='").append(this.nonce).append('\'');
      sb.append(", username='").append(this.username).append('\'');
      sb.append(", password='").append(this.password).append('\'');
      sb.append(", admin=").append(this.admin);
      sb.append(", mac='").append(this.mac).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }

  @JsonDeserialize
  @JsonSerialize
  public static final class PAdminCreateUserResponse
    implements PMatrixJSONResponseType
  {
    @JsonProperty(required = true, value = "access_token")
    public String accessToken;
    @JsonProperty(required = true, value = "user_id")
    public String userId;
    @JsonProperty(required = true, value = "home_server")
    public String homeServer;
    @JsonProperty(required = true, value = "device_id")
    public String deviceId;

    public PAdminCreateUserResponse()
    {

    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder(
        "PAdminCreateUserResponse{");
      sb.append("accessToken='").append(this.accessToken).append('\'');
      sb.append(", userId='").append(this.userId).append('\'');
      sb.append(", homeServer='").append(this.homeServer).append('\'');
      sb.append(", deviceId='").append(this.deviceId).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }
}
