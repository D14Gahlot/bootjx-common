# MeherY


## Deployment
### POSTMAN
```
// One time clone
git clone git@github.com:mehery-soccom/apache-open-nlp.git ext-resources/apache-open-nlp

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

### ADMIN
```
mvn package -pl server-admin -am -DskipTests
// 
java -jar server-admin/target/server-admin-0.0.1-SNAPSHOT.jar
```

### XMS
```
mvn package -pl server-xms -am -DskipTests
// 
java -jar server-xms/target/server-xms-0.0.1-SNAPSHOT.jar
```


## URL mapping

### API
* https://api.mehery.com/postman => 127.0.0.1:8082/postman
* https://api.mehery.com/agent => 127.0.0.1:8083/agent
* https://api.mehery.com/bot => 127.0.0.1:8084/bot
* https://api.mehery.com/xms => 127.0.0.1:8084/xms

### SERVER
* https://app.mehery.com/agent => 127.0.0.1:8083/agent
* https://app.mehery.com/admin => 127.0.0.1:8081/admin
* https://app.mehery.com/xms => 127.0.0.1:8081/xms

### GUIDE
[Application Properties](docs/application.properties.md)