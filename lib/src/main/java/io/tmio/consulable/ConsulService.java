
package io.tmio.consulable;

import java.util.List;
import java.util.Map;

import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;

/**
 *
 */
public interface ConsulService {

    public String getName();
    
    public String getId();
    
    public int getPort();
    
    public void update(List<ConsulEntry> services);
}
