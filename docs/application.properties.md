# Application Properties

## Postman
###### application.properties
```application.properties
###### App Identification  ########
app.env=DEMO 					# optional Enviroment
app.group=1  					# optional group of services
app.id=PM1						# optional instance name

###### Appplication ########
server.port=8082				# optional Port

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### MeherY ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot
mry.admin.url=http://localhost:8081/admin

```


## Agent
###### application.properties
```application.properties
###### App Identification  ########
app.env=DEMO 					# optional Enviroment
app.group=1  					# optional group of services
app.id=AG1						# optional instance name

###### Appplication ########
server.port=8083

###### DB mysql ########
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/meherydemo
spring.datasource.username=meherydemo
spring.datasource.password=meherydemo

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### MeherY ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot
mry.admin.url=http://localhost:8081/admin

```

## BOT
###### application.properties
```application.properties
###### App Identification  ########
app.env=DEMO 					# optional Enviroment
app.group=1  					# optional group of services
app.id=BT1						# optional instance name

###### Appplication ########
server.port=8084

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### MeherY ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot
mry.admin.url=http://localhost:8081/admin
```

## Admin
###### application.properties
```application.properties
###### App Identification  ########
app.env=DEMO 					# optional Enviroment
app.group=1  					# optional group of services
app.id=AD1						# optional instance name

###### Appplication ########
server.port=8081

###### DB mysql ########
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/meherydemo
spring.datasource.username=meherydemo
spring.datasource.password=meherydemo

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### MeherY ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot

```



## TenantProperties 
###### application.aertrip.properties
```application.aertrip.properties
###### DB mysql ########
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/aertrip
spring.datasource.username=aertrip
spring.datasource.password=aertrip

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://aertrip:aertrip@10.28.42.30:27017/aertrip
spring.data.mongodb.repositories.enabled=true
```
