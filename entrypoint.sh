#!/bin/sh
set -e

PORT=${PORT:-80}

# Inject PORT into nginx config
envsubst '${PORT}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Start backend
java -jar /app/backend.jar &

# Wait for backend to be ready before nginx starts accepting requests
echo "Waiting for backend..."
until curl -sf http://localhost:8080/api/event-types > /dev/null; do
  sleep 2
done
echo "Backend ready"

exec nginx -g 'daemon off;'
