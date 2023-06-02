#!/bin/bash

./gradlew build

tag=$(date +"%Y-%m-%dT%H-%M-%S")
image_name="sanchoys/bewired:$tag"
docker build -t "$image_name" .

docker tag "$image_name" "sanchoys/bewired:latest"

docker-compose up -d
