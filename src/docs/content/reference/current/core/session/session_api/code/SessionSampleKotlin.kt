/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
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

import io.gatling.javaapi.core.Session

class SessionSampleKotlin {

  private class MyPojo
  val session: Session = TODO()

  init {
//#sessions-are-immutable
// wrong usage: result from first call is discarded
session.set("foo", "FOO")
session.set("bar", "BAR")

// correct usage
session.set("foo", "FOO").set("bar", "BAR")
//#sessions-are-immutable

//#set
// set one single attribute
val newSession1 = session.set("key", "whateverValue")
// set multiple attributes
val newSession2 = session.setAll(mapOf("key" to "value"))
// remove one single attribute
val newSession3 = session.remove("key")
// remove multiple attributes
val newSession4 = session.removeAll("key1", "key2")
// remove all non Gatling internal attributes
val newSession5 = session.reset()
//#set

//#get
// check if an attribute is stored in the session
val contains = session.contains("key")

// get an attribute value and cast it
val string = session.getString("key")

// get an int attribute (will throw if it's null)
val primitiveInt = session.getInt("key")
// get an Integer attribute
val intWrapper = session.getIntegerWrapper("key")

// get a long attribute (will throw if it's null)
val primitiveLong = session.getLong("key")
// get a Long attribute
val longWrapper = session.getLongWrapper("key")

// get a boolean attribute (will throw if it's null)
val primitiveBoolean = session.getBoolean("key")
// get a Boolean attribute
val booleanWrapper = session.getBooleanWrapper("key")

// get a double attribute (will throw if it's null)
val primitiveDouble = session.getDouble("key")
// get a Double attribute
val doubleWrapper = session.getDoubleWrapper("key")

// get an attribute value and cast it into a List
val list: List<MyPojo> = session.getList("key")
// get an attribute value and cast it into a Set
val set: Set<MyPojo> = session.getSet("key")
// get an attribute value and cast it into a Map
val map: Map<String, MyPojo> = session.getMap("key")
// get an attribute value and cast it
val myPojo: MyPojo = session.get("key")
//#get
  }

  init {
//#state
// return true if the virtual user has experienced a failure before this point
val failed: Boolean = session.isFailed()
// reset the state to success
// so that interrupt mechanisms such as exitHereIfFailed don't trigger
// reset the state to success
// so that interrupt mechanisms such as exitHereIfFailed don't trigger
val newSession1: Session = session.markAsSucceeded()
// force the state to failure
// so that interrupt mechanisms such as exitHereIfFailed do trigger
// force the state to failure
// so that interrupt mechanisms such as exitHereIfFailed do trigger
val newSession2: Session = session.markAsFailed()
//#state
  }
}
