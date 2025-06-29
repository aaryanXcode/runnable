package com.runnable.agent.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Jobcontroller {

    public Jobcontroller(){};

    @GetMapping("/jobs")
    public ResponseEntity<List<String>> getJobSchedule() {
        List<String> jobs = List.of(
                "Build a todo app in React",
                "Generate a Python script for file renaming",
                "Create a Java REST API for task tracking"
        );

        return ResponseEntity.ok(jobs);
    }
}
