package org.yenbo.jetty.repo;

import java.util.UUID;

import org.yenbo.jetty.data.AbstractInMemoryOauth2Data;

public class AbstractInMemoryOauth2Repository<T extends AbstractInMemoryOauth2Data, K>
	extends AbstractInMemoryRepository<T, K> {

	public boolean containsClientId(UUID clientId) {
		
		if (null == clientId) {
			throw new IllegalArgumentException("clientId is null.");
		}
		
		for (T entity: map.values()) {
			if (clientId.equals(entity.getClientId())) {
				return true;
			}
		}
		
		return false;
	}
}
