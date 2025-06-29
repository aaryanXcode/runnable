# 🧠 Ollama-Based Job Execution Platform

This platform allows executing AI code-generation jobs inside Docker containers. Each container connects to a local Ollama instance for inference and exposes a GUI via VNC to interact with the generated code.

---

## 🔧 Project Components

### 1. **Local Ollama LLM Server**

* Runs on host: `http://localhost:11434`
* Must have the required model (`qwen3:0.6b`) preloaded.

### 2. **Spring Boot Backend**

* Accepts job requests.
* Spins up Docker containers for each job.

### 3. **Dockerized Agent Container**

* Runs a Python script inside the container.
* Generates code via Ollama and launches GUI tools (gedit, xclock, xterm, pcmanfm).
* GUI is accessible via VNC using `noVNC`.

### 4. **VNC Interface**

* Access containers via browser: `http://localhost:<mapped-port>/vnc.html`

---

## ⚙️ Setup Guide

### 1. **Start Ollama and preload model**

```bash
ollama run qwen3:0.6b
```

Wait until the model is ready and returns a response.

### 2. **Test Ollama manually**

```bash
curl -X POST http://localhost:11434/api/generate \
  -d '{"model":"qwen3:0.6b","prompt":"say hello","stream":false}' \
  -H "Content-Type: application/json"
```

### 3. **Build and run Spring Boot application**

```bash
./mvnw spring-boot:run
```

### 4. **Create a Job (via API or UI)**

* The backend starts a container.
* A dynamic port is assigned for VNC access.

### 5. **Access the container**

* URL printed by backend, e.g.:

```
http://localhost:6080/vnc.html?host=localhost&port=6080
```

---

## 🚀 Agent Container Behavior

**Python script actions:**

1. Accepts task via command-line argument.
2. Sends prompt to `http://localhost:11434/api/generate`.
3. Saves generated code to `/output/<task>.py`.
4. Launches:

   * `gedit` for editing
   * `xclock` for UI test
   * `xterm` terminal
   * `pcmanfm` file browser
5. Waits for 1 hour with GUI active.

---

## 🛠️ Utility Summary

| Utility   | Purpose                          |
| --------- | -------------------------------- |
| `gedit`   | View/edit generated code         |
| `xclock`  | Simple visual check              |
| `xterm`   | Terminal access inside container |
| `pcmanfm` | File explorer inside container   |
| `noVNC`   | Browser-based GUI interface      |
| `ollama`  | LLM backend for code generation  |

---

## ✅ Tips

* Always preload the Ollama model before container access.
* Use longer timeout in Python script (e.g. 60s).
* Use `docker ps` to inspect running containers and mapped ports.

---

## 📂 Logs

* Python logs printed to container stdout.
* View via `docker logs <container_id>`

---

## 🔄 Restarting Components

```bash
# Restart Ollama
ollama run qwen3:0.6b

# Restart backend
./mvnw spring-boot:run


#if you want to rebuild everything
docker-compose --env-file .env up --build

#clean build 
docker-compose down --volumes --remove-orphans
docker-compose --env-file .env up --build

#Run
docker compose run --rm app


#if no dependency need restart
docker-compose --env-file .env up --build --no-deps app

```

---

🔁 Architecture Flow Overview

Here’s how the system flows from user input to code execution inside a Docker container:
Components Involved:

    User/UI or API

        Initiates job request (task prompt).

    Spring Boot Backend

        Receives request.

        Starts a Docker container running an Agent.

        Maps a dynamic VNC port for GUI access.

    Agent (Dockerized Python Script)

        Parses task.

        Sends prompt to Ollama LLM (running on host).

        Receives generated code.

        Writes code to file.

        Launches GUI tools (gedit, xterm, xclock, pcmanfm).

    Ollama LLM

        Processes prompt and returns code.

    VNC + noVNC (Browser GUI)

        Provides web-based access to container GUI tools.

        
    [User/API] 
    ↓ (task prompt)
    [Spring Boot Backend] 
        ↓ (Docker API)
    [Docker Container: Agent] 
        ↓ (HTTP request)
    [Ollama LLM Server on Host] 
        ↑ (code response)
    [Agent writes file & launches GUI tools]
        ↓
    [noVNC: VNC exposed in browser]
        ↓
    [User interacts with container GUI]

    +------------------------+
    |  👨‍💻 User submits task  |
    +------------------------+
                 |
                 v
    +-----------------------------+
    | Spring Boot Backend (API)  |
    | - Accepts job requests     |
    | - Starts Docker container  |
    +-----------------------------+
                 |
                 v
    +------------------------------+
    | 🐳 Dockerized Agent Container |
    | - Runs Python agent         |
    | - Calls Ollama for code     |
    +------------------------------+
                 |
                 v
    +-----------------------------+
    | 🧠 Ollama (Local LLM)       |
    | - Model: qwen3:0.6b         |
    | - Generates code            |
    +-----------------------------+
                 |
                 v
    +-----------------------------+
    | Agent writes code to file   |
    | and launches GUI tools:     |
    | - gedit (edit code)         |
    | - xterm (terminal)          |
    | - pcmanfm (files)           |
    | - xclock (UI test)          |
    +-----------------------------+
                 |
                 v
    +-----------------------------+
    | 🖥️ Access via noVNC Web GUI |
    | - URL: http://localhost:PORT/vnc.html |
    +-----------------------------+


  
  ![flow](https://github.com/user-attachments/assets/1d206694-5655-4a56-8a97-8e7d11639117)

  # Live running demo snaps
  ![Screenshot from 2025-06-29 16-51-40](https://github.com/user-attachments/assets/ad29da55-d88f-4c32-8d35-9b13f1b313ea)

  ✅ Working Features

    Create a new job

    List jobs with their running VNC URLs

    Delete jobs (removes job entry + container)

    Delete jobs by ID

    Restart jobs by ID

    Check job status
🚧 Work in Progress / Known Issues

    🔄 LLM Agent: Needs improvement in response handling and integration

    📥 Output File Download: Not implemented yet

    🧪 UI & API: Currently a basic CLI with minimal functionality

    🐞 Bugs present and under active fix

        Known issue: Status update inconsistencies

    🐳 Agent-Docker integration still being optimized

  ![Screenshot from 2025-06-29 16-58-23](https://github.com/user-attachments/assets/a08e0127-7bf7-451b-b89b-5412ad87171f)







