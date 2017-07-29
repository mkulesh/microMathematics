#!/bin/bash

echo Converting ${1}
if [[ ${1} == *"flb_"* ]]
then
  echo "Using floating button size";
  ./convert_file.sh ./${1} mdpi 56 
  ./convert_file.sh ./${1} hdpi 84 
  ./convert_file.sh ./${1} xhdpi 112
  ./convert_file.sh ./${1} xxhdpi 168
else
  echo "Using normal button size";
  ./convert_file.sh ./${1} mdpi 24 
  ./convert_file.sh ./${1} hdpi 36 
  ./convert_file.sh ./${1} xhdpi 48 
  ./convert_file.sh ./${1} xxhdpi 72
  ./convert_file.sh ./${1} xxxhdpi 96
fi


