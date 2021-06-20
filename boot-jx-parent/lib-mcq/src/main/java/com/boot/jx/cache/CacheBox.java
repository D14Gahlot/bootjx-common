package com.boot.jx.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.ReconnectionStrategy;
import org.redisson.api.LocalCachedMapOptions.SyncStrategy;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.thavam.util.concurrent.blockingMap.BlockingHashMap;

import com.boot.jx.AppConfig;
import com.boot.jx.AppParam;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.cache.MCQStatus.MCQStatusCodes;
import com.boot.jx.cache.MCQStatus.MCQStatusError;
import com.boot.jx.def.ICacheBox;
import com.boot.jx.logger.LoggerService;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;
import com.boot.utils.JsonUtil;

public class CacheBox<T> implements ICacheBox<T> {

	private Logger LOGGER = LoggerService.getLogger(CacheBox.class);

	LocalCachedMapOptions<String, T> localCacheOptions = LocalCachedMapOptions.<String, T>defaults()
			.evictionPolicy(EvictionPolicy.NONE).cacheSize(1000).reconnectionStrategy(ReconnectionStrategy.NONE)
			.syncStrategy(SyncStrategy.INVALIDATE).timeToLive(10000).maxIdle(10000);

	@Autowired(required = false)
	RedissonClient redisson;

	@Autowired
	AppConfig appConfig;

	public void setClient(RedissonClient redisson) {
		this.redisson = redisson;
	}

	private RLocalCachedMap<String, T> cache = null;
	private BlockingHashMap<String, T> locker = null;

	public RLocalCachedMap<String, T> map() {
		if (redisson != null) {
			if (locker == null) {
				locker = new BlockingHashMap<String, T>();
			}
			String localCacheName = String.format("%s-%s-%s.%s", AppParam.APP_ENV.getValue(),
					(ArgUtil.isEmpty(getCahceName()) ? getClazzName() : getCahceName()),
					CacheRedisConfiguration.CODEC_VERSION, version());
			if (cache == null) {
				cache = redisson.getLocalCachedMap(localCacheName, localCacheOptions);
			}
			return cache;
		}
		return null;
	}

	String clazzName = null;

	public String getClazzName() {
		if (this.clazzName == null) {
			this.clazzName = ClazzUtil.getClassName(this);
		}
		return clazzName;
	}

	String cahceName = null;

	public String getCahceName() {
		return cahceName;
	}

	public void setCahceName(String cahceName) {
		this.cahceName = cahceName;
	}

	protected CacheBox(String name) {
		this.cahceName = name;
	}

	protected CacheBox() {

	}

	@Override
	public T put(String key, T value) {
		try {
			return this.map().put(key, value);
		} catch (Exception e) {
			LOGGER.error("REDIS_SAVE_EXCEPTION KEY:" + key + " = " + JsonUtil.toJson(value), e);
			throw new MCQStatusError(MCQStatusCodes.DATA_SAVE_ERROR, "REDIS_SAVE_EXCEPTION KEY:" + key);
		}
	}

	@Override
	public T get(String key) {
		try {
			return this.map().get(key);
		} catch (Exception e) {

			LOGGER.error("REDIS_READ_EXCEPTION KEY:" + key + " from " + cache.getName(), e);
			ApiFieldError w = new ApiFieldError();
			w.code(MCQStatusCodes.DATA_READ_ERROR);
			w.setDescription("REDIS_READ_EXCEPTION KEY");
			w.setField(key);
			ApiResponseUtil.addWarning(w);
			// throw new MCQStatusError(MCQStatusCodes.DATA_READ_ERROR,
			// "REDIS_READ_EXCEPTION KEY:" + key);
			return null;
		}
	}

	@Override
	public T putIfAbsent(String key, T value) {
		try {
			return this.map().putIfAbsent(key, value);
		} catch (Exception e) {
			LOGGER.error("REDIS_SAVE_EXCEPTION KEY:" + key + " = " + JsonUtil.toJson(value), e);
			throw new MCQStatusError(MCQStatusCodes.DATA_SAVE_ERROR, "REDIS_SAVE_EXCEPTION KEY:" + key);
		}
	}

	@Override
	public T remove(String key) {
		try {
			return this.map().remove(key);
		} catch (Exception e) {
			LOGGER.error("REDIS_REMOVE_EXCEPTION KEY:" + key, e);
			ApiFieldError w = new ApiFieldError();
			w.code(MCQStatusCodes.DATA_REMOVE_ERROR);
			w.setDescription("REDIS_REMOVE_EXCEPTION KEY");
			w.setField(key);
			ApiResponseUtil.addWarning(w);
			return null;
		}
	}

	@Override
	public T replace(String key, T value) {
		return this.map().replace(key, value);
	}

	@Override
	public boolean replace(String key, T oldValue, T newValue) {
		return this.map().replace(key, oldValue, newValue);
	}

	@Override
	public boolean remove(String key, Object value) {
		return this.map().remove(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends T> map) {
		this.map().putAll(map);
	}

	@Override
	public Map<String, T> getAll(Set<String> keys) {
		return this.map().getAll(keys);
	}

	@Override
	public long fastRemove(String... keys) {
		return this.map().fastRemove(keys);
	}

	@Override
	public boolean fastPut(String key, T value) {
		try {
			return this.map().fastPut(key, value);
		} catch (Exception e) {
			LOGGER.error("REDIS_FAST_SAVE_EXCEPTION KEY:" + key + " = " + JsonUtil.toJson(value), e);
			throw new MCQStatusError(MCQStatusCodes.DATA_SAVE_ERROR, "REDIS_SAVE_EXCEPTION KEY:" + key);
		}
	}

	@Override
	public boolean fastPutIfAbsent(String key, T value) {
		return this.map().fastPutIfAbsent(key, value);
	}

	@Override
	public T getOrDefault(String key, T defaultValue) {
		try {
			return this.map().getOrDefault(key, defaultValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public T take(String key, long timeout, TimeUnit unit) throws InterruptedException {
		T item = this.map().get(key);
		long waiting = unit.toSeconds(timeout);
		while (item == null && waiting > 0) {
			item = locker.take(key, 1, unit);
			item = this.map().get(key);
			waiting--;
		}
		return item;
	}

	@Override
	public T getOrDefault(String key) {
		return getOrDefault(key, getDefault());
	}

	@Override
	public T getDefault() {
		return null;
	}

	public Object version() {
		return 0;
	}

	public static <CB> CacheBox<CB> getInstance(String name, RedissonClient redisson) {
		CacheBox<CB> x = new CacheBox<CB>(name);
		x.setClient(redisson);
		return x;
	}

}
