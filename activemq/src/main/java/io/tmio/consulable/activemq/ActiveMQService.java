package io.tmio.consulable.activemq;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.DiscoveryNetworkConnector;
import org.apache.activemq.network.NetworkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.tmio.consulable.ConsulEntry;
import io.tmio.consulable.ConsulService;

public class ActiveMQService implements ConsulService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActiveMQService.class);

    private String name;

    private String id;

    private int port;

    private BrokerService broker;

    public ActiveMQService(String name, String id, int port) {
        this.name = name;
        this.id = id;
        this.port = port;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void update(List<ConsulEntry> services) {
        logger.debug("{} for {}", services, broker.getBrokerName());
        List<NetworkConnector> connectors = broker.getNetworkConnectors();
        List<NetworkConnector> toRemove = new ArrayList<>(connectors);
        for (ConsulEntry service : services) {
            try {
                logger.debug("{}", service);
                // static:(tcp://localhost:61616)
                URI uri = new URI("static:(tcp://" + service.getHost() + ":" + service.getPort() + ")");
                boolean notFound = true;
                for (NetworkConnector nc : connectors) {
                    if ((((DiscoveryNetworkConnector) nc).getUri()).equals(uri)) {
                        System.err.println("Found connector " + ((DiscoveryNetworkConnector) nc).getUri());
                        toRemove.remove(nc);
                        notFound = false;
                        break;
                    }
                }
                if (notFound) {
                    System.err.println("Creating new connector " + uri);
                    DiscoveryNetworkConnector connector = new DiscoveryNetworkConnector(uri);
                    connector.setName(UUID.randomUUID().toString());
                    broker.addNetworkConnector(connector);
                    connector.start();
                }
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        for (NetworkConnector nc : toRemove) {
            try {
                nc.stop();
                broker.removeNetworkConnector(nc);
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
            
        }
        
        
    }

    public void setBroker(BrokerService broker) {
        this.broker = broker;
    }

}
