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

# Rebuild Docker image (if changed)
docker build -t agent-ollama .
```

---

## ✅ Status

*


