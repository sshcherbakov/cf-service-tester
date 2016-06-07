The purpose
-----------

The cf-service-tester test tool Java application is developed to 
test the availalbility of the [Redis](http://redis.io) and [RabbitMQ](http://rabbitmq.com) services on a
[Cloud Foundry](http://cloudfoundry.org) and [Pivotal Web Services](https://run.pivotal.io/) platform.

This is a [Spring Boot](http://projects.spring.io/spring-boot/) Java application that can also be used 
in standalone mode and connect to the local Redis and RabbitMQ services. 

In the standalone mode the tool can be
used to verify the message delivery via RabbitMQ message broker in different failure mode scenarios.

How to build
------------
After the you have cloned the project locally just run the Gradle `build` task
````
	./gradlew build
````

Running in standalone mode
-----------------------------
After the application is successfully built it can be started from a single application ["fat jar"](http://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html) file from the command line:
````
	java -jar build/libs/cf-service-tester-0.0.1-RELEASE.jar
````
The configuration properties including the hostnames for the Redis and RabbitMQ services will be taken automatically
from the [`VCAP_SERVICES`](http://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html) environment variable for
the cloud deployment of from the [application.properties](src/main/resources/application.properties) files packaged 
into the application JAR for the standalone version.

You can override numerous application configuration settings passing new values on the command line during startup.
For example, to run the tool in standalone mode with 

* web UI listening on port 8090
* application instance name set to `two`
* with no publishers activated
* one single-threaded RabbitMQ queue listener

````
java -Dserver.port=8090 -Drabbit.addresses=vf1:5672 -Dvcap.application.instance_id=two -Drabbit.publishers=0 -Drabbit.consumer.instances=1 -Drabbit.concurrent.consumers=1 -jar build/libs/cf-service-tester-0.0.1-RELEASE.jar
````

Running in Cloud Foundry
------------------------
After logging in to your Cloud Foundry account in the command line console using the [cf-cli](https://github.com/cloudfoundry/cli)
````
cf create-service p-rabbitmq standard myrabbit
cf create-service p-redis shared-vm myredis
cf create-service p-mysql 512mb testmysql
cf push cst -p build/libs/cf-service-tester-0.0.1-RELEASE.jar --no-start
cf bind-service cst myrabbit
cf bind-service cst myredis
cf bind-service cst testmysql
cf start cst
````
will start the application in the cloud and bind it automatically to the Redis and RabbitMQ instances

WebUI interface
---------------
After the application is started its root page at for example `http://localhost:8080` or `http://cst.cfapps.io` for the PWS will show two 
icons for Redis and RabbitMQ services indicating their current status:

* `UP` service is running
* `DOWN` service is unavailable (e.g. not bound to the application instance in the cloud)

