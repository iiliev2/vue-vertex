# Vue-Vertex

Vue-Vertex is a simple web application, a PoC to illustrate the integration of two innovative frameworks - a high-performance, scalable back-end and a modern front-end.
A Vert.x web server exposes a simple REST API, which is fetching data from a MongoDB database. It is also serving a Vue.js-built SPA, which is using REST calls to interface back with the server.
The various modules of the application are packaged in a single executable fat jar using maven.

To build the application run mvn clean, mvn install in the parent project. After that you can run the jar using the start script in the tools porject.

You can run the webpack-dev-server throgh the included batch script run-webpack-dev-server.bat.
