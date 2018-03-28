#!/usr/bin/env bash

./gradlew :clean
./gradlew :vcalendar:assemble
./gradlew :vcalendar:generatePomFileForVcalendarPublication
./gradlew :vcalendar:bintrayUpload