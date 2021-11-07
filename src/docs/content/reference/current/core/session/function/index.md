---
title: "Functions"
description: "Passing functions for computing dynamic values"
lead: "Use functions to programmatically generate dynamic parameters"
date: 2021-04-20T18:30:56+02:00
lastmod: 2021-04-20T18:30:56+02:00
weight: 003053
---

Sometimes, you might want to dynamic parameters that are too complex to compute for Gatling EL.
Most Gatling DSL methods can also be passed a function to compute your parameter value programmatically.

## Syntax

Those functions always take a `Session` parameter, so you can extract previously stored data.

The generic signature of these functions is:

* In Java and Kotlin: `Session -> T`
* In Scala: `Expression[T]` is an alias for `Session => Validation[T]`. Values can implicitly lifted in `Validation`.

{{< include-code "function" java kt scala >}}

{{< alert warning >}}
(Scala Only): For more information about `Validation`, please check out the [Validation reference]({{< ref "../validation" >}}).
{{< /alert >}}
