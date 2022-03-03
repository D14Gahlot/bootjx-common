package com.boot.jx.tunnel;

import java.util.Map;
import java.util.Queue;

public class ITunnelDefs {
    public interface TunnelQueue<T> extends Queue<T> {
    }

    public interface ITunnelEventLimiter {
	Map<String, Object> getStats();

	String getName();
    }
}
