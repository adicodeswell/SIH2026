#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

echo "Stopping MahaSetu Platform..."
docker compose down
echo "All MahaSetu services stopped."
