codeFormatter.xml for intellij

run_system.bat uses the run command to start all microservices in a separate window, which then can be gracefully closed with ctrl+c.

start_system.bat uses the start command to run all microservices as background processes, but then under windows they can only be killed, which prevents vertx to be gracefully closed.

stop_system.bat uses the stop command to stop all microservices which were started with the previous script.