#!/usr/bin/env bash

echo "cleaning up..."
rm -rf ./release
rm -rf  ./.cp-cache

echo "uberjaring..."
clj -A:uberjar

echo "making release..."
cp config.edn ./release/
