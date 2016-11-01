package io.tmio.consulable;

import java.util.List;
import java.util.Map;

import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;

public class ConsulServiceImpl implements ConsulService {
    
    private String id;
    
    private List<ConsulEntry> services;
    
    public ConsulServiceImpl(String id) {
        this.id = id;
    }

    public String getName() {
        return "foo";
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public int getPort() {
        return 80;
    }
    
    public void update(List<ConsulEntry> services) {
        this.services = services;
    }
    
    public List<ConsulEntry> getServices() {
        return services;
    }
}
