#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
command -v java >/dev/null || { echo "Install a Java 21+ JDK."; exit 1; }
command -v node >/dev/null || { echo "Install Node.js 20+."; exit 1; }
command -v npm >/dev/null || { echo "Install npm."; exit 1; }
"$root/backend/mvnw" -q -DskipTests package
(cd "$root/frontend" && npm install && npm run build)
echo "Setup complete. Run ./scripts/dev.sh"
