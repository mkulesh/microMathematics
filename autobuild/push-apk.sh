#!/bin/sh

APK_VERSION=2.20.0

echo "Generated APK"
pwd; ls -l ./app/build/outputs/apk

# Checkout autobuild branch
cd ..
git clone https://github.com/mkulesh/microMathematics.git --branch autobuild --single-branch microMathematics_autobuild
cd microMathematics_autobuild

# Copy newly created APK into the target directory
mv ../microMathematics/app/build/outputs/apk/debug/microMathematics-v${APK_VERSION}-debug.apk ./autobuild
echo "Target APK"
pwd; ls -l ./autobuild

# Setup git for commit and push
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis CI"
git remote add origin-master https://${AUTOBUILD_TOKEN}@github.com/mkulesh/microMathematics > /dev/null 2>&1
git add ./autobuild/microMathematics-v${APK_VERSION}-debug.apk

# We don’t want to run a build for a this commit in order to avoid circular builds: 
# add [ci skip] to the git commit message
git commit --message "Snapshot autobuild N.$TRAVIS_BUILD_NUMBER [ci skip]"
git push origin-master
