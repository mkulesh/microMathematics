#!/bin/bash

echo Converting ic_launcher
  ./convert_file.sh ./ic_launcher.svg mdpi 48
  ./convert_file.sh ./ic_launcher.svg hdpi 72 
  ./convert_file.sh ./ic_launcher.svg xhdpi 96 
  ./convert_file.sh ./ic_launcher.svg xxhdpi 144
  ./convert_file.sh ./ic_launcher.svg xxxhdpi 192
echo Converting ic_launcher_adapt
  ./convert_file.sh ./ic_launcher_adapt.svg mdpi 108
  ./convert_file.sh ./ic_launcher_adapt.svg hdpi 162 
  ./convert_file.sh ./ic_launcher_adapt.svg xhdpi 216 
  ./convert_file.sh ./ic_launcher_adapt.svg xxhdpi 324
  ./convert_file.sh ./ic_launcher_adapt.svg xxxhdpi 432

