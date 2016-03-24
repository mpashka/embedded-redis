### 1.1.0

 * Remove support for RedisServer(File, int) constructor
 * Remove redis binaries from distributed jar library
 * Improve PortProvider interface and implementation
 * RedisCluster.Builder can now use a PortProvider instead of a collection of port

### 1.0.3

 * Deprecates use of `builder()` methods, use `Builder` class directly

### 1.0.1 -> 1.0.2
 * Miscellaneous fixes to get this lib on jCenter 

### 1.0
 * Rename RedisCluster into SentinelCluster
 * Introduce a real redis cluster support (thx to @3Dragan for the initial work)
 * Make all Builder inner classes
 * Possibility to provide your own OutputStream for logs (instead of System.out)

### 0.6
 * Support JDK 6 +

### 0.5
 * OS detection fix
 * redis binary per OS/arch pair
 * Updated to 2.8.19 binary for Windows

### 0.4 
 * Updated for Java 8
 * Added Sentinel support
 * Ability to create arbitrary clusters on arbitrary (ephemeral) ports
 * Updated to latest guava 
 * Throw an exception if redis has not been started
 * Redis errorStream logged to System.out

### 0.3
 * Fluent API for RedisServer creation

### 0.2
 * Initial decent release
