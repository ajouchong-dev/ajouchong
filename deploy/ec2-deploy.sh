#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f ".env" ]]; then
  echo "[ERROR] .env file not found at: $ROOT_DIR/.env"
  exit 1
fi

if [[ ! -x "./gradlew" ]]; then
  chmod +x ./gradlew
fi

echo "[1/5] Build bootJar"
./gradlew bootJar -x test

echo "[2/5] Build backend image"
docker build -f deploy/Dockerfile -t ajouchong-local:release .

echo "[3/5] Stop old stack"
docker compose -f docker-compose.ec2.yml down --remove-orphans

echo "[4/5] Start new stack"
docker compose --env-file .env -f docker-compose.ec2.yml up -d

echo "[5/5] Verify"
docker compose -f docker-compose.ec2.yml ps
echo
curl -sS -o /dev/null -w "HTTP %{http_code}\n" http://localhost/ || true
