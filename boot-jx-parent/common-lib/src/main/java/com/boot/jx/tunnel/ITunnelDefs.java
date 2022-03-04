package com.boot.jx.tunnel;

import java.io.Serializable;
import java.util.Map;
import java.util.Queue;

import com.boot.model.MapModel;

public class ITunnelDefs {
    public interface TunnelQueue<T> extends Queue<T> {
    }

    public interface ITaskLimiter {
	Map<String, Object> getStats();

	String getName();

	void doTask(int pollQNum, int pushQNum, int batchSize);
    }

    public static class TunnelTask implements ITunnelEvent {
	private static final long serialVersionUID = 7707873093764252510L;
	private String name;
	private String id;
	private long interval;

	private Map<String, Object> data;

	public Map<String, Object> getData() {
	    return data;
	}

	public void setData(Map<String, Object> data) {
	    this.data = data;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}

	public long getInterval() {
	    return interval;
	}

	public void setInterval(long interval) {
	    this.interval = interval;
	}

	public TunnelTask interval(long interval) {
	    this.setInterval(interval);
	    return this;
	}

	public TunnelTask id(String id) {
	    this.setId(id);
	    return this;
	}

	public TunnelTask name(String name) {
	    this.setName(name);
	    return this;
	}

	public TunnelTask data(MapModel data) {
	    this.data = data.map();
	    return this;
	}

	public MapModel data() {
	    MapModel x = MapModel.from(this.data);
	    if (this.data == null) {
		this.data = x.map();
	    }
	    return x;
	}

    }

    public static class TaskInfo implements Serializable {
	private static final long serialVersionUID = -8230113556466236531L;
	long timestamp;
	long interval;
	String key;

	public long getTimestamp() {
	    return timestamp;
	}

	public void setTimestamp(long timestamp) {
	    this.timestamp = timestamp;
	}

	public long getInterval() {
	    return interval;
	}

	public void setInterval(long interval) {
	    this.interval = interval;
	}

	public String getKey() {
	    return key;
	}

	public void setKey(String key) {
	    this.key = key;
	}

    }
}
