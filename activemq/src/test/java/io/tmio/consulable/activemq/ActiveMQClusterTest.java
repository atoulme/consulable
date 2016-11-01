package io.tmio.consulable.activemq;

import org.apache.activemq.broker.BrokerService;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;

import io.tmio.consulable.BaseIntegrationTest;
import io.tmio.consulable.ConsulAgent;
import io.tmio.consulable.ConsulService;

public class ActiveMQClusterTest extends BaseIntegrationTest {

    private ConsulAgent agent;

    @Before
    public void setUp() {
        agent = new ConsulAgent(client);
    }

    @Test
    public void testRegisterActiveMQService() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:broker1.xml");
        try {
            ConsulService service = context.getBean(ConsulService.class);
            agent.registerService(service);
        } finally {
            context.close();
        }
    }

    private boolean received = false;
    
    @Test
    public void testCreateCluster() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:broker1.xml");
        ClassPathXmlApplicationContext context2 = new ClassPathXmlApplicationContext("classpath:broker2.xml");
        MqttClient subscriber = new MqttClient("tcp://localhost:1883", "client1");
        try {
            ConsulService service = context.getBean(ConsulService.class);
            BrokerService broker1 = context.getBean(BrokerService.class);

            ConsulService service2 = context2.getBean(ConsulService.class);
            BrokerService broker2 = context2.getBean(BrokerService.class);

            broker1.start();
            broker2.start();

            agent.registerService(service);
            agent.registerService(service2);

            System.err.println(broker1.getNetworkConnectors());
            System.err.println(broker2.getNetworkConnectors());

            Thread.sleep(3000);

            subscriber.connect();
            subscriber.subscribe("foo/#", new IMqttMessageListener() {

                @Override
                public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
                    received = true;
                    System.err.println(new String(arg1.getPayload()));
                }
            });

            MqttClient client2 = new MqttClient("tcp://localhost:1884", "client2");
            client2.connect();
            client2.publish("foo/bar", "hello world".getBytes(), 0, false);

            Thread.sleep(1000);

            Assert.assertTrue("didn't receive the message.", received);
            received = false;

            System.err.println("STARTING 3RD BROKER");
            ClassPathXmlApplicationContext context3 = new ClassPathXmlApplicationContext("classpath:broker3.xml");
            try {
                ConsulService service3 = context3.getBean(ConsulService.class);
                BrokerService broker3 = context3.getBean(BrokerService.class);

                broker3.start();
                
                System.err.println("#############");
                agent.registerService(service3);
                System.err.println("#############");
                
                Thread.sleep(1000);
                System.err.println("====== ");
                System.err.println(broker1.getNetworkConnectors());
                System.err.println(broker2.getNetworkConnectors());
                System.err.println(broker3.getNetworkConnectors());
                
                
                client2.publish("foo/bar", "hello world 2".getBytes(), 0, false);
               
                
                MqttClient client3 = new MqttClient("tcp://localhost:1885", "client3");
                client3.connect();
                client3.publish("foo/bar2", "hello world 2".getBytes(), 1, false);

                Assert.assertTrue("didn't receive the message.", received);
                
                received = false;
                // closing second broker.
                context2.close();
                
                client3.publish("foo/bar3", "hello world 3".getBytes(), 1, false);
                
                client3.disconnect();
                client3.close();
                
            } finally {
                context3.close();
            }
        } finally {

            subscriber.disconnect();
            context.close();
            context2.close();
            subscriber.close();
        }
    }
}
