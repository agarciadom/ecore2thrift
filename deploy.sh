#!/bin/bash

# Exit immediately if something goes wrong
set -o errexit

SOURCE_BRANCH="master"
TARGET_BRANCH="gh-pages"

function doCompile {
  mvn --quiet install
}

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "$SOURCE_BRANCH" ]; then
    echo "Skipping deploy; just doing a build."
    doCompile
    exit 0
fi

# Save some useful information
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
SHA=`git rev-parse --verify HEAD`

# Clone the existing gh-pages for this repo into out/
# Create a new empty branch if gh-pages doesn't exist yet (should only happen on first deply)
git clone $REPO out
cd out
git checkout $TARGET_BRANCH || git checkout --orphan $TARGET_BRANCH
cd ..

# Run our compile script
doCompile

# Indicate clearly that this commit comes from Travis
cd out
git config user.name "Travis CI"
git config user.email "a.garcia-dominguez@aston.ac.uk"

# If the tip comes from Travis, amend it. Otherwise, add a new commit.
rm -rf ecore2thrift-updates
cp -r ../uk.ac.york.mondo.ecore2thrift.updatesite/target/repository ecore2thrift-updates
git add --all .
if git log --format=%an HEAD~.. | grep -q "Travis CI"; then
    COMMIT_FLAGS="--amend"
fi
git commit $COMMIT_FLAGS -am "Build update site"

# Decrypt SSH key
ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in deploy_key.enc -out deploy_key -d
chmod 600 deploy_key
eval `ssh-agent -s`
ssh-add deploy_key

# Force push to the gh-pages branch
git push --force $SSH_REPO $TARGET_BRANCH
