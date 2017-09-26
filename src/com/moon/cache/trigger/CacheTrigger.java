package com.moon.cache.trigger;

/**
 * @description 缓存触发器
 *
 * @author Ranger
 * @version 2017年9月26日_下午1:56:49
 *
 */
public interface CacheTrigger {
	
	Object doTrigger(Object event);
	
}
