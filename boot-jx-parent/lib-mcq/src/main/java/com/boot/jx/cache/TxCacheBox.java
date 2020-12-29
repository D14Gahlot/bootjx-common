package com.boot.jx.cache;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.def.ATxCacheBox;
import com.boot.jx.def.ICacheBox;

public abstract class TxCacheBox<T> extends ATxCacheBox<T> {

	@Autowired(required = false)
	RedissonClient redisson;

	CacheBox<T> cache;

	@Override
	public ICacheBox<T> getCacheBox() {
		if (cache == null) {
			this.cache = new CacheBox<T>("txcb-" + this.getClazzName());
			this.cache.setClient(redisson);
		}
		return this.cache;
	}

}
