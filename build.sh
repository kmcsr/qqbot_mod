#!/bin/sh

cd $(dirname $0)

echo
echo "cleaning..."
./gradlew clean || exit $?
echo
echo "building..."
./gradlew build || exit $?

echo
echo "copying targets..."
rm -rf ./target
mkdir -p ./target
cp ./build/libs/* ./target/
