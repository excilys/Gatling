/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.javaapi.http;

import static io.gatling.javaapi.core.internal.Expressions.*;

import io.gatling.javaapi.core.Session;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * DSL for building <a href="https://en.wikipedia.org/wiki/MIME#Multipart_messages">multipart</a>
 * request body parts.
 *
 * <p>Immutable, so all methods return a new occurrence and leave the original unmodified.
 */
public final class BodyPart {

  private final io.gatling.http.request.BodyPart wrapped;

  BodyPart(io.gatling.http.request.BodyPart wrapped) {
    this.wrapped = wrapped;
  }

  public io.gatling.http.request.BodyPart asScala() {
    return wrapped;
  }

  /**
   * Define the contentType attribute
   *
   * @param contentType the contentType attribute, expressed as a Gatling Expression Language String
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart contentType(@Nonnull String contentType) {
    return new BodyPart(wrapped.contentType(toStringExpression(contentType)));
  }

  /**
   * Define the contentType attribute
   *
   * @param contentType the contentType attribute, expressed as a function
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart contentType(@Nonnull Function<Session, String> contentType) {
    return new BodyPart(wrapped.contentType(javaFunctionToExpression(contentType)));
  }

  /**
   * Define the charset attribute
   *
   * @param charset the static charset attribute
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart charset(@Nonnull String charset) {
    return new BodyPart(wrapped.charset((charset)));
  }

  /**
   * Define the dispositionType attribute
   *
   * @param dispositionType the dispositionType attribute, expressed as a Gatling Expression
   *     Language String
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart dispositionType(@Nonnull String dispositionType) {
    return new BodyPart(wrapped.dispositionType(toStringExpression(dispositionType)));
  }

  /**
   * Define the dispositionType attribute
   *
   * @param dispositionType the dispositionType attribute, expressed as a function
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart dispositionType(@Nonnull Function<Session, String> dispositionType) {
    return new BodyPart(wrapped.dispositionType(javaFunctionToExpression(dispositionType)));
  }

  /**
   * Define the fileName attribute
   *
   * @param fileName the fileName attribute, expressed as a Gatling Expression Language String
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart fileName(@Nonnull String fileName) {
    return new BodyPart(wrapped.fileName(toStringExpression(fileName)));
  }

  /**
   * Define the fileName attribute
   *
   * @param fileName the fileName attribute, expressed as a function
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart fileName(@Nonnull Function<Session, String> fileName) {
    return new BodyPart(wrapped.fileName(javaFunctionToExpression(fileName)));
  }

  /**
   * Define the contentId attribute
   *
   * @param contentId the contentId attribute, expressed as a Gatling Expression Language String
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart contentId(@Nonnull String contentId) {
    return new BodyPart(wrapped.contentId(toStringExpression(contentId)));
  }

  /**
   * Define the contentId attribute
   *
   * @param contentId the contentId attribute, expressed as a function
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart contentId(@Nonnull Function<Session, String> contentId) {
    return new BodyPart(wrapped.contentId(javaFunctionToExpression(contentId)));
  }

  /**
   * Define the transferEncoding attribute
   *
   * @param transferEncoding the static transferEncoding attribute
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart transferEncoding(@Nonnull String transferEncoding) {
    return new BodyPart(wrapped.transferEncoding(transferEncoding));
  }

  /**
   * Define a header
   *
   * @param name the header name, expressed as a Gatling Expression Language String
   * @param value the header value, expressed as a Gatling Expression Language String
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart header(@Nonnull String name, @Nonnull String value) {
    return new BodyPart(wrapped.header(toStringExpression(name), toStringExpression(value)));
  }

  /**
   * Define a header
   *
   * @param name the header name, expressed as a Gatling Expression Language String
   * @param value the header value, expressed as a function
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart header(@Nonnull String name, @Nonnull Function<Session, String> value) {
    return new BodyPart(wrapped.header(toStringExpression(name), javaFunctionToExpression(value)));
  }

  /**
   * Define a header
   *
   * @param name the header name, expressed as a function
   * @param value the header value, expressed as a Gatling Expression Language String
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart header(@Nonnull Function<Session, String> name, @Nonnull String value) {
    return new BodyPart(wrapped.header(javaFunctionToExpression(name), toStringExpression(value)));
  }

  /**
   * Define a header
   *
   * @param name the header name, expressed as a function
   * @param value the header value, expressed as a function
   * @return a new BodyPart instance
   */
  @Nonnull
  public BodyPart header(
      @Nonnull Function<Session, String> name, @Nonnull Function<Session, String> value) {
    return new BodyPart(
        wrapped.header(javaFunctionToExpression(name), javaFunctionToExpression(value)));
  }
}
