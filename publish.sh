#!/usr/bin/env bash

./gradlew :clean
./gradlew :vcalendar:assemble
./gradlew :vcalendar:generatePomFileForVcalendarPublication
./gradlew :vcalendar:androidJavadocJar
./gradlew :vcalendar:androidJavadocJar
./gradlew :vcalendar:publishToMavenLocal
./gradlew :vcalendar:bintrayUpload