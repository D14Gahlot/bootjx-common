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

## URL mapping

### API
* https://api.mehery.com/postman => 127.0.0.1:8082/postman
* https://api.mehery.com/agent => 127.0.0.1:8083/agent
* https://api.mehery.com/bot => 127.0.0.1:8084/bot

### SERVER
* https://demo.mehery.com/agent => 127.0.0.1:8083/agent
* https://demo.mehery.com/admin => 127.0.0.1:8081/admin

### GUIDE
[Application Properties](docs/application.properties.md)