/**
 * 
 */
package io.tmio.consulable;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.orbitz.consul.model.health.ServiceHealth;

public class ConsulServiceLifecycleTest extends BaseIntegrationTest {
    
    private ConsulAgent agent;
    
    @Before
    public void setUp() {
        agent = new ConsulAgent(client);
    }

    @Test
    public void testNewService() {
        ConsulService service = new ConsulServiceImpl("1");
        agent.registerService(service);
        List<ServiceHealth> services = client.healthClient().getHealthyServiceInstances("foo").getResponse();
        Assert.assertEquals(1, services.size());
        Assert.assertEquals("1", services.get(0).getService().getId());
    }
    
    @Test
    public void testServiceSeesAnotherService() throws Exception {
        ConsulServiceImpl service = new ConsulServiceImpl("1");
        agent.registerService(service);
        ConsulService service2 = new ConsulServiceImpl("2");
        agent.registerService(service2);
        List<ServiceHealth> services = client.healthClient().getHealthyServiceInstances("foo").getResponse();
        Assert.assertEquals(2, services.size());
        Thread.sleep(2000);
        Assert.assertEquals(2, service.getServices().size());
    }
    
    @Test
    public void addAndRemoveService() throws Exception {
        ConsulServiceImpl service = new ConsulServiceImpl("1");
        agent.registerService(service);
        ConsulServiceImpl service2 = new ConsulServiceImpl("2");
        agent.registerService(service2);
        Thread.sleep(1000);
        List<ServiceHealth> services = client.healthClient().getHealthyServiceInstances("foo").getResponse();
        Assert.assertEquals(2, services.size());
        agent.removeService(service);
        
        services = client.healthClient().getHealthyServiceInstances("foo").getResponse();
        Assert.assertEquals(1, services.size());
        
        Assert.assertEquals(1, service2.getServices().size());
    }
}
