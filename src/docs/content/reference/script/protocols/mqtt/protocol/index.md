---
title: MQTT Protocol
seotitle: Gatling MQTT protocol reference - protocol configuration
description: How to use the MQTT support in Gatling to connect to a broker and perform checks against inbound messages.
lead: How to use the MQTT support in Gatling to connect to a broker and perform checks against inbound messages.
date: 2021-04-20T18:30:56+02:00
lastmod: 2022-12-14T21:30:56+02:00
---

{{< alert warning >}}
The MQTT protocol is not supported by Gatling JS.
{{< /alert >}}

{{< alert info >}}
It only supports MQTT 3.1 and 3.1.1. More recent versions are not currently supported.
{{< /alert >}}

## Prerequisites

Gatling Enterprise MQTT DSL is not imported by default.

You have to manually add the following imports:

{{< include-code "imports" java kt scala >}}

## MQTT Protocol

Use the `mqtt` object in order to create a MQTT protocol.

{{< include-code "protocol-sample" java kt scala >}}

## Request

Use the `mqtt("requestName")` method in order to create a MQTT request.

### `connect`

Your virtual users first have to establish a connection.

{{< include-code "connect" java kt scala >}}

### `subscribe`

Use the `subscribe` method to subscribe to an MQTT topic:

{{< include-code "subscribe" java kt scala >}}

### `publish`

Use the `publish` method to publish a message. You can use the same `Body` API as for HTTP request bodies:

{{< include-code "publish" java kt scala >}}

## MQTT Checks

You can define blocking checks with `await` and non-blocking checks with `expect`.
Those can be set right after subscribing, or after publishing:

{{< include-code "check" java kt scala >}}

You can optionally define in which topic the expected message will be received:

You can optionally define check criteria to be applied on the matching received message:

You can use `waitForMessages` and block for all pending non-blocking checks:

{{< include-code "waitForMessages" java kt scala >}}

## MQTT configuration

MQTT support honors the ssl and netty configurations from `gatling.conf`.

## Example

{{< include-code "example" java kt scala >}}
