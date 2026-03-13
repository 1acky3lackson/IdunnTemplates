#!/usr/bin/env bash
set -e

rm -rf out
mkdir -p out

if [ -d run ]; then
  echo "==> Copying conf files to output directory"
  cp -r run/* out/
fi

if [ "$ENV_TYPE" != "PROD" ]; then
  echo "==> Building debug binary (Delve compatible)"

  CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
  go build \
    -gcflags="all=-N -l" \
    -o out/main \
    main.go
else
  echo "==> Building prod binary"

  CGO_ENABLED=0 GOOS=linux \
  go build \
    -ldflags="-s -w" \
    -o out/main \
    main.go
fi