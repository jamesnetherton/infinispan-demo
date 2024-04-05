Reproducer project for https://github.com/apache/camel-quarkus/issues/5965.

## Prerequisites

1. Start an Infinispan container.

```
docker run -it -p 11222:11222 -e USER="admin" -e PASS="password" quay.io/infinispan/server:14.0
```

> Note: if your docker host is not running on `localhost`, then edit `camel.component.infinispan.hosts` in `application.properties`.

2. Browse to http://localhost:11222 log in with `admin` / `password`. Then create a new cache named `test`

## Build & run the working application

Run `mvn clean package`, then `java -jar target/quarkus-app/quarkus-run.jar`.

Every 10 seconds you should see a log message indicating Camel is successfully invoking a cache put operation.

```
2024-04-05 09:28:05,005 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Routes startup (started:1)
2024-04-05 09:28:05,006 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started route1 (timer://cachePut)
2024-04-05 09:28:05,006 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 4.4.1 (camel-1) started in 170ms (build:0ms init:0ms start:170ms)
2024-04-05 09:28:05,007 INFO  [io.quarkus] (main) infinispan-demo 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.8.3) started in 0.498s.
2024-04-05 09:28:05,007 INFO  [io.quarkus] (main) Profile prod activated.
2024-04-05 09:28:05,007 INFO  [io.quarkus] (main) Installed features: [camel-core, camel-infinispan, camel-timer, cdi, infinispan-client, smallrye-context-propagation]
2024-04-05 09:28:06,047 INFO  [route1] (Camel (camel-1) thread #1 - timer://cachePut) Cache PUT : foo = bar
```

## Build & run the failing application

Edit `application.properties` and set `quarkus.infinispan-client.devservices.enabled` to `true`.

Rebuild and run the application with `mvn clean package`, then `java -jar target/quarkus-app/quarkus-run.jar`. Observe the failure.

```
jakarta.enterprise.inject.CreationException: Null contextual instance was produced by a normal scoped synthetic bean: SYNTHETIC bean [class=org.infinispan.client.hotrod.RemoteCacheManager, id=3e5ozT9gyWKQ9VvCN1iNcOXJaqs]
	at org.infinispan.client.hotrod.RemoteCacheManager_3e5ozT9gyWKQ9VvCN1iNcOXJaqs_Synthetic_Bean.doCreate(Unknown Source)
	at org.infinispan.client.hotrod.RemoteCacheManager_3e5ozT9gyWKQ9VvCN1iNcOXJaqs_Synthetic_Bean.create(Unknown Source)
	at org.infinispan.client.hotrod.RemoteCacheManager_3e5ozT9gyWKQ9VvCN1iNcOXJaqs_Synthetic_Bean.create(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.createInstanceHandle(AbstractSharedContext.java:119)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:38)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.c3(Unknown Source)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.computeIfAbsent(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.impl.ClientProxies.getApplicationScopedDelegate(ClientProxies.java:21)
	at org.infinispan.client.hotrod.RemoteCacheManager_3e5ozT9gyWKQ9VvCN1iNcOXJaqs_Synthetic_ClientProxy.arc$delegate(Unknown Source)
	at org.infinispan.client.hotrod.RemoteCacheManager_3e5ozT9gyWKQ9VvCN1iNcOXJaqs_Synthetic_ClientProxy.getCache(Unknown Source)
	at org.apache.camel.component.infinispan.remote.InfinispanRemoteManager.getCache(InfinispanRemoteManager.java:162)
	at org.apache.camel.component.infinispan.InfinispanManager.getCache(InfinispanManager.java:36)
	at org.apache.camel.component.infinispan.InfinispanProducer.getCache(InfinispanProducer.java:412)
	at org.apache.camel.component.infinispan.InfinispanProducer.onPut(InfinispanProducer.java:62)
	at org.apache.camel.component.infinispan.InfinispanProducerInvokeOnHeaderFactory.invoke(InfinispanProducerInvokeOnHeaderFactory.java:36)
	at org.apache.camel.support.HeaderSelectorProducer.process(HeaderSelectorProducer.java:142)
	at org.apache.camel.processor.SendProcessor.process(SendProcessor.java:210)
	at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.handleFirst(RedeliveryErrorHandler.java:462)
	at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.run(RedeliveryErrorHandler.java:438)
	at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.doRun(DefaultReactiveExecutor.java:199)
	at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.executeReactiveWork(DefaultReactiveExecutor.java:189)
	at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.tryExecuteReactiveWork(DefaultReactiveExecutor.java:166)
	at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.schedule(DefaultReactiveExecutor.java:148)
	at org.apache.camel.impl.engine.DefaultReactiveExecutor.scheduleMain(DefaultReactiveExecutor.java:59)
	at org.apache.camel.processor.Pipeline.process(Pipeline.java:163)
	at org.apache.camel.impl.engine.CamelInternalProcessor.processNonTransacted(CamelInternalProcessor.java:354)
	at org.apache.camel.impl.engine.CamelInternalProcessor.process(CamelInternalProcessor.java:330)
	at org.apache.camel.component.timer.TimerConsumer.sendTimerExchange(TimerConsumer.java:293)
	at org.apache.camel.component.timer.TimerConsumer$1.doRun(TimerConsumer.java:164)
	at org.apache.camel.component.timer.TimerConsumer$1.run(TimerConsumer.java:136)
	at java.base/java.util.TimerThread.mainLoop(Timer.java:566)
	at java.base/java.util.TimerThread.run(Timer.java:516)
```


