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

package io.gatling.javaapi.core.pause;

import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.StructureBuilder;
import io.gatling.javaapi.core.internal.pause.ScalaPaces;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Pace methods for defining point where a given virtual user can't go through too fast (pauses
 * otherwise).
 *
 * <p>Important: instances are immutable so any method doesn't mutate the existing instance but
 * returns a new one.
 *
 * @param <T> the type of {@link StructureBuilder} to attach to and to return
 * @param <W> the type of wrapped Scala instance
 */
public interface Paces<
    T extends StructureBuilder<T, W>, W extends io.gatling.core.structure.StructureBuilder<W>> {

  T make(Function<W, W> f);

  /////////////// long duration
  /**
   * Attach a pace action
   *
   * @param duration the duration of the pace in seconds
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(long duration) {
    return pace(duration, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action
   *
   * @param duration the duration of the pace in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(long duration, @Nonnull String counterName) {
    return pace(Duration.ofSeconds(duration), counterName);
  }

  /////////////// Duration duration
  /**
   * Attach a pace action
   *
   * @param duration the duration of the pace
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull Duration duration) {
    return pace(duration, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action
   *
   * @param duration the duration of the pace
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull Duration duration, @Nonnull String counterName) {
    return ScalaPaces.apply(this, duration, counterName);
  }

  /////////////// Gatling EL duration
  /**
   * Attach a pace action where the duration is defined as a Gatling Expression Language string.
   * This expression must resolve to either an {@link Integer}, then the unit will be seconds, or a
   * {@link Duration}.
   *
   * @param duration the duration of the pace
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull String duration) {
    return pace(duration, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action where the duration is defined as a Gatling Expression Language string.
   * This expression must resolve to either an {@link Integer}, then the unit will be seconds, or a
   * {@link Duration}.
   *
   * @param duration the duration of the pace
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull String duration, @Nonnull String counterName) {
    return ScalaPaces.apply(this, duration, counterName);
  }

  /////////////// Function duration
  /**
   * Attach a pace action
   *
   * @param duration the duration of the pace
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull Function<Session, Duration> duration) {
    return pace(duration, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action
   *
   * @param duration the duration of the pace
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull Function<Session, Duration> duration, String counterName) {
    return ScalaPaces.apply(this, duration, counterName);
  }

  /////////////// long min max
  /**
   * Attach a pace action where the duration is random between 2 bounds
   *
   * @param min the minimum duration of the pace in seconds
   * @param max the maximum duration of the pace in seconds
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(long min, long max) {
    return pace(min, max, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action where the duration is random between 2 bounds
   *
   * @param min the minimum duration of the pace in seconds
   * @param max the maximum duration of the pace in seconds
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(long min, long max, @Nonnull String counterName) {
    return pace(Duration.ofSeconds(min), Duration.ofSeconds(max), counterName);
  }

  /////////////// Duration min max
  /**
   * Attach a pace action where the duration is random between 2 bounds
   *
   * @param min the minimum duration of the pace
   * @param max the maximum duration of the pace
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull Duration min, @Nonnull Duration max) {
    return pace(min, max, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action where the duration is random between 2 bounds
   *
   * @param min the minimum duration of the pace
   * @param max the maximum duration of the pace
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull Duration min, @Nonnull Duration max, @Nonnull String counterName) {
    return ScalaPaces.apply(this, min, max, counterName);
  }

  /////////////// Gatling EL min max
  /**
   * Attach a pace action where the duration is random between 2 bounds as Gatling Expression
   * Language strings. These expressions must resolve to either {@link Integer}s, then the unit will
   * be seconds, or {@link Duration}s.
   *
   * @param min the minimum duration of the pace
   * @param max the maximum duration of the pace
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(@Nonnull String min, @Nonnull String max, @Nonnull String counterName) {
    return ScalaPaces.apply(this, min, max, counterName);
  }

  /////////////// Function min max
  /**
   * Attach a pace action where the duration is random between 2 bounds as functions
   *
   * @param min the minimum duration of the pace
   * @param max the maximum duration of the pace
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(
      @Nonnull Function<Session, Duration> min, @Nonnull Function<Session, Duration> max) {
    return pace(min, max, UUID.randomUUID().toString());
  }

  /**
   * Attach a pace action where the duration is random between 2 bounds as functions
   *
   * @param min the minimum duration of the pace
   * @param max the maximum duration of the pace
   * @param counterName the name of the loop counter, as stored in the {@link Session}
   * @return a new StructureBuilder
   */
  @Nonnull
  default T pace(
      @Nonnull Function<Session, Duration> min,
      @Nonnull Function<Session, Duration> max,
      @Nonnull String counterName) {
    return ScalaPaces.apply(this, min, max, counterName);
  }
}
