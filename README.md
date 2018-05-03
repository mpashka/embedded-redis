embedded-redis 
==============

Redis embedded server for Java integration testing

_This is a fork of https://github.com/fmonniot/embedded-redis_

_Used Redis v.3.2.1_ 

_Features:_
* _Windows support_


Usage
==============

Running RedisServer is as simple as:
```java
RedisServer redisServer = new RedisServer(6379);
redisServer.start();
// do some work
redisServer.stop();
```

This works as long as you have a `redis-server-3.2.1-*` executables in your path (see
[redis/embedded/RedisExecProvider.java](https://github.com/fmonniot/embedded-redis/blob/master/src/main/java/redis/embedded/RedisExecProvider.java#L26)
for the complete list of default binaries paths).

You can also provide RedisServer with your own executable:
```java
// Give an OS-independent matrix
RedisExecProvider provider = RedisExecProvider.defaultProvider()
  .override(OS.UNIX, "/path/to/unix/redis")
  .override(OS.WINDOWS, Architecture.x86, "/path/to/windows/redis")
  .override(OS.Windows, Architecture.x86_64, "/path/to/windows/redis")
  .override(OS.MAC_OS_X, Architecture.x86, "/path/to/macosx/redis")
  .override(OS.MAC_OS_X, Architecture.x86_64, "/path/to/macosx/redis")
  
RedisServer redisServer = new RedisServer(provider, 6379);
```

You can also use fluent API to create RedisServer:
```java
RedisServer redisServer = new RedisServer.Builder()
  .redisExecProvider(customRedisProvider)
  .port(6379)
  .slaveOf("locahost", 6378)
  .configFile("/path/to/your/redis.conf")
  .build();
```

Or even create the redis.conf file from the builder:
```java
RedisServer redisServer = new RedisServer.Builder()
  .redisExecProvider(customRedisProvider)
  .port(6379)
  .slaveOf("locahost", 6378)
  .setting("daemonize no")
  .setting("appendonly no")
  .build();
```

The `JedisUtil` class contains utility methods to get the list of port in a Jedis friendly format.

## Setting up a cluster

Embedded Redis has support for [Redis Cluster](http://redis.io/topics/cluster-tutorial).

```java
Redis cluster = new RedisCluster.Builder()
        .withServerBuilder(myOwnRedisServerBuilder)
        .serverPorts(Arrays.asList(42000,42001,42002,42003,42004,42005))
        .numOfReplicates(1)
        .numOfRetries(42)
        .build()

cluster.start()
cluster.stop()
```

## Setting up a sentinel cluster

Embedded Redis has support for HA Redis clusters with Sentinels and master-slave replication

#### Using ephemeral ports

A simple Redis cluster on ephemeral ports, with setup similar to that from production, would look like this:


```java
//creates a cluster with 3 sentinels, quorum size of 2 and 3 replication groups, each with one master and one slave
cluster = new SentinelCluster.Builder()
                .ephemeral()
                .sentinelCount(3)
                .quorumSize(2)
                .replicationGroup("master1", 1)
                .replicationGroup("master2", 1)
                .replicationGroup("master3", 1)
                .build();
cluster.start();
cluster.stop();
```

The above example starts Redis cluster on ephemeral ports, which you can later get with ```cluster.ports()```,
which will return a list of all ports of the cluster. You can also get ports of sentinels with ```cluster.sentinelPorts()```
or servers with ```cluster.serverPorts()```. 

This library also includes an utility for helping you integrating it with Jedis
 
 ```java
 //retrieve ports on which sentinels have been started, using a simple Jedis utility class
 Set<String> jedisSentinelHosts = JedisUtil.sentinelHosts(cluster);
 // testing code that requires redis running
 JedisSentinelPool pool = new JedisSentinelPool("master1", jedisSentinelHosts);
 ```

#### Using predefined ports
You can also start Redis cluster on predefined ports and even mix both approaches:

```java
final List<Integer> sentinels = Arrays.asList(26739, 26912);
final List<Integer> group1 = Arrays.asList(6667, 6668);
final List<Integer> group2 = Arrays.asList(6387, 6379);

//creates a cluster with 3 sentinels, quorum size of 2 and 3 replication groups, each with one master and one slave
cluster = new SentinelCluster.Builder()
                .sentinelPorts(sentinels)
                .quorumSize(2)
                .serverPorts(group1).replicationGroup("master1", 1)
                .serverPorts(group2).replicationGroup("master2", 1)
                .ephemeralServers().replicationGroup("master3", 1)
                .build();
cluster.start();
```

The above will create and start a cluster with sentinels on ports ```26739, 26912```, first replication group on ```6667, 6668```,
second replication group on ```6387, 6379``` and third replication group on ephemeral ports.


License
==============
Licensed under the Apache License, Version 2.0


Contributors
==============
 * François Monniot ([@fmonniot](https://github.com/fmonniot))
 * Krzysztof Styrc ([@kstyrc](https://github.com/kstyrc))
 * Piotr Turek ([@turu](https://github.com/turu))
 * anthonyu ([@anthonyu](https://github.com/anthonyu))
 * Artem Orobets ([@enisher](https://github.com/enisher))
 * Sean Simonsen ([@SeanSimonsen](https://github.com/SeanSimonsen))
 * Rob Winch ([@rwinch](https://github.com/rwinch))
 * Pavel Moukhataev ([@mpashka](https://github.com/mpashka))
