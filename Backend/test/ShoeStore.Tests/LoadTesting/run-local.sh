#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$backend_dir"
mode="${1:-all}"
users="${2:-500}"
case "$mode" in all|seed|last-pair|hot-sku|mixed|journey) ;; *) echo 'Usage: bash run-local.sh [all|seed|last-pair|hot-sku|mixed|journey] [1..1000 users]' >&2; exit 2 ;; esac
if ! [[ "$users" =~ ^[0-9]+$ ]] || (( users < 1 || users > 1000 )); then
  echo 'Users must be 1..1000' >&2; exit 2
fi
for executable in dotnet docker k6 curl; do command -v "$executable" >/dev/null; done

# Ignore any inherited Azure connection/JWT settings; use the dedicated local fixture.
# PostgreSQL defaults to 100 client slots. Leave room for audit/admin connections.
export ConnectionStrings__DefaultConnection='Host=127.0.0.1;Port=5433;Database=shoestore_loadtest;Username=shoestore;Password=shoestore_local_only;Maximum Pool Size=80'
export Jwt__Key='ShoeStore_Local_LoadTest_Only_Key_2026_Change_For_Production_12345'
export Jwt__Issuer='shoestore-loadtest'
export Jwt__Audience='shoestore-loadtest'
export ASPNETCORE_ENVIRONMENT=Development
export ASPNETCORE_URLS=http://127.0.0.1:8080
# Checkout does not call the AI provider. Supply local placeholders for DI startup.
export Chatbot__ActiveProvider=Gemini
export Chatbot__Gemini__ApiKey=local-loadtest-unused
export Chatbot__Gemini__Model=local-loadtest-unused
export Chatbot__Gemini__EmbeddingModel=local-loadtest-unused
export DOTNET_CLI_TELEMETRY_OPTOUT=1

artifacts="$backend_dir/.artifacts/loadtest"
mkdir -p "$artifacts"
docker compose -p shoestore-loadtest up -d --wait database
dotnet test test/ShoeStore.Tests/ShoeStore.Tests.csproj -c Release --verbosity quiet > "$artifacts/unit-tests.log" 2>&1 || { tail -60 "$artifacts/unit-tests.log"; exit 1; }
tail -2 "$artifacts/unit-tests.log"
dotnet build tools/ShoeStore.LoadTesting/ShoeStore.LoadTesting.csproj -c Release --verbosity quiet > "$artifacts/tool-build.log" 2>&1 || { tail -40 "$artifacts/tool-build.log"; exit 1; }
utility=(dotnet "$backend_dir/tools/ShoeStore.LoadTesting/bin/Release/net10.0/ShoeStore.LoadTesting.dll")
"${utility[@]}" seed "$artifacts"
if [[ "$mode" == seed ]]; then exit 0; fi

# Refuse to test a different API already listening on this port.
if curl -s --max-time 2 http://127.0.0.1:8080/ >/dev/null; then
  echo 'Port 8080 is occupied. Stop that API before running this dedicated local test.' >&2; exit 1
fi
dotnet build src/ShoeStore.Api/ShoeStore.Api.csproj -c Release --verbosity quiet > "$artifacts/api-build.log" 2>&1 || { tail -40 "$artifacts/api-build.log"; exit 1; }
dotnet "$backend_dir/src/ShoeStore.Api/bin/Release/net10.0/ShoeStore.Api.dll" > "$artifacts/api.log" 2>&1 &
api_pid=$!
trap 'kill "$api_pid" 2>/dev/null || true; wait "$api_pid" 2>/dev/null || true' EXIT
ready=0
for (( attempt=0; attempt<60; attempt++ )); do
  if curl -fsS --max-time 2 http://127.0.0.1:8080/openapi/v1.json >/dev/null 2>&1; then ready=1; break; fi
  if ! kill -0 "$api_pid" 2>/dev/null; then break; fi
  sleep 1
done
if (( ready == 0 )); then tail -60 "$artifacts/api.log"; exit 1; fi

modes=("$mode")
if [[ "$mode" == all ]]; then modes=(last-pair hot-sku mixed); fi
result=0
for scenario in "${modes[@]}"; do
  output="$artifacts/$(date -u +%Y%m%dT%H%M%SZ)-$scenario-$RANDOM"
  "${utility[@]}" prepare "$output" "$scenario" "$users"
  k6_status=0
  script=test/ShoeStore.Tests/LoadTesting/checkout-race.js
  if [[ "$scenario" == journey ]]; then script=test/ShoeStore.Tests/LoadTesting/loadtest.js; fi
  k6 run -e FIXTURE_DIR="$output" -e BASE_URL=http://127.0.0.1:8080 \
    "$script" || k6_status=$?
  audit_status=0
  "${utility[@]}" verify "$output" || audit_status=$?
  if (( k6_status != 0 || audit_status != 0 )); then result=1; fi
  echo "Report: $output/audit.json"
done
exit "$result"
