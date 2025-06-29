FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

# Install all dependencies
RUN apt-get update && apt-get install -y \
    python3 python3-pip \
    x11vnc xvfb fluxbox \
    wget unzip curl git \
    pcmanfm \
    xterm \
    chromium-browser \
    build-essential \
    nodejs npm \
    xdotool gedit x11-apps \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Set up noVNC
RUN mkdir -p /opt/novnc && \
    curl -L https://github.com/novnc/noVNC/archive/refs/heads/master.zip -o /tmp/novnc.zip && \
    unzip /tmp/novnc.zip -d /opt && \
    mv /opt/noVNC-master/* /opt/novnc/ && \
    chmod +x /opt/novnc/utils/novnc_proxy

# Install Python packages
RUN pip3 install numpy

# Copy application files
COPY agent.py /agent.py
COPY startup.sh /startup.sh

# Fix line endings and permissions
RUN dos2unix /startup.sh && chmod +x /startup.sh

# Expose VNC and noVNC ports
EXPOSE 5900 6080

# Run startup script
ENTRYPOINT ["/startup.sh"]
