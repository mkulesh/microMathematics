# Directory microMathematics/autobuild

This directory contains an 'unstable' and unsigned APK file that is build automaticcaly by [Travis CI](https://travis-ci.org/mkulesh/microMathematics) after each commit.

## How does it work?

* In the root directory of this repository, there is a file [.travis.yml](https://raw.githubusercontent.com/mkulesh/microMathematics/master/.travis.yml) that contains build rules for Travis

* Travis perform the checkpot of the repository in the "detached HEAD" mode; i.e the working tree on the Travis CI virtual machine is restricted for pushing anythings into the github master repositiory. The "detached HEAD" mode only allows to push files into a branch. However, we want to commit the snapshot APK here, into the master branch. In order to achieve it, we first need to checkout the master branch that is done in the `install` section of `.travis.yml` file. The install section also prepares the script [push-apk.sh](https://raw.githubusercontent.com/mkulesh/microMathematics/master/autobuild/push-apk.sh) from this directory for execution
```
install:
  - git checkout master
  - chmod +x ./autobuild/push-apk.sh
```

* After the build is successfully finished, we can push newly created APK back to the master branch into this directory. It is done by executing of `push-apk.sh` script:
```
after_success:
  - ./autobuild/push-apk.sh
```

* In this script, we first copy the newly created APK from gradle output directory `./app/build/outputs/apk` into this directory

* Next, we prepare git for push into a remote repository using github personal accesse token as a "Travis CI" user:
```
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis CI"
git remote add origin-master https://${AUTOBUILD_TOKEN}@github.com/mkulesh/microMathematics
```

* The private personal accesse token is generated once [here](https://github.com/settings/tokens) and needs to be stored somewhere (but not in the repository).

* The token can be encripted using `travis` tool and attached to the `.travis.yml` file. Just install Travis gem (`sudo gem install travis`) and run following command in the root directory of the repository:
```
echo AUTOBUILD_TOKEN=<generated token> | travis encrypt --add -r mkulesh/microMathematics
```

* This tool expands `.travis.yml` by new section that contains encripted variable `${AUTOBUILD_TOKEN}`
```
env:
  global:
    secure: ERsltm1IX7A...
```

* After performing `git remote add origin-master https://${AUTOBUILD_TOKEN}@github.com/mkulesh/microMathematics`, we are autorizesd in the github for read/write operations and can finally commit and push the APK. Here, we don’t want to run a build for a this commit in order to avoid circular builds. To achieve it, travis needs `[ci skip]` label in the commit message:
```
git commit --message "Snapshot autobuild N.$TRAVIS_BUILD_NUMBER [ci skip]"
git push origin-master
```

See also [How to set up TravisCI for projects that push back to github](https://gist.github.com/willprice/e07efd73fb7f13f917ea).
