package org.acme;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.camel.builder.RouteBuilder;
import org.infinispan.commons.util.OS;

import java.util.HashMap;
import java.util.Map;

import static org.infinispan.client.hotrod.impl.ConfigurationProperties.CLIENT_INTELLIGENCE;

public class Routes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // Puts a cache entry every 10 seconds
        from("timer:cachePut?period=10s")
                .setHeader("CamelInfinispanKey").constant("foo")
                .setHeader("CamelInfinispanValue").constant("bar")
                .to("infinispan:test?operation=PUT")
                .log("Cache PUT : ${header.CamelInfinispanKey} = ${header.CamelInfinispanValue}");
    }

    @Produces
    @Named("additionalConfig")
    Map<String, String> additionalInfinispanConfig() {
        Map<String, String> config = new HashMap<>();
        if (OS.getCurrentOs().equals(OS.MAC_OS) || OS.getCurrentOs().equals(OS.WINDOWS)) {
            config.put(CLIENT_INTELLIGENCE, "BASIC");
        }
        return config;
    }
}
