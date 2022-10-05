#!/bin/bash

NAME1=${1%.svg}
TARGET="../../app/src/main/res/drawable-"${2}"/"${NAME1:2}".png"
echo Converting to ${1} to png and plains SVG
inkscape --export-type=png -w ${3} -h ${3} ${1} -o ${TARGET}