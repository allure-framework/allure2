#### Contribute to the Allure Framework
In order to build Allure, you need:
* [JDK](http://java.sun.com/) 1.7 or above (tested with Oracle JDK on different platforms) to compile and run.
* [Maven](http://maven.apache.org/) 3.0.4 or above to build.
* [PhantomJS](http://phantomjs.org/) 1.9 or above to run browser tests.

When editing the code, please configure your preferred IDE to use UTF-8 for all files and 4-space indentation. To build the project, you need to run:
```bash
$ mvn clean install
```

#### Pull requests 

* Create separate PR for each feature/bug/improvement.
* PR title should be as clear as possible. In the case of adding new feature PR should contain detailed description. An example https://github.com/allure-framework/allure-core/pull/362 
* PR should reference an issue An example: "Added @Issues annotation support (fixes #360)".
* Squash your commits into one. See http://blog.steveklabnik.com/posts/2012-11-08-how-to-squash-commits-in-a-github-pull-request and http://stackoverflow.com/questions/14534397/how-to-squash-all-my-commits-into-one-github

In addition, you should know that we declare minor versions of Allure fully compatible, so the produced XML format shouldn't change in your pull request. If you would like to introduce changes to the XML, please [contact us](mailto:allure@yandex-team.ru) before implementing.
