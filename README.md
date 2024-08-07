# Allure Report-IL (Linux)

## BUILD ALLURE
### Build Allure project
```bash
# ~SNAPSHOT
./gradlew build buildDeb buildRpm
# My version
./gradlew build buildRpm buildDeb -Pversion=0.0.1-il
```

### Install Allure
```bash
# allure2/allure-commandline/build/distributions
sudo dpkg -i allure_0.0.1~il-1_all.deb
```

### Version Allure
```bash
allure --version
```

## START ALLURE
### Allure generate report
```bash
allure generate logs -c - report
```

### Allure server
```bash
allure open -p 9000 allure-report
```

#### Version
- **JAVA**: "17.0.6" 2023-01-17
- **Apache Maven** - 3.6.3

#### Version VS code plugins
- **Test Runner for Java** - 0.42.0
- **Project Manager for Java** - 0.24.0
- **Maven for Java** - 0.44.0
- **Gradle for Java** - 3.16.2
- **Extension Pack for Java** - 0.28.0
- **Debugger for Java** - 0.58.0