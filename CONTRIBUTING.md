# Contributing to this project
(We took some of this from Atom's GitHub as an starting point)

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

The following is a set of guidelines for this project, which are hosted in the [Null Bucket organuzation (null0)](https://github.com/nullbucket) on GitHub. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

### Your First Code Contribution

Unsure where to begin contributing? You can start by looking through these `beginner` and `help-wanted` issues:

* [Beginner issues][beginner] - issues which should only require a few lines of code, and a test or two.
* [Help wanted issues][help-wanted] - issues which should be a bit more involved than `beginner` issues.

We have yest tried accepting forks but are open to it.

#### Local development

### Pull Requests (We took some of this from Atom's GitHub as an example)

* Fill in [the required template](PULL_REQUEST_TEMPLATE.md)
* Do not include issue numbers in the PR title
* Include screenshots and animated GIFs in your pull request whenever possible.
* Document new code based on the [Documentation Styleguide](#documentation-styleguide)
* End all files with a newline


## QuickStart

Right now main contributers are within the project, using branches. We do prefer that pushes be pgp verified (signed).

This is an example of a simple session:

1. Do your work on a local branch in Git like this:
git checkout -b mybranch
git status
git add -A
git status
git commit -m "comment w/github ref like #9"
git push -u origin mybranch

2. Submit a pull request for your branch in GitHub

3. The reviewer will merge your changes and probably delete your branch right away.

4. After that happens, switch your local back to master and delete your branch:
git checkout master
git pull --rebase
git branch -d mybranch

## Styleguides

### Git Commit Messages (We took some of this from Atom's GitHub as an example)

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line
* When only changing documentation, include `[ci skip]` in the commit title
* Consider starting the commit message with an applicable emoji:
    * :art: `:art:` when improving the format/structure of the code
    * :racehorse: `:racehorse:` when improving performance
    * :non-potable_water: `:non-potable_water:` when plugging memory leaks
    * :memo: `:memo:` when writing docs
    * :penguin: `:penguin:` when fixing something on Linux
    * :apple: `:apple:` when fixing something on macOS
    * :checkered_flag: `:checkered_flag:` when fixing something on Windows
    * :bug: `:bug:` when fixing a bug
    * :fire: `:fire:` when removing code or files
    * :green_heart: `:green_heart:` when fixing the CI build
    * :white_check_mark: `:white_check_mark:` when adding tests
    * :lock: `:lock:` when dealing with security
    * :arrow_up: `:arrow_up:` when upgrading dependencies
    * :arrow_down: `:arrow_down:` when downgrading dependencies
    * :shirt: `:shirt:` when removing linter warnings



