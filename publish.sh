#!/usr/bin/env bash

./gradlew :clean
./gradlew :vcalendar:assemble :vcalendar:generatePomFileForVcalendarPublication :vcalendar:androidSourcesJar :vcalendar:androidJavadoc :vcalendar:androidJavadocJar
./gradlew :vcalendar:publishToMavenLocal
#./gradlew :vcalendar:bintrayUpload --info