#!/usr/bin/env bash

set -e

chmod +x gradlew
chmod +x scripts/publish.sh

./scripts/publish.sh -p vcalendar -f release