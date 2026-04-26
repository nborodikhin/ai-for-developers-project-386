#!/bin/sh
set -e

PORT=${PORT:-80}
export BACKEND_PORT=$((PORT + 1000))

envsubst '${PORT} ${BACKEND_PORT}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

java -jar /app/backend.jar --server.port=${BACKEND_PORT} &

echo "Waiting for backend..."
until curl -sf http://localhost:${BACKEND_PORT}/api/event-types > /dev/null; do
  sleep 2
done
echo "Backend ready"

exec nginx -g 'daemon off;'
