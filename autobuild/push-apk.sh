#!/bin/csh

# Copy newly created APK to target directory
echo "Generated APK"
ls -l ./app/build/outputs/apk
mv ./app/build/outputs/apk/microMathematics-v2.15.4a.apk ./autobuild/
echo "Target APK"
ls -l ./autobuild

# Setup git for commit and push
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis CI"
git remote add origin-master https://${AUTOBUILD_TOKEN}@github.com/mkulesh/microMathematics
git add ./autobuild/microMathematics-v2.15.4a.apk

# We don’t want to run a build for a this commit in urder to avoid circular builds: 
# add [ci skip] to the git commit message
git commit --message "Snapshot autobuild [ci skip]"
git push origin-master

