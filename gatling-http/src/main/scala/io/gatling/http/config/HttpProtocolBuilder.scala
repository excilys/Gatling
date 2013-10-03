/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.http.config

import java.net.InetAddress

import scala.collection.mutable

import com.ning.http.client.{ ProxyServer, Request }
import com.typesafe.scalalogging.slf4j.Logging

import io.gatling.core.result.message.Status
import io.gatling.core.session.{ Expression, Session }
import io.gatling.http.HeaderNames._
import io.gatling.http.check.HttpCheck
import io.gatling.http.response.Response
import io.gatling.http.util.HttpHelper

/**
 * HttpProtocolBuilder class companion
 */
object HttpProtocolBuilder {

	val default = new HttpProtocolBuilder(HttpProtocol.default)

	val warmUpUrls = mutable.Set.empty[String]
}

/**
 * Builder for HttpProtocol used in DSL
 *
 * @param protocol the protocol being built
 * @param warmUpUrl a URL to be pinged in order to warm up the HTTP engine
 */
case class HttpProtocolBuilder(protocol: HttpProtocol) extends Logging {

	def baseURL(baseUrl: String) = copy(protocol = protocol.copy(baseURLs = List(baseUrl)))

	def baseURLs(baseUrl1: String, baseUrl2: String, baseUrls: String*) = copy(protocol = protocol.copy(baseURLs = baseUrl1 :: baseUrl2 :: baseUrls.toList))

	def baseURLs(baseUrls: Seq[String]) = copy(protocol = protocol.copy(baseURLs = baseUrls))

	def disableFollowRedirect = copy(protocol = protocol.copy(followRedirect = false))

	def disableAutoReferer = copy(protocol = protocol.copy(autoReferer = false))

	def disableCaching = copy(protocol = protocol.copy(cache = false))

	def disableResponseChunksDiscarding = copy(protocol = protocol.copy(discardResponseChunks = false))

	def disableClientSharing = copy(protocol = protocol.copy(shareClient = false))

	def shareConnections = copy(protocol = protocol.copy(shareConnections = true))

	def baseHeaders(headers: Map[String, String]) = protocol.copy(baseHeaders = protocol.baseHeaders ++ headers)

	def acceptHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT -> value)))

	def acceptCharsetHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT_CHARSET -> value)))

	def acceptEncodingHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT_ENCODING -> value)))

	def acceptLanguageHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT_LANGUAGE -> value)))

	def authorizationHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (AUTHORIZATION -> value)))

	def connection(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (CONNECTION -> value)))

	def doNotTrackHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (DO_NOT_TRACK -> value)))

	def userAgentHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (USER_AGENT -> value)))

	def warmUp(url: String) = copy(protocol = protocol.copy(warmUpUrl = Some(url)))

	def disableWarmUp = copy(protocol = protocol.copy(warmUpUrl = None))

	def basicAuth(username: Expression[String], password: Expression[String]) = copy(protocol = protocol.copy(basicAuth = Some(HttpHelper.buildRealm(username, password))))

	def virtualHost(virtualHost: Expression[String]) = protocol.copy(virtualHost = Some(virtualHost))

	def extraInfoExtractor(f: (Status, Session, Request, Response) => List[Any]) = copy(protocol = protocol.copy(extraInfoExtractor = Some(f)))

	def proxy(host: String, port: Int) = new HttpProxyBuilder(this, host, port)

	def addProxies(httpProxy: ProxyServer, httpsProxy: Option[ProxyServer]) = copy(protocol = protocol.copy(proxy = Some(httpProxy), securedProxy = httpsProxy))

	def localAddress(localAddress: InetAddress) = copy(protocol = protocol.copy(localAddress = Some(localAddress)))

	def check(checks: HttpCheck*) = copy(protocol = protocol.copy(checks = protocol.checks ::: checks.toList))

	def fetchHtmlResources = copy(protocol = protocol.copy(fetchHtmlResources = true))

	def maxConnectionsPerHostLikeFirefoxOld = maxConnectionsPerHost(2)
	def maxConnectionsPerHostLikeFirefox = maxConnectionsPerHost(6)
	def maxConnectionsPerHostLikeOperaOld = maxConnectionsPerHost(4)
	def maxConnectionsPerHostLikeOpera = maxConnectionsPerHost(6)
	def maxConnectionsPerHostLikeSafariOld = maxConnectionsPerHost(4)
	def maxConnectionsPerHostLikeSafari = maxConnectionsPerHost(6)
	def maxConnectionsPerHostLikeIE7 = maxConnectionsPerHost(2)
	def maxConnectionsPerHostLikeIE8 = maxConnectionsPerHost(6)
	def maxConnectionsPerHostLikeIE10 = maxConnectionsPerHost(8)
	def maxConnectionsPerHostLikeChrome = maxConnectionsPerHost(6)

	def maxConnectionsPerHost(max: Int) = copy(protocol = protocol.copy(maxConnectionsPerHost = max))

	def build = {
		require(!(!protocol.shareClient && protocol.shareConnections), "Invalid protocol configuration: can't stop sharing the HTTP client while still sharing connections!")
		protocol
	}
}
