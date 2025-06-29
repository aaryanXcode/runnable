import os
import sys
import time
import subprocess
import requests
import traceback

MODEL = "qwen3:0.6b"
OLLAMA_API = "http://localhost:11434/api/generate"

def log(level, msg):
    print(f"[{level}] {msg}")

def generate_code_with_ollama(prompt: str) -> str:
    payload = {
        "model": MODEL,
        "prompt": prompt,
        "stream": False
    }

    try:
        log("INFO", f"Sending prompt to Ollama at {OLLAMA_API}")
        res = requests.post(OLLAMA_API, json=payload, timeout=70)
        res.raise_for_status()
        data = res.json()
        log("INFO", f"Code generation succeeded")
        return data.get("response", "# Ollama returned no response.")
    except Exception as e:
        log("ERROR", f"Failed to generate code with Ollama: {e}")
        traceback.print_exc()
        return "# ERROR: Could not connect to Ollama instance."

def launch(app, *args):
    try:
        subprocess.Popen([app, *args])
        log("INFO", f"Launched: {app} {' '.join(args)}")
    except FileNotFoundError:
        log("ERROR", f"{app} not found in container.")
    except Exception as e:
        log("ERROR", f"Failed to launch {app}: {e}")
        traceback.print_exc()

def write_code_to_file(code_path, task, code):
    try:
        with open(code_path, "w") as f:
            f.write(f"# Task: {task}\n\n")
            f.write(code.strip() + "\n")
        log("INFO", f"Code written to: {code_path}")
    except Exception as e:
        log("ERROR", f"Failed to write code to file: {e}")
        traceback.print_exc()

def main():
    task = sys.argv[1] if len(sys.argv) > 1 else "Build a Hello World app"
    log("INFO", f"Task received: {task}")

    output_dir = "/output"
    try:
        os.makedirs(output_dir, exist_ok=True)
    except Exception as e:
        log("ERROR", f"Failed to create output directory: {e}")
        return

    filename = task.lower().replace(" ", "_").replace("'", "") + ".py"
    code_path = os.path.join(output_dir, filename)

    log("INFO", "Generating code using Ollama...")
    code = generate_code_with_ollama(task)
    write_code_to_file(code_path, task, code)

    # Attempt to launch development apps
    launch("xclock")
    launch("gedit", code_path)
    launch("xterm")
    launch("pcmanfm", output_dir)

    log("INFO", "Container is ready and running for VNC.")
    time.sleep(3600)

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        log("FATAL", f"Unhandled exception: {e}")
        traceback.print_exc()
