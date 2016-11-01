package io.tmio.consulable;

import static com.pszymczyk.consul.ConsulStarterBuilder.consulStarter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.pszymczyk.consul.ConsulProcess;

public abstract class BaseIntegrationTest {

    protected static ConsulProcess consul;
    protected static Consul client;

    @BeforeClass
    public static void beforeClass() {
        consul = consulStarter().build().start();
        client = Consul.builder().withHostAndPort(HostAndPort.fromParts("localhost", consul.getHttpPort())).build();
    }

    @AfterClass
    public static void afterClass() {
        if (consul != null) {
            consul.close();
        }
    }

    @Before
    public void beforeTest() {
        consul.reset();
    }
}