#!/bin/bash
set -e

case $1 in
  server)
    echo "JVM_OPTS="$JVM_OPTS
    echo "→ Starting server"
    exec java $JVM_OPTS -jar /app.jar
    ;;
  *)
    exec "$@"
    ;;
esac