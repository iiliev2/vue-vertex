# Vue-Vertex 2.0

Vue-Vertex 2.0 is a PoC of a web application, built as a distributed system. Each of the three main components is deployed as an independent microservice:
-a Vert.x web server is serving a Vue.js-built SPA, which is using REST calls to perform various actions on a collection of users. The server also acts as a proxy for any calls under /api, forwarding them on the event bus.
-an event bus message consumer acts as a rest-api, and handles any forwarded requests from the web server. This component demonstrates the use of custom objects, transmitted over the event bus.
-a proxy service component to illustrate the use of the proxy generation and service discovery facilities. The DAO is exposed as a simple interface in the service discovery.

Each microservice is built as a separate executable standalone jar. When executed, they will attempt to enter a cluster, depending on configuration.

To build the application run mvn clean install in the parent project. After that you can run tools/start_system.bat to start the cluster.

You can run the webpack-dev-server though the included batch script run-webpack-dev-server.bat when developing the SPA.
