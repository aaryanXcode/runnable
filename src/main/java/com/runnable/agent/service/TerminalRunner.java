package com.runnable.agent.service;

import com.runnable.agent.commandhandler.CommandHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Component
public class TerminalRunner implements CommandLineRunner {

    private final JobService jobService;
    private final Map<String, CommandHandler> commands = new HashMap<>();

    public TerminalRunner(JobService jobService) {
        this.jobService = jobService;
        registerCommands();
    }

    private void registerCommands() {
        commands.put("run-job", args -> {
            if (args.length < 2) {
                System.out.println("Usage: run-job <job-name>");
                return;
            }
            String jobName = args[1];
            String result = jobService.runJob(jobName);
            System.out.println("Result: " + result);
        });

        commands.put("create-job", args -> {
            if (args.length < 2) {
                System.out.println("Usage: create-job <job-name>");
                return;
            }
            String jobName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            Thread loading = showLoading("⏳ Creating job and starting container");
            try {
                boolean success = jobService.createJob(jobName);
                loading.interrupt();
                System.out.print("\r✅ Job created and container started successfully!         \n");
                System.out.println("Result: " + success);
            } catch (Exception e) {
                loading.interrupt();
                System.out.print("\r❌ Failed to create job or run container: " + e.getMessage() + "\n");
            }
        });

        commands.put("stop-job", args -> {
            if (args.length < 2) {
                System.out.println("Usage: stop-job <jobId>");
                return;
            }

            try {
                int jobId = Integer.parseInt(args[1]);
                boolean result = jobService.stopJobById(jobId);
                System.out.println(result ? "✅ Job stopped successfully" : "❌ Failed to stop job.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid job ID format");
            }
        });

        commands.put("stop-all", args -> {
            jobService.stopAllJobs();
        });

        commands.put("list-jobs", args -> {
            jobService.getAllJobs().forEach(job -> System.out.println("- " + job));
        });

        commands.put("list-containers", args -> {
            jobService.getAllContainers().forEach(System.out::println);
        });

        commands.put("list-images", args -> {
            jobService.getAllImages().forEach(System.out::println);
        });

        commands.put("start-job", args -> {
            if (args.length < 2) {
                System.out.println("Usage: start-job <job-id>");
                return;
            }
            try {
                int jobId = Integer.parseInt(args[1]);
                jobService.startJobById(jobId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid job ID format.");
            }
        });

        commands.put("help", args -> printHelp());

        commands.put("exit", args -> {
            System.out.println("Bye!");
            System.exit(0);
        });
    }

    @Override
    public void run(String... args) {
        System.out.println("Runnable Terminal started. Type 'help' for a list of commands.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] tokens = input.split("\\s+");
                String command = tokens[0];

                CommandHandler handler = commands.get(command);
                if (handler != null) {
                    try {
                        handler.handle(tokens);
                    } catch (Exception e) {
                        System.out.println("⚠️  Error executing command '" + command + "': " + e.getMessage());
                    }
                } else {
                    System.out.println("Unknown command: " + command + ". Type 'help' for commands.");
                }
            }
        } catch (Exception e) {
            System.out.println("Fatal error in terminal: " + e.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("""
                 create-job <name>   - Create a new Job
                 run-job <name>     - Run a job by name
                 stop-job <id>      - Stop a job by ID
                 stop-all           - Stop all running jobs [pending]...
                 list-jobs          - List available jobs
                 list-containers    - List running containers
                 list-images        - List available images
                 help               - Show this help message
                 exit               - Exit the terminal
            """);
    }

    private Thread showLoading(String message) {
        Thread loadingThread = new Thread(() -> {
            String[] dots = {".  ", ".. ", "..."};
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("\r" + message + dots[i++ % dots.length]);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        loadingThread.start();
        return loadingThread;
    }

}
