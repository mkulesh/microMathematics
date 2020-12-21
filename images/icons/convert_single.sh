#!/bin/bash

echo Converting ${1}
  ./convert_file.sh ./${1} mdpi 24
  ./convert_file.sh ./${1} hdpi 36 
  ./convert_file.sh ./${1} xhdpi 48 
  ./convert_file.sh ./${1} xxhdpi 72
  ./convert_file.sh ./${1} xxxhdpi 96


