package com.runnable.agent.service;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.runnable.agent.dto.ContainerInfo;
import com.runnable.agent.dto.Jobs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;

@Service
public class DockerClientService {

    DockerClient dockerClient;

    public DockerClientService(DockerClient dockerClient) {
        this.dockerClient = dockerClient; // ✅ use injected bean
    }

    public List<Container> getAllContainer(){
        List<Container> containers = dockerClient.listContainersCmd().exec();
        return containers;
    }

    public List<Image> getAllImages(){
        List<Image> images = dockerClient.listImagesCmd().exec();
        return images;
    }

    public ContainerInfo createNewJob(String jobName) {
        int port = findFreePort();

        ExposedPort vncPort = ExposedPort.tcp(6080); // internal port inside container
        Ports portBindings = new Ports();
        portBindings.bind(vncPort, Ports.Binding.bindPort(port)); // host port

        CreateContainerResponse container = dockerClient.createContainerCmd("coding-agent:latest")
                .withName("agent-" + System.currentTimeMillis())
                .withCmd(jobName)
                .withEnv(
                        List.of(
                                "OLLAMA_API=http://host.docker.internal:11434",
                                "OLLAMA_MODEL=codellama"
                        )
                )
                .withExposedPorts(vncPort)
                .withHostConfig(new HostConfig().withPortBindings(portBindings))
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
        String name = inspect.getName().replaceFirst("/", "");
        String status = inspect.getState().getStatus(); // e.g. "running"
        String vncUrl = "http://localhost:" + port + "/vnc_lite.html";
        return new ContainerInfo(container.getId(), name, status, port, vncUrl);
    }

    private int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort(); // OS gives a free port
        } catch (IOException e) {
            // fallback to random in safe range
            return 10000 + new Random().nextInt(50000);
        }
    }

    public int getVncPort(String containerId) {
        Container container = dockerClient.listContainersCmd()
                .withIdFilter(List.of(containerId))
                .exec()
                .stream()
                .findFirst()
                .orElseThrow();

        return container.getPorts()[1].getPublicPort();
    }

    public void stopContainerByJobId(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }
}
