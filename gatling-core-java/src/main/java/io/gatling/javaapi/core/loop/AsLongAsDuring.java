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

package io.gatling.javaapi.core.loop;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.StructureBuilder;
import io.gatling.javaapi.core.internal.loop.ScalaAsLongAsDuring;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Methods for defining "asLongAsDuring" loops.
 *
 * <p>Important: instances are immutable so any method doesn't mutate the existing instance but
 * returns a new one.
 *
 * @param <T> the type of {@link StructureBuilder} to attach to and to return
 * @param <W> the type of wrapped Scala instance
 */
public interface AsLongAsDuring<
    T extends StructureBuilder<T, W>, W extends io.gatling.core.structure.StructureBuilder<W>> {

  T make(Function<W, W> f);

  // Gatling EL condition
  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration, expressed as a Gatling Expression Language String that
   *     must either evaluate to an {@link Integer} (seconds then) or a {@link Duration}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull String condition, @Nonnull String duration) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString());
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration in seconds
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull String condition, long duration) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration));
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull String condition, Duration duration) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString());
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration function
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull String condition, Function<Session, Duration> duration) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString());
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration, expressed as a Gatling Expression Language String that
   *     must either evaluate to an {@link Integer} (seconds then) or a {@link Duration}
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, @Nonnull String duration, @Nonnull String counterName) {
    return asLongAsDuring(condition, duration, counterName, false);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, long duration, @Nonnull String counterName) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration), counterName);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, Duration duration, @Nonnull String counterName) {
    return asLongAsDuring(condition, duration, counterName, false);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration function
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition,
      Function<Session, Duration> duration,
      @Nonnull String counterName) {
    return asLongAsDuring(condition, duration, counterName, false);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration, expressed as a Gatling Expression Language String that
   *     must either evaluate to an {@link Integer} (seconds then) or a {@link Duration}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, @Nonnull String duration, boolean exitASAP) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString(), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration in seconds
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull String condition, long duration, boolean exitASAP) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull String condition, Duration duration, boolean exitASAP) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString(), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration function
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, Function<Session, Duration> duration, boolean exitASAP) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString(), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration, expressed as a Gatling Expression Language String that
   *     must either evaluate to an {@link Integer} (seconds then) or a {@link Duration}
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition,
      @Nonnull String duration,
      @Nonnull String counterName,
      boolean exitASAP) {
    return new On<>(ScalaAsLongAsDuring.apply(this, condition, duration, counterName, exitASAP));
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, long duration, @Nonnull String counterName, boolean exitASAP) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration), counterName, exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition, Duration duration, @Nonnull String counterName, boolean exitASAP) {
    return new On<>(ScalaAsLongAsDuring.apply(this, condition, duration, counterName, exitASAP));
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a Gatling Expression Language String
   * @param duration the maximum duration function
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull String condition,
      Function<Session, Duration> duration,
      @Nonnull String counterName,
      boolean exitASAP) {
    return new On<>(ScalaAsLongAsDuring.apply(this, condition, duration, counterName, exitASAP));
  }

  // Function condition
  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration, expressed as a function
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      @Nonnull Function<Session, Duration> duration) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString());
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration in seconds
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(@Nonnull Function<Session, Boolean> condition, long duration) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration));
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration in seconds
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition, @Nonnull Duration duration) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString());
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration, expressed as a function
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      @Nonnull Function<Session, Duration> duration,
      @Nonnull String counterName) {
    return asLongAsDuring(condition, duration, counterName, false);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition, long duration, @Nonnull String counterName) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration), counterName);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      @Nonnull Duration duration,
      @Nonnull String counterName) {
    return asLongAsDuring(condition, duration, counterName, false);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration, expressed as a function
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      @Nonnull Function<Session, Duration> duration,
      boolean exitASAP) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString(), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration in seconds
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition, long duration, boolean exitASAP) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition, @Nonnull Duration duration, boolean exitASAP) {
    return asLongAsDuring(condition, duration, UUID.randomUUID().toString(), exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration, expressed as a function
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      @Nonnull Function<Session, Duration> duration,
      @Nonnull String counterName,
      boolean exitASAP) {
    return new On<>(ScalaAsLongAsDuring.apply(this, condition, duration, counterName, exitASAP));
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      long duration,
      @Nonnull String counterName,
      boolean exitASAP) {
    return asLongAsDuring(condition, Duration.ofSeconds(duration), counterName, exitASAP);
  }

  /**
   * Define a loop that will iterate as long as the condition holds true and a maximum duration
   * isn't reached
   *
   * @param condition the condition, expressed as a function
   * @param duration the maximum duration
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @param exitASAP if the loop must be interrupted if the condition becomes false or the maximum
   *     duration inside the loop
   * @return a DSL component for defining the loop content
   */
  @Nonnull
  default On<T> asLongAsDuring(
      @Nonnull Function<Session, Boolean> condition,
      @Nonnull Duration duration,
      @Nonnull String counterName,
      boolean exitASAP) {
    return new On<>(ScalaAsLongAsDuring.apply(this, condition, duration, counterName, exitASAP));
  }

  /**
   * A DSL component for defining the loop content
   *
   * @param <T> the type of {@link StructureBuilder} to attach to and to return
   */
  final class On<T extends StructureBuilder<T, ?>> {
    private final ScalaAsLongAsDuring.Loop<T, ?> wrapped;

    On(ScalaAsLongAsDuring.Loop<T, ?> wrapped) {
      this.wrapped = wrapped;
    }

    /**
     * Define the loop content
     *
     * @param chain the loop content
     * @return a new {@link StructureBuilder}
     */
    @Nonnull
    public T on(@Nonnull ChainBuilder chain) {
      return wrapped.loop(chain);
    }
  }
}
