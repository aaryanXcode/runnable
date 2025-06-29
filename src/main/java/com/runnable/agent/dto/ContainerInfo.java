package com.runnable.agent.dto;

public record ContainerInfo(
        String id,
        String name,
        String status,
        int port,
        String vncUrl
) {}
