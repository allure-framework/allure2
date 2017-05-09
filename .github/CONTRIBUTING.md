## Contributing 

We love pull requests from everyone. 

Fork, then clone the repo:

```bash
$ git clone git@github.com:your-username/allure2.git
```

Then build the project (build requires JDK 1.8 or higher):

```bash
$ ./gradlew build
```

Make your change. Add tests for your change. Make sure all the tests pass:

```bash
$ ./gradlew test
```

Push your fork and submit a pull request. 

### Webpack dev server

To start dev server you can run

```bash
$ ./gradlew build //only for a first time
$ ./gradlew dev
```

And then open `http://localhost:3000`

Also you can choose different demo data using `results` parameter:

```bash
$ ./gradlew dev -Presults=allure2
```