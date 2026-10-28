#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
if lsof -ti tcp:5173 >/dev/null 2>&1; then
  echo "Port 5173 is already in use. Stop the existing Vite server, then run ./scripts/dev.sh again." >&2
  exit 1
fi
"$root/backend/mvnw" -f "$root/backend/pom.xml" spring-boot:run >"$root/.merkle-backend.log" 2>&1 &
backend_pid=$!
trap 'kill "$backend_pid" 2>/dev/null || true' EXIT
printf 'Starting backend'
for attempt in {1..30}; do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    printf '\nBackend is ready at http://localhost:8080\n'
    break
  fi
  if ! kill -0 "$backend_pid" 2>/dev/null; then
    printf '\nBackend failed to start. See %s/.merkle-backend.log\n' "$root" >&2
    exit 1
  fi
  printf '.'
  sleep 1
done
if ! curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
  printf '\nBackend did not become ready. See %s/.merkle-backend.log\n' "$root" >&2
  exit 1
fi
(cd "$root/frontend" && npm run dev -- --port 5173 --strictPort)
