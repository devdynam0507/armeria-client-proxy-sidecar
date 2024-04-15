#!/bin/bash

./gradlew build
docker build -t engagemoment-proxy-sidecar -f ./docker/Dockerfile .

