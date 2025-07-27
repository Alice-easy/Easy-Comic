#!/bin/bash

# This script increments the build number in the pubspec.yaml file.

PUBSPEC_FILE="Easy-Comic/pubspec.yaml"

if [ ! -f "$PUBSPEC_FILE" ]; then
    echo "Error: pubspec.yaml not found at '$PUBSPEC_FILE'"
    exit 1
fi

# Read the current version line
VERSION_LINE=$(grep "version:" "$PUBSPEC_FILE")

# Extract the version and build number
VERSION=$(echo "$VERSION_LINE" | sed -n 's/version: \(.*\)+\(.*\)/\1/p')
BUILD_NUMBER=$(echo "$VERSION_LINE" | sed -n 's/version: \(.*\)+\(.*\)/\2/p')

if [ -z "$BUILD_NUMBER" ]; then
    echo "Error: Could not parse build number from '$VERSION_LINE'"
    exit 1
fi

# Increment the build number
NEW_BUILD_NUMBER=$((BUILD_NUMBER + 1))

# Create the new version string
NEW_VERSION_STRING="version: $VERSION+$NEW_BUILD_NUMBER"

# Replace the old version line with the new one
sed -i "s/$VERSION_LINE/$NEW_VERSION_STRING/" "$PUBSPEC_FILE"

echo "Version bumped to: $NEW_VERSION_STRING"