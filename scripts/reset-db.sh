#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

echo "Resetting MahaSetu containers and database volumes..."
docker compose down -v
echo "Volume data cleared."
echo "You can now run ./scripts/start.sh to start fresh."
