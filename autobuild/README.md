# Directory microMathematics/autobuild

![Page logo](https://github.com/mkulesh/microMathematics/blob/master/autobuild/shema.svg)

This directory contains a script that allows us to collect 'unstable' and unsigned APK file that is build automatically by [Travis CI](https://travis-ci.org/mkulesh/microMathematics) after each commit.

## How does it work?

* The repository contains a [special branch](https://github.com/mkulesh/microMathematics/tree/autobuild) where only one APK file is stored: `autobuild`

* In the root directory of this repository, there is a file [.travis.yml](https://raw.githubusercontent.com/mkulesh/microMathematics/master/.travis.yml) that contains build rules for Travis

* After the build is successfully finished, we can push newly created APK back to the `autobuild` branch into this directory. It is done by executing of `push-apk.sh` script:
```
after_success:
  - ./autobuild/push-apk.sh
```

* In this script, we first clone `autobuild` branch into a separate directory:
```
git clone https://github.com/mkulesh/microMathematics.git --branch autobuild --single-branch microMathematics_autobuild
cd microMathematics_autobuild
```

* Next, we copy the newly created APK from gradle output directory `./app/build/outputs/apk` into this directory of the `autobuild` branch
```
mv ../microMathematics/app/build/outputs/apk/microMathematics-v2.15.5.apk ./autobuild
```

* Next, we prepare git for push into a remote repository using github personal access token as a "Travis CI" user:
```
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis CI"
git remote add origin-master https://${AUTOBUILD_TOKEN}@github.com/mkulesh/microMathematics
```

* The private personal access token is generated once [here](https://github.com/settings/tokens) and needs to be stored somewhere (but not in the repository).

* The token can be encrypted using `travis` tool and attached to the `.travis.yml` file. Just install Travis gem (`sudo gem install travis`) and run following command in the root directory of the repository:
```
echo AUTOBUILD_TOKEN=<generated token> | travis encrypt --add -r mkulesh/microMathematics
```

* This tool expands `.travis.yml` by new section that contains encrypted variable `${AUTOBUILD_TOKEN}`
```
env:
  global:
    secure: ERsltm1IX7A...
```

* After performing `git remote add origin-master https://${AUTOBUILD_TOKEN}@github.com/mkulesh/microMathematics`, we are in the local branch `autobuild`, authorized in the github for read/write operations and can finally commit and push the APK. Here, we don’t want to run a build for a this commit in order to avoid circular builds. To achieve it, travis needs `[ci skip]` label in the commit message:
```
git commit --message "Snapshot autobuild N.$TRAVIS_BUILD_NUMBER [ci skip]"
git push origin-master
```

See also [How to set up TravisCI for projects that push back to github](https://gist.github.com/willprice/e07efd73fb7f13f917ea).
The free icons for the page logo are provided by [visualpharm.com](https://visualpharm.com/free-icons).
