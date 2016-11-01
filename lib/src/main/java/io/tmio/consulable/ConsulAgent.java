package io.tmio.consulable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;


public class ConsulAgent {
    
    private static final long TTL = 60L;
    
    private class ConsulEntryImpl implements ConsulEntry {
        
        private String host;
        
        private int port;
        
        private String serviceId;
        
        public ConsulEntryImpl(String host, int port, String serviceId) {
          this.host = host;
          this.port = port;
          this.serviceId = serviceId;
        }
        
        @Override
        public String getHost() {
            return host;
        }
        
        @Override
        public int getPort() {
            return port;
        }
        
        @Override
        public String getServiceId() {
            return serviceId;
        }
        
        @Override
        public String toString() {
            return "<" + serviceId + "> " + host + ":" + port;
        }
    }
    
    private class CheckInTask extends TimerTask {

        @Override
        public void run() {
            AgentClient agentClient = client.agentClient();
            for (String serviceId : services.keySet()) {
                try {
                    agentClient.pass(serviceId);
                } catch (NotRegisteredException e) {
                    
                }
            }

        }
    }

    private Consul client;
    
    private Timer timer = new Timer();

    private Map<String, ServiceHealthCache> services = new ConcurrentHashMap<String, ServiceHealthCache>();

    public ConsulAgent(Consul consulClient) {
        client = consulClient;
    }
    
    public void start() {
        CheckInTask checkInTask = new CheckInTask();
        timer.schedule(checkInTask, (int) Math.floor(TTL - (TTL * 0.1)));
    }
    
    public void stop() {
        timer.cancel();
    }
    
    private String computeServiceKey(ConsulService service) {
        return service.getId();
    }
    
    private ServiceHealthCache computeHealthService(ConsulService service) {
        try {
            ServiceHealthCache svHealth = ServiceHealthCache.newCache(client.healthClient(), service.getName());
            svHealth.start();
            return svHealth;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerService(ConsulService service) {
        ServiceHealthCache svHealth = services.computeIfAbsent(computeServiceKey(service), k -> computeHealthService(service));
        
        AgentClient agentClient = client.agentClient();

        agentClient.register(service.getPort(), TTL, service.getName(), service.getId());
        try {
            agentClient.pass(service.getId());
        } catch(NotRegisteredException e) {
            throw new RuntimeException(e);
        }
        updateService(service, svHealth.getMap());
        
        
        svHealth.addListener(new ConsulCache.Listener<ServiceHealthKey, ServiceHealth>() {
            @Override
            public void notify(Map<ServiceHealthKey, ServiceHealth> newValues) {
                updateService(service, newValues);
            }
        });
    }
    
    private void updateService(ConsulService service, Map<ServiceHealthKey, ServiceHealth> newValues) {
        List<ConsulEntry> entries = new ArrayList<>();
        for (ServiceHealthKey key : newValues.keySet()) {
            entries.add(new ConsulEntryImpl(key.getHost(), key.getPort(), key.getServiceId()));
        }
        service.update(entries);
    }
    
    public void removeService(ConsulService service) {
        services.remove(computeServiceKey(service));
        
        AgentClient agentClient = client.agentClient();

        agentClient.deregister(service.getId());
    }
}
