start "persistence" java -jar ../vw-users-persistence/target/vw-users-persistence-1.0-SNAPSHOT.jar
start "api" java -jar ../vw-users-restapi/target/vw-users-restapi-2.0-SNAPSHOT.jar
start "server" java -jar ../vw-web/target/vw-web-2.0-SNAPSHOT.jar