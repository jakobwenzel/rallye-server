/**
 * 
 */
package de.rallye.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Class implements a Cache that allows to remove entries after a defined lifetime.
 * @author Felix Huebner
 * @date 12.12.2012
 * 
 */
@SuppressWarnings("serial")
public class TemporaryCache<E, V> extends LinkedHashMap<E, V> {

	// temprarily time for a element default 5 minutes
	private static int validTime = 5 * 60;
	private TreeMap<E, Long> lifeTime = new TreeMap<E, Long>();
	private Lock lock = new ReentrantLock();

	private Logger logger = LogManager.getLogger(TemporaryCache.class.getName());

	/**
	 * @param ValidTime_ms
	 *            time im ms how long a entry is valid
	 */
	public void initialize(int validTime_ms) {
		validTime = validTime_ms * 1000;
		lifeTime = new TreeMap<E, Long>();
		logger.info("Setup "+TemporaryCache.class.getName()+": set validTime to: " + validTime_ms
				+ "ms (" + validTime_ms / 60 + "min)");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.LinkedHashMap#get(java.lang.Object)
	 */
	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return super.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(E key, V value) {
		lock.lock();
		lifeTime.put(key, System.currentTimeMillis());
		V v = super.put(key, value);
		lock.unlock();
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		lock.lock();
		lifeTime.remove(key);
		V v = super.remove(key);
		lock.unlock();
		return v;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<E, V> eldest) {
		long lt = System.currentTimeMillis() - lifeTime.get(eldest.getKey());

		logger.info("Oldes Element \"" + eldest.toString() + "\" lifetime is: "
				+ (lt / 1000) + "ms (max: " + validTime / 1000 + "ms) remove: "
				+ Boolean.toString(lt > validTime));

		if (lt > validTime) {
			lifeTime.remove(eldest.getKey());
		}

		return lt > validTime;
	}

	/**
	 * this method returns a Status info about this instance of the
	 * TemporarilyCache
	 * 
	 * @return
	 */
	public String getStatus() {
		lock.lock();
		StringBuilder str = new StringBuilder();
		Entry<E, Long> e = lifeTime.firstEntry();
		Iterator<Entry<E, Long>> i = lifeTime.entrySet().iterator();
		
		while(i.hasNext()) {
			str.append("Entry: ").append(i.next().toString()).append("\n");
		}
		
		str.append(TemporaryCache.class.getName()).append(" Status: ");
		str.append("Elements: " + super.size());
		str.append(" oldestElement: " + e.getKey()+"="+super.get(e.getKey()).toString());
		str.append(" lifeTime: "
				+ ((System.currentTimeMillis() - e.getValue()) / 1000) + "ms");
		lock.unlock();
		return str.toString();
	}

}
