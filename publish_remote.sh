#!/usr/bin/env bash

set -e

export ARTIFACTORY_USER=$ARTIFACTORY_USER_ES
export ARTIFACTORY_PASS=$ARTIFACTORY_KEY_ES

chmod +x gradlew
chmod +x scripts/publish.sh

./scripts/publish.sh -p vcalendar -f release -t artifactory