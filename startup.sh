#!/bin/bash
# Startup script for Render deployment

echo "=== Mental Wellness Journal - Startup Script ==="
echo "Environment variables:"
echo "  DB_PATH: ${DB_PATH}"
echo "  JAVA_OPTS: ${JAVA_OPTS}"

echo ""
echo "Creating database directory..."

# Create database directory with proper permissions
mkdir -p /opt/render/project/data
chmod 777 /opt/render/project/data

echo "Database directory ready at: /opt/render/project/data"
echo "Directory permissions:"
ls -ld /opt/render/project/data

echo ""
echo "Checking mount point:"
df -h /opt/render/project/data

echo ""
echo "Directory contents:"
ls -la /opt/render/project/data || echo "Directory empty (expected for first run)"

echo ""
echo "Testing write permissions..."
touch /opt/render/project/data/test_write.txt && echo "✓ Write test successful" || echo "✗ Write test failed"
rm -f /opt/render/project/data/test_write.txt

echo ""
echo "Exporting DB_PATH for Java application..."
export DB_PATH="${DB_PATH:-/opt/render/project/data/mental_journal}"
echo "DB_PATH is now: $DB_PATH"

echo ""
echo "Starting Tomcat..."
exec catalina.sh run

