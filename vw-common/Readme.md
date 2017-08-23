This module holds all common facilities and functionality, needed to build a distributed system.

The application uses slf4j and log4j2 for logging. log4j2 has a built in shutdown hook which closes all loggers on system exit.
The Vertx Launcher also provides an automatic resource cleanup using a shutdown hook. The custom VertxShutdownCallbackRegistry is therefore provided, in order to be able to control the order of hooks execution.
All log4j2 hooks will be redirected to the DistributedLauncher, which will call them after vertx has been closed.

In order for the haselcast cluster manager to detect nodes on your network, make sure to change the cluster.host in config.json to the ip of your network adapter.