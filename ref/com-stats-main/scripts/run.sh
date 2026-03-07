#!/bin/sh
set -e

chmod +x ./main

if [ "$ENV_TYPE" != "PROD" ]; then
  echo "==> Starting with Delve (headless, attachable)"
  exec dlv exec /app/main \
    --headless \
    --listen=:2345 \
    --api-version=2 \
    --accept-multiclient \
    --continue \
    -- "$@"
else
  echo "==> Starting normally"
  exec /app/main "$@"
fi
