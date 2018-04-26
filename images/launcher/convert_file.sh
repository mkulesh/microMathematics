#!/bin/bash

NAME1=${1%.svg}
TARGET="../../app/src/main/res/mipmap-"${2}"/"${NAME1:2}".png"
echo Converting to ${1} to png and plains SVG
inkscape --vacuum-defs --export-plain-svg=${1} ${1}
inkscape --export-png ${TARGET} -w ${3} -h ${3} ${1}
