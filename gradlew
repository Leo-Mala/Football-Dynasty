#!/bin/sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java "$APP_HOME/gradle/wrapper-bootstrap/GradleBootstrap.java" "$@"
