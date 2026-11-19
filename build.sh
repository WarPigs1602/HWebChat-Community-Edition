#!/bin/sh
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$root"

if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven (mvn) is required." >&2
    exit 1
fi

lock="$root/.hwebchat-build.lock"
if ! mkdir "$lock" 2>/dev/null; then
    echo "Cannot acquire build lock: $lock (another build may be running)." >&2
    exit 1
fi
tmp=
cleanup() {
    if [ -n "$tmp" ]; then
        rm -f -- "$tmp"
    fi
    rmdir -- "$lock" || echo "Cannot remove build lock: $lock" >&2
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
trap 'exit 129' HUP

version=$(awk '
    /<artifactId>HWebChat<\/artifactId>/ && !found { found = 1; next }
    found && /<version>/ {
        line = $0
        sub(/^.*<version>/, "", line)
        sub(/<\/version>.*$/, "", line)
        print line
        exit
    }
' pom.xml)
if ! printf '%s\n' "$version" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+\.(0|[1-9][0-9]*)$'; then
    echo "Invalid project version in pom.xml: $version" >&2
    exit 1
fi

build=${version##*.}
if [ "${#build}" -gt 10 ] || [ "$build" -ge 2147483647 ]; then
    echo "Build number is too large: $build" >&2
    exit 1
fi
next="${version%.*}.$((build + 1))"
tmp=$(mktemp "$root/.pom.xml.XXXXXX")
cp -p pom.xml "$tmp"
awk -v version="$next" '
    /<artifactId>HWebChat<\/artifactId>/ && !found { found = 1; print; next }
    found == 1 && /<version>/ {
        sub(/<version>[^<]*<\/version>/, "<version>" version "</version>")
        found = 2
    }
    { print }
    END { if (found != 2) exit 1 }
' pom.xml > "$tmp"
mv -- "$tmp" pom.xml
tmp=

echo "Building HWebChat $next"
mvn "$@" clean package
