#!/bin/sh

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <version number>"
    exit 1
fi

root=$(pwd)

# This will fail silently (but deadly) if there's ever a modloader that's lexicographically before "core"
find . -maxdepth 3 -type f -name 'build.gradle' | sed -r 's|/[^/]+$||' | sort \
| while read -r folder; do
    echo -e "\n\e[1;104m Building $folder... \e[0m"
    cd "$folder" || exit 1
    
    # Older Gradle versions are naughty and consume stdin for some reason
    ./gradlew -Pversion=$1 build < /dev/null

    cd "$root" || exit 1
done
