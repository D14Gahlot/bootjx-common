# MeherY


## Deployment
### POSTMAN
```
mvn package -pl ms-postman -am -DskipTests
// 
java -jar ms-postman/target/ms-postman-0.0.1-SNAPSHOT.jar
```
### AGENT
```
mvn package -pl server-agent -am -DskipTests
// 
java -jar server-agent/target/server-agent-0.0.1-SNAPSHOT.jar
```
### BOT
```
mvn package -pl ms-bot -am -DskipTests
// 
java -jar ms-bot/target/ms-bot-0.0.1-SNAPSHOT.jar
```