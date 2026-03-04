#!/bin/bash
# Gradle Cache Cleaner - Run this when you get metadata.bin or cache corruption errors

echo "🧹 Cleaning Gradle caches..."

# Kill any running Gradle daemon
./gradlew --stop 2>/dev/null || true

echo "Removing corrupted cache directories..."

# Remove all potentially corrupted caches (keeps downloaded dependencies in caches/modules-2)
rm -rf ~/.gradle/caches/transforms/
rm -rf ~/.gradle/caches/build-cache-1/
rm -rf ~/.gradle/caches/jars-9/
rm -rf ~/.gradle/caches/groovy-dsl/
rm -rf ~/.gradle/caches/8.*/
rm -rf .gradle/

echo "✅ Cache cleaned. Rebuilding with --no-build-cache..."

# Build without cache to regenerate clean state
./gradlew :app:assembleDebug --no-build-cache

echo "🎉 Done! Future builds can use cache again."
