#!/usr/bin/env bash
EpochTag="$(date +%s)"

cd ~/external/Similarity_Exercise_Gerard/
cp -r src infrastructure/build
cp -r data infrastructure/build
cp -r pom.xml infrastructure/build

# DOCKER BUILD
cd ~/external/Similarity_Exercise_Gerard/infrastructure/build
docker build -f Dockerfile \
  -t kmer:$EpochTag \
  --force-rm \
  --no-cache .
#docker push kmer:$EpochTag

cd ~/external/Similarity_Exercise_Gerard/