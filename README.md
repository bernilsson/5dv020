Building
========

[Install sbt] [install-sbt] and then run the following command:

    $ sbt
    > compile

Running
=======

From the sbt prompt:

    > nameserver/run
    > client/run

Running tests:

    > gcom/test

Code layout
===========

In general, source lives under `component/src` and tests are under
`component/test`.

Nameserver code is under `nameserver`. Debugging client code lives under
`client`. The `gcom` directory houses the GCom middleware source code.

The `gcom` directory is further subdived into the following parts:

* `common` - Common classes and utility functions.
* `transport` - The transport layer.
* `communication` - Communication layer.
* `ordering` - Message ordering layer.
* `consensus` - Consensus algorithm implementation.
* `group` - Group management layer.

The layers are ordered hierarchically, where each layer depends only on the
layers beneath. For example, `common` does not depend on any other layer and
`communication` depends only on `transport` and `common`.

The `gcom` component is independent, and the `nameserver` and `client`
components are implemented in terms of `gcom`.

[install-sbt]: http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt
