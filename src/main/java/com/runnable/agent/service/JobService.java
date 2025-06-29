package com.runnable.agent.service;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.runnable.agent.dto.ContainerInfo;
import com.runnable.agent.dto.Jobs;
import com.runnable.agent.repository.JobsRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private static final Logger log = LogManager.getLogger(JobService.class);
    private final JobsRepository jobsRepository;
    private final DockerClientService dockerClientService;
    public JobService(JobsRepository jobsRepository,DockerClientService dockerClientService) {
        this.jobsRepository = jobsRepository;
        this.dockerClientService = dockerClientService;
    }

    public String runJob(String jobName) {
        return "Running job: " + jobName;
    }

    //list all the jobs create
    public List<String> getAllJobs() {
        return jobsRepository.findAll().stream()
                .map(job -> {
                    String vncUrl = "unknown";
                    try {
                        int port = dockerClientService.getVncPort(job.getContainerId());
                        vncUrl = "http://localhost:" + port + "/vnc.html";
                    } catch (Exception e) {
                        vncUrl = "N/A";
                    }
                    return job.getJobId() + " - " + job.getJobName() + " [" + job.getJobStatus() + "] VNC: " + vncUrl;
                })
                .toList();
    }

    public List<String> getAllContainers() {
        List<Container> containers = dockerClientService.getAllContainer();

        return containers.stream().map(container -> {
            String id = container.getId();
            String name = container.getNames() != null && container.getNames().length > 0 ? container.getNames()[0] : "unknown";
            String status = container.getStatus();
            String ports = container.getPorts() != null && container.getPorts().length > 0
                    ? container.getPorts()[0].getPublicPort() + "->" + container.getPorts()[0].getPrivatePort()
                    : "no port";

            String vncUrl = ports.contains("6080") ? "http://localhost:6080" : "N/A";

            return "ID: " + id + ", Name: " + name + ", Status: " + status + ", VNC: " + vncUrl;
        }).toList();
    }

    public List<String> getAllImages() {
        List<Image> images = dockerClientService.getAllImages();

        return images.stream().map(image -> {
            String id = image.getId().substring(7, 19); // sha256:xxxx
            String tags = (image.getRepoTags() != null) ? String.join(", ", image.getRepoTags()) : "<none>";
            long sizeMB = image.getSize() / (1024 * 1024);
            return String.format("ID: %-12s | Tags: %-30s | Size: %dMB", id, tags, sizeMB);
        }).toList();
    }


    public boolean createJob(String jobName) {
        Jobs job = new Jobs();
        job.setJobName(jobName);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        try {
            // Try to start the container
            ContainerInfo info = dockerClientService.createNewJob(jobName);

            // If successful, set container info and mark as STARTED
            job.setContainerId(info.id());
            job.setVncPort(info.port());
            job.setJobStatus("STARTED");

        } catch (Exception e) {
            System.err.println("❌ Failed to create job container: " + e.getMessage());
            job.setJobStatus("FAILED");
        }
        // Save job regardless of success/failure
        jobsRepository.save(job);
        return !"FAILED".equals(job.getJobStatus());
    }

    public boolean stopJobById(int jobId) {
        Optional<Jobs> jobOpt = jobsRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            System.out.println("⚠️ Job ID not found: " + jobId);
            return false;
        }

        Jobs job = jobOpt.get();
        String containerId = job.getContainerId();

        if (containerId == null || containerId.isEmpty()) {
            System.out.println("⚠️ No container associated with job: " + jobId);
            return false;
        }
        try {
            dockerClientService.stopContainerByJobId(containerId);
            job.setJobStatus("STOPPED");
            job.setUpdatedAt(LocalDateTime.now());
            jobsRepository.save(job);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to stop container: " + e.getMessage());
            return false;
        }
    }

    public void stopAllJobs() {
        List<Jobs> runningJobs = jobsRepository.findAllByJobStatus("STARTED");
        for (Jobs job : runningJobs) {
            try {
                dockerClientService.stopContainerByJobId(job.getContainerId());
                job.setJobStatus("STOPPED");
                job.setUpdatedAt(LocalDateTime.now());
                jobsRepository.save(job);
                System.out.println("✅ Stopped job " + job.getJobId() + ": " + job.getJobName());
            } catch (Exception e) {
                System.out.println("❌ Failed to stop job " + job.getJobId() + ": " + e.getMessage());
            }
        }
        if (runningJobs.isEmpty()) {
            System.out.println("⚠️ No running jobs found.");
        }
    }

    public boolean startJobById(int jobId) {
        Optional<Jobs> optionalJob = jobsRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            System.out.println("❌ Job ID " + jobId + " not found.");
            return false;
        }

        Jobs job = optionalJob.get();
        String containerId = job.getContainerId();

        if (containerId == null || containerId.isEmpty()) {
            System.out.println("❌ No container ID found for job " + jobId);
            return false;
        }

        try {
            dockerClientService.startContainer(containerId);
            job.setJobStatus("STARTED");
            job.setUpdatedAt(LocalDateTime.now());
            jobsRepository.save(job);

            System.out.println("✅ Job " + jobId + " container restarted successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("❌ Failed to restart container for job " + jobId + ": " + e.getMessage());
            job.setJobStatus("FAILED");
            job.setUpdatedAt(LocalDateTime.now());
            jobsRepository.save(job);
            return false;
        }
    }



}
