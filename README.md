play-tepkin-mongo-example
==================

A starter application with Play Framework 2.5 and Tepkin a Reactive MongoDB Driver for Scala built on top of Akka IO and Akka Streams

Using a driver built on top of Akka Streams allows for a very smooth integration with the new streaming API of Play framework 2.5

More information about the tepkin driver in its own github project
https://github.com/jeroenr/tepkin


# Requirements
- MongoDB
- SBT

# Configure
Review conf/application.conf and check mongo.url a new database 'tepkin' will be created with a collection 'projects'


#Run
```
sbt run
```

Open http://localhost:9000/assets/websocketsample.html to insert projects in a non blocking streaming fashion (with backpressure) using a websocket connection

Open http://localhost:9000/ to list the projects

Or run to create a project with your preferred name
```
curl -X "PUT" http://localhost:9000/projects/my-project
```

Have fun.