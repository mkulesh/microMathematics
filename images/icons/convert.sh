#!/bin/sh

find . -name "*.svg" -exec ./convert_single.sh {} \;

