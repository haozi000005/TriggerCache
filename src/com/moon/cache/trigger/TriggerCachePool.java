package com.moon.cache.trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description 缓存
 *
 * @author Ranger
 * @version 2017年9月26日_下午1:57:54
 *
 */
public class TriggerCachePool implements Runnable{
	
	//时间触发器单位秒
	private int timeTrigger = 10;
	//长度触发器
	private int sizeTrigger = 100;
	
	private boolean batch = false;
	
	ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	ExecutorService cachedPool = Executors.newCachedThreadPool();
	int click = timeTrigger;
	
	LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	CacheTrigger trigger = null;
	
	public TriggerCachePool(CacheTrigger trigger) {
		this.trigger = trigger;
		scheduledExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
	}
	
	public TriggerCachePool(int timeTrigger, int sizeTrigger, CacheTrigger trigger) {
		this.trigger = trigger;
		this.timeTrigger = timeTrigger;
		this.sizeTrigger = sizeTrigger;
		click = timeTrigger;
		scheduledExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
	}
	
	public TriggerCachePool(int timeTrigger, int sizeTrigger, boolean batch, CacheTrigger trigger) {
		this.trigger = trigger;
		this.timeTrigger = timeTrigger;
		this.sizeTrigger = sizeTrigger;
		this.batch = batch;
		click = timeTrigger;
		scheduledExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
	}
	
	public void addEvent(Object t) {
		queue.add(t);
		int size = size();
		if (size >= sizeTrigger) {
			doTrigger();
			click = timeTrigger;//恢复计数
		}
	}
	
	public Object pollEvent() throws InterruptedException {
		return queue.poll(100, TimeUnit.MILLISECONDS);
	}
	
	public int size() {
		return queue.size();
	}
	
	public int getTimeTrigger() {
		return timeTrigger;
	}
	public void setTimeTrigger(int timeTrigger) {
		this.timeTrigger = timeTrigger;
	}
	public int getSizeTrigger() {
		return sizeTrigger;
	}
	public void setSizeTrigger(int sizeTrigger) {
		this.sizeTrigger = sizeTrigger;
	}

	@Override
	public void run() {
		if (click == 0) {
			doTrigger();
			click = timeTrigger;
		}
		click --;
	}
	
	private void doTrigger() {
		cachedPool.submit(new Runnable() {
			public void run() {
				try {
					Object data = pollEvent();
					if (batch) {
						List<Object> dataList = new ArrayList<Object>();
						while (data != null) {
							dataList.add(data);
							// 执行队列中的下个数据
							data = pollEvent();
						}
						if (dataList.size() > 0) {
							trigger.doTrigger(dataList);
						}
					}else {
						while (data != null) {
							trigger.doTrigger(data);
							// 执行队列中的下个数据
							data = pollEvent();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
