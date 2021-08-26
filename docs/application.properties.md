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

###### CherryBase ########
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
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/cherrybase
spring.datasource.username=cherrybase
spring.datasource.password=cherrybase

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### CherryBase ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot
mry.admin.url=http://localhost:8081/admin


aws.s3.b1.bucket=
aws.s3.b1.accessKey=
aws.s3.b1.secretKey=
aws.s3.b1.region=eu-west-2


aws.s3.b2.bucket=
aws.s3.b2.accessKey=
aws.s3.b2.secretKey=
aws.s3.b2.region=eu-west-2

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

###### CherryBase ########
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
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/cherrybase
spring.datasource.username=cherrybase
spring.datasource.password=cherrybase

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### cherrybase ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot

aws.s3.b1.bucket=
aws.s3.b1.accessKey=
aws.s3.b1.secretKey=
aws.s3.b1.region=eu-west-2


aws.s3.b2.bucket=
aws.s3.b2.accessKey=
aws.s3.b2.secretKey=
aws.s3.b2.region=eu-west-2

```

## XMS
###### application.properties
```application.properties
###### App Identification  ########
app.env=DEMO 					# optional Enviroment
app.group=1  					# optional group of services
app.id=XMS1						# optional instance name

###### Appplication ########
server.port=8085				# optional Port

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### cherrybase ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot
mry.admin.url=http://localhost:8081/admin

```


## ACCOUNT
###### application.properties
```application.properties
###### App Identification  ########
app.env=DEMO 					# optional Enviroment
app.group=1  					# optional group of services
app.id=ACC1						# optional instance name

###### Appplication ########
server.port=8086				# optional Port

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://localbot:localbot@10.28.42.30:27017/localbot

###### DB Redis ########
spring.redis.host=localhost
spring.redis.port=6379
server.connection-timeout=-1

###### cherrybase ########
mry.postman.url=http://localhost:8082/postman
mry.agent.url=http://localhost:8083/agent
mry.bot.url=http://localhost:8084/bot
mry.admin.url=http://localhost:8081/admin
mry.xms.url=http://localhost:8085/xms

###### Comma Sperated Emails allowed to login as Admin ########
mry.superadmin.emails=cherrybase786@gmail.com,xyz@gmail.com
```


## TenantProperties for All Services
###### application.&lt;tnt&gt;.properties
```application.tnt.properties
###### DB mysql ########
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/<tnt>
spring.datasource.username=<tnt>
spring.datasource.password=<tnt>

###### DB Mongo ########
spring.data.mongodb.uri=mongodb://<tnt>:<tnt>@10.28.42.30:27017/<tnt>
spring.data.mongodb.repositories.enabled=true
```
