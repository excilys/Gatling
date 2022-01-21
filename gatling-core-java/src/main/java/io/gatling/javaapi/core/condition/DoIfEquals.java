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

package io.gatling.javaapi.core.condition;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.StructureBuilder;
import io.gatling.javaapi.core.internal.condition.ScalaDoIfEquals;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Methods for defining "doIfEquals" conditional blocks.
 *
 * <p>Important: instances are immutable so any method doesn't mutate the existing instance but
 * returns a new one.
 *
 * @param <T> the type of {@link StructureBuilder} to attach to and to return
 * @param <W> the type of wrapped Scala instance
 */
public interface DoIfEquals<
    T extends StructureBuilder<T, W>, W extends io.gatling.core.structure.StructureBuilder<W>> {

  T make(Function<W, W> f);

  // Gatling EL actual

  /**
   * Execute the "then" block only if the actual value is equal to the expected one
   *
   * @param actual the actual value expressed as a Gatling Expression Language String
   * @param expected the expected value expressed as a Gatling Expression Language String
   * @return a DSL component for defining the "then" block
   */
  @Nonnull
  default Then<T> doIfEquals(@Nonnull String actual, @Nonnull String expected) {
    return new Then<>(ScalaDoIfEquals.apply(this, actual, expected));
  }

  /**
   * Execute the "then" block only if the actual value is equal to the expected one
   *
   * @param actual the actual value expressed as a Gatling Expression Language String
   * @param expected the expected static value
   * @return a DSL component for defining the "then" block
   */
  @Nonnull
  default Then<T> doIfEquals(@Nonnull String actual, @Nonnull Object expected) {
    return new Then<>(ScalaDoIfEquals.apply(this, actual, expected));
  }

  /**
   * Execute the "then" block only if the actual value is equal to the expected one
   *
   * @param actual the actual value expressed as a Gatling Expression Language String
   * @param expected the expected value expressed as a function
   * @return a DSL component for defining the "then" block
   */
  @Nonnull
  default Then<T> doIfEquals(@Nonnull String actual, @Nonnull Function<Session, Object> expected) {
    return new Then<>(ScalaDoIfEquals.apply(this, actual, expected));
  }

  // Function actual
  /**
   * Execute the "then" block only if the actual value is equal to the expected one
   *
   * @param actual the actual value expressed as a function
   * @param expected the expected value expressed as a Gatling Expression Language String
   * @return a DSL component for defining the "then" block
   */
  @Nonnull
  default Then<T> doIfEquals(@Nonnull Function<Session, Object> actual, @Nonnull String expected) {
    return new Then<>(ScalaDoIfEquals.apply(this, actual, expected));
  }

  /**
   * Execute the "then" block only if the actual value is equal to the expected one
   *
   * @param actual the actual value expressed as a function
   * @param expected the expected static value
   * @return a DSL component for defining the "then" block
   */
  @Nonnull
  default Then<T> doIfEquals(@Nonnull Function<Session, Object> actual, Object expected) {
    return new Then<>(ScalaDoIfEquals.apply(this, actual, expected));
  }

  /**
   * Execute the "then" block only if the actual value is equal to the expected one
   *
   * @param actual the actual value expressed as a function
   * @param expected the expected value expressed as a function
   * @return a DSL component for defining the "then" block
   */
  @Nonnull
  default Then<T> doIfEquals(
      @Nonnull Function<Session, Object> actual, @Nonnull Function<Session, Object> expected) {
    return new Then<>(ScalaDoIfEquals.apply(this, actual, expected));
  }

  /**
   * The DSL component for defining the "then" block
   *
   * @param <T> the type of {@link StructureBuilder} to attach to and to return
   */
  final class Then<T extends StructureBuilder<T, ?>> {
    private final ScalaDoIfEquals.Then<T, ?> wrapped;

    Then(ScalaDoIfEquals.Then<T, ?> wrapped) {
      this.wrapped = wrapped;
    }

    /**
     * Define the chain to be executed when the actual and expected values are equal
     *
     * @param chain the "then" chain
     * @return a new {@link StructureBuilder}
     */
    @Nonnull
    public T then(ChainBuilder chain) {
      return wrapped.then_(chain);
    }
  }
}
