#!/bin/bash
set -e

echo "[Startup] Setting DISPLAY..."
export DISPLAY=:0

echo "[Startup] Starting virtual screen (Xvfb)..."
/usr/bin/Xvfb :0 -screen 0 1920x1080x24 &

echo "[Startup] Starting window manager (fluxbox)..."
fluxbox &

echo "[Startup] Starting VNC server..."
x11vnc -display :0 -forever -nopw -shared -rfbport 5900 &

echo "[Startup] Starting noVNC proxy..."
/opt/novnc/utils/novnc_proxy --vnc localhost:5900 --listen 6080 &

# Wait a bit for everything to initialize
sleep 3

# Read full job description
JOB="$*"
echo "[Startup] Running job: $JOB"

# Launch agent with job input
python3 /agent.py "$JOB"

# Keep container alive
tail -f /dev/null
