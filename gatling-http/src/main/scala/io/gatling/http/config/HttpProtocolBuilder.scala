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

import scala.collection.mutable

import com.ning.http.client.{ ProxyServer, Request, RequestBuilder }
import com.typesafe.scalalogging.slf4j.Logging

import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.result.message.Status
import io.gatling.core.session.{ Expression, Session }
import io.gatling.http.Headers.Names._
import io.gatling.http.ahc.GatlingHttpClient
import io.gatling.http.request.builder.{ GetHttpRequestBuilder, PostHttpRequestBuilder }
import io.gatling.http.response.Response
import io.gatling.http.util.HttpHelper

/**
 * HttpProtocolBuilder class companion
 */
object HttpProtocolBuilder {

	val default = new HttpProtocolBuilder(HttpProtocol.default, configuration.http.warmUpUrl)

	val warmUpUrls = mutable.Set.empty[String]
}

/**
 * Builder for HttpProtocol used in DSL
 *
 * @param protocol the protocol being built
 * @param warmUpUrl a URL to be pinged in order to warm up the HTTP engine
 */
case class HttpProtocolBuilder(protocol: HttpProtocol, warmUpUrl: Option[String]) extends Logging {

	def baseURL(baseUrl: String) = copy(protocol = protocol.copy(baseURLs = Some(List(baseUrl))))

	def baseURLs(baseUrl1: String, baseUrl2: String, baseUrls: String*) = copy(protocol = protocol.copy(baseURLs = Some(baseUrl1 :: baseUrl2 :: baseUrls.toList)))

	def disableFollowRedirect = copy(protocol = protocol.copy(followRedirectEnabled = false))

	def disableAutomaticReferer = copy(protocol = protocol.copy(automaticRefererEnabled = false))

	def disableCaching = copy(protocol = protocol.copy(cachingEnabled = false))

	def disableResponseChunksDiscarding = copy(protocol = protocol.copy(responseChunksDiscardingEnabled = false))

	def disableClientSharing = copy(protocol = protocol.copy(shareClient = false))

	def shareConnections = copy(protocol = protocol.copy(shareConnections = true))

	def acceptHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT -> value)))

	def acceptCharsetHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT_CHARSET -> value)))

	def acceptEncodingHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT_ENCODING -> value)))

	def acceptLanguageHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (ACCEPT_LANGUAGE -> value)))

	def authorizationHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (AUTHORIZATION -> value)))

	def connection(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (CONNECTION -> value)))

	def doNotTrackHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (DO_NOT_TRACK -> value)))

	def userAgentHeader(value: String) = copy(protocol = protocol.copy(baseHeaders = protocol.baseHeaders + (USER_AGENT -> value)))

	def warmUp(url: String) = copy(warmUpUrl = Some(url))

	def disableWarmUp = copy(warmUpUrl = None)

	def basicAuth(username: Expression[String], password: Expression[String]) = copy(protocol = protocol.copy(basicAuth = Some(HttpHelper.buildRealm(username, password))))

	def extraInfoExtractor(f: (Status, Session, Request, Response) => List[Any]) = copy(protocol = protocol.copy(extraInfoExtractor = Some(f)))

	def proxy(host: String, port: Int) = new HttpProxyBuilder(this, host, port)

	def addProxies(httpProxy: ProxyServer, httpsProxy: Option[ProxyServer]) = copy(protocol = protocol.copy(proxy = Some(httpProxy), securedProxy = httpsProxy))

	def build = {
		require(!(!protocol.shareClient && protocol.shareConnections), "Invalid protocoluration: can't stop sharing the HTTP client while still sharing connections!")

		warmUpUrl.map { url =>
			if (!HttpProtocolBuilder.warmUpUrls.contains(url)) {
				HttpProtocolBuilder.warmUpUrls += url
				val requestBuilder = new RequestBuilder().setUrl(url)
					.setHeader(ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.setHeader(ACCEPT_LANGUAGE, "en-US,en;q=0.5")
					.setHeader(ACCEPT_ENCODING, "gzip")
					.setHeader(CONNECTION, "keep-alive")
					.setHeader(USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

				protocol.proxy.map { proxy => if (url.startsWith("http://")) requestBuilder.setProxyServer(proxy) }
				protocol.securedProxy.map { proxy => if (url.startsWith("https://")) requestBuilder.setProxyServer(proxy) }

				try {
					GatlingHttpClient.defaultClient.executeRequest(requestBuilder.build).get
				} catch {
					case e: Exception => logger.info(s"Couldn't execute warm up request $url", e)
				}
			}
		}

		if (HttpProtocolBuilder.warmUpUrls.isEmpty) {
			GetHttpRequestBuilder.warmUp
			PostHttpRequestBuilder.warmUp
		}

		protocol
	}
}