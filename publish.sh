#!/usr/bin/env bash

./gradlew :clean
./gradlew :vcalendar:assemble :vcalendar:build
./gradlew :vcalendar:androidSources :vcalendar:androidJavadoc :vcalendar:androidJavadocJar :vcalendar:generatePomFileForVcalendarPublication
./gradlew :vcalendar:artifactoryPublish --info
#./gradlew :vcalendar:assemble :vcalendar:generatePomFileForVcalendarPublication :vcalendar:androidSourcesJar :vcalendar:androidJavadoc :vcalendar:androidJavadocJar
#./gradlew :vcalendar:publishToMavenLocal
#./gradlew :vcalendar:bintrayUpload --info