## TunnelService
There different types of events, which can be triggered and acted upon.

#### Task
Task is an event which is can be executed only once, globally in whole enviroment.
#### Trigger/Handle by TaskClass
```java
// Trigger/Publish
GeoNotifyTask task = new GeoNotifyTask();
task.setCustomerId(customerId);
task.setGeoPoint(ArgUtil.parseAsString(hotpoint));
task.setAppTitle(webAppConfig.getAppTitle());
tunnelService.task(task);

//Handler/Subscriber
@TunnelEventMapping(byEvent = GeoNotifyTask.class, scheme = TunnelEventXchange.TASK_WORKER)
public class GeoNotifyTaskWorker implements ITunnelSubscriber<GeoNotifyTask> {
	@Override
	public void onMessage(String channel, GeoNotifyTask task) {
	    // Handle Event
	}
}
```
#### Trigger/Handle by Event Name 
When using this we can send any class.
```java
// Push to Message Queue
DBEvent event = new DBEvent();
event.setData(new HashMap<String,Object> ());
tunnelService.task("CUSTOMER_KYC_UPDATE", event);

//Handler/Subscriber
@TunnelEventMapping(topic = AmxTunnelEvents.Names.CUSTOMER_KYC_UPDATE, scheme = TunnelEventXchange.TASK_WORKER)
public class CustomerKYCUpdateListner implements ITunnelSubscriber<DBEvent> {
	@Override
	public void onMessage(String channel, DBEvent event) {
	    // Handle Event
	}
}
```
* Though any class can be used generally *TunnelEvent* for normal events and *DBEvent* for DataBase events are recommended.
#### Task Listener
When using this we can send any class.
```java
// Push to Message Queue
DBEvent event = new DBEvent();
event.setData(new HashMap<String,Object> ());
tunnelService.task("TRNX_BENE_CREDIT", event);

//Handler/Subscriber - TASK_WORKER
@TunnelEventMapping(topic = AmxTunnelEvents.Names.TRNX_BENE_CREDIT, scheme = TunnelEventXchange.TASK_WORKER)
public class TrnaxBeneCreditListner implements ITunnelSubscriber<DBEvent> {
	@Override
	public void onMessage(String channel, DBEvent event) {
	    // ONLY one instance of this task will be excuted
	    // Handle Event
	}
}
//Handler/Subscriber - TASK_LISTNER
@TunnelEventMapping(topic = AmxTunnelEvents.Names.TRNX_BENE_CREDIT, scheme = TunnelEventXchange.TASK_LISTNER)
public class TranxViewUpdateLegacyListner implements ITunnelSubscriber<DBEvent> {
	@Override
	public void onMessage(String channel, DBEvent event) {
	    // All the instances of this task will be excuted
	    // Handle Event
	}
}
```