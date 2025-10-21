#!/bin/bash
# Startup script for Render deployment

echo "=== Mental Wellness Journal - Startup Script ==="
echo "Creating database directory..."

# Create database directory with proper permissions
mkdir -p /opt/render/project/data
chmod 755 /opt/render/project/data

echo "Database directory ready at: /opt/render/project/data"
echo "Listing directory contents:"
ls -la /opt/render/project/data || echo "Directory empty (expected for first run)"

echo "Starting Tomcat..."
exec catalina.sh run

