#!/usr/bin/env bash
# Uso: ./ejecutar_jmeter.sh [host] [puerto]   (por defecto localhost 8080)
# Requiere JMeter 5.6+ en el PATH (jmeter -v) y el backend corriendo.
set -e
HOST=${1:-localhost}; PORT=${2:-8080}
OUT="resultados_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$OUT"
jmeter -n -t santandar_plan.jmx \
  -Jhost=$HOST -Jport=$PORT \
  -JadminEmail=${ADMIN_EMAIL:-admin@santandar.test} -JadminPass=${ADMIN_PASS:-Admin12345} \
  -Jusers=${USERS:-100} -Jramp=${RAMP:-30} -Jloops=${LOOPS:-10} \
  -l "$OUT/resultados.jtl" -e -o "$OUT/dashboard"
echo "Dashboard HTML: $OUT/dashboard/index.html"
