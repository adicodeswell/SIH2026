#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

echo "========================================================"
echo " Starting MahaSetu Platform (SIH Problem Statement 26129)"
echo "========================================================"

# Avoid port 5432 conflict if host PostgreSQL is running
if ss -tuln | grep -q ":5432 "; then
  echo "[INFO] Host PostgreSQL detected on port 5432. Mapping container PostgreSQL to port 5433."
  export POSTGRES_PORT=5433
fi

docker compose up -d --build

echo ""
echo "========================================================"
echo " MahaSetu Platform is booting up!"
echo "========================================================"
echo " - Frontend Portal:            http://localhost:3000"
echo "   * Citizen Demo Portal:      http://localhost:3000/citizen/?demo=true"
echo "   * Officer Demo Portal:      http://localhost:3000/officer/?demo=true"
echo " - Keycloak IAM:               http://localhost:8080 (admin/admin)"
echo " - Application Service:        http://localhost:8081/actuator/health"
echo " - Interoperability Service:   http://localhost:8082/actuator/health"
echo " - Security Workflow Service:  http://localhost:8083/actuator/health"
echo " - Mock Legacy Systems:        http://localhost:8091/employment/MH1001"
echo "========================================================"
echo " To view logs: docker compose logs -f"
echo " To stop:      ./scripts/stop.sh"
