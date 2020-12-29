package com.boot.jx.cache;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.def.ATransactionModel;
import com.boot.jx.def.ICacheBox;

/**
 * @deprecated use {@link com.boot.jx.cache.TxCacheBox}
 * @author lalittanwar
 *
 * @param <T>
 */
@Deprecated
public abstract class TransactionModel<T> extends ATransactionModel<T> {

	@Autowired(required = false)
	RedissonClient redisson;

	CacheBox<T> cache;

	@Override
	public ICacheBox<T> getCacheBox() {
		if (cache == null) {
			this.cache = new CacheBox<T>("txm-" + this.getClazzName());
			this.cache.setClient(redisson);
		}
		return this.cache;
	}

	@Override
	public T init() {
		return save(getDefault());
	};

	@Override
	public T commit() {
		return null;
	};

}
