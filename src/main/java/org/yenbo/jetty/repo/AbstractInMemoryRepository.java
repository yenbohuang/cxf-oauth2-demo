package org.yenbo.jetty.repo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yenbo.jetty.exception.InMemoryEntityException;

public abstract class AbstractInMemoryRepository<T, K> {

	private static final Logger log = LoggerFactory.getLogger(AbstractInMemoryRepository.class);
	
	protected Map<K, T> map = new HashMap<>();
	
	private void validateKey(K key) {
		
		if (null == key || StringUtils.isBlank(key.toString())) {
			throw new IllegalArgumentException("key is null or blank.");
		}
	}
	
	private void validateEntity(T entity) {
		
		if (null == entity) {
			throw new IllegalArgumentException("entity is null.");
		}
	}
	
	public void save(T entity, K key) {
		
		validateEntity(entity);
		validateKey(key);
		
		int sizeBeforeSave = map.size();
		
		if (map.containsKey(key)) {
			InMemoryEntityException exception = new InMemoryEntityException(
					"Existing entity found");
			exception.addContextValue("key", key);
			exception.addContextValue("entity", entity);
			throw exception;
		}
		
		map.put(key, entity);
		log.debug("Save ({}): key={}, sizeBeforeSave={}, sizeAfterSave={}",
				entity.getClass(), key, sizeBeforeSave, map.size());
	}
	
	public T get(K key) {
		
		validateKey(key);
		
		T entity = map.get(key);
		
		if (null == entity) {
			log.debug("Not found: key={}", key);
		} else {
			log.debug("Found ({}): key={}", entity.getClass(), key);
		}
		
		return entity;
	}
	
	public void delete(K key) {
		
		validateKey(key);
		
		int sizeBeforeDelete = map.size();
		T entity = map.remove(key);
		
		if (null == entity) {
			log.warn("Not found: key={}", key);
		} else {
			log.debug("Delete ({}): key={}, sizeBeforeDelete={}, sizeAfterDelete={}",
					entity.getClass(), key, sizeBeforeDelete, map.size());
		}
	}
	
	public void update(T entity, K key) {
		
		validateEntity(entity);
		validateKey(key);
		
		if (map.containsKey(key)) {
			map.put(key, entity);
			log.debug("Replace ({}): key={}", entity.getClass(), key);
		} else {
			InMemoryEntityException exception = new InMemoryEntityException(
					"Entity does not exist.");
			exception.addContextValue("key", key);
			exception.addContextValue("entity", entity);
			throw exception;
		}
	}
	
	public void saveOrUpdate(T entity, K key) {
		
		validateKey(key);
		
		if (map.containsKey(key)) {
			update(entity, key);
		} else {
			save(entity, key);
		}
	}
}
