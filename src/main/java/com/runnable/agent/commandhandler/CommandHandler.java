package com.runnable.agent.commandhandler;

@FunctionalInterface
public interface CommandHandler {
    void handle(String[] args);
}
