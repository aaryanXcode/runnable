package com.runnable.agent.configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientConfiguration {

    @Bean
    public DockerClient dockerClient() {
        return DockerClientBuilder.getInstance()
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }
}
