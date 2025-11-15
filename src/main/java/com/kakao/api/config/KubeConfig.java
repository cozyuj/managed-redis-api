package com.kakao.api.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KubeConfig {
    @Bean
    public KubernetesClient kubernetesClient() {
        Config config = Config.autoConfigure(null);
        log.info("K8s API 연결: {}", config.getMasterUrl());

        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
