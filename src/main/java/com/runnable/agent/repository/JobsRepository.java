package com.runnable.agent.repository;


import com.runnable.agent.dto.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobsRepository extends JpaRepository<Jobs, Integer> {
    // You can add custom queries here if needed
    List<Jobs> findAllByJobStatus(String jobStatus);

    Optional<Jobs> findById(Integer jobId);
    Jobs findByContainerId(String containerId);
}