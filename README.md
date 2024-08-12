# Allure Report-IL (Linux)

## Structure
```
# Создаем страницу Login
├── 📁 allure-commandline/          # build prod file
├── 📁 allure-generator/            # generator web
├── 📁 allure-jira-commons/         #
├── 📁 allure-plugin-api/           # 
├── 📁 plugins/                     #
└──  ...                            #
```

## BUILD ALLURE
### Prod build Allure project
```bash
# ~SNAPSHOT
./gradlew build buildDeb buildRpm
# My version prod
./gradlew buildDeb -Pversion=2.30.0-1-il
```

### Install Allure
```bash
# allure2/allure-commandline/build/distributions
sudo dpkg -i allure_2.30.0~il-1_all.deb
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
- **NODE js**: 20.16.0
- **Apache Maven** - 3.6.3
- **Gradle** - 8.5
- **Kubuntu** - 22.04

#### Version VS code plugins
- **Test Runner for Java** - 0.42.0
- **Project Manager for Java** - 0.24.0
- **Maven for Java** - 0.44.0
- **Gradle for Java** - 3.16.2
- **Extension Pack for Java** - 0.28.0
- **Debugger for Java** - 0.58.0

#### Change version JAVA
```bash
# deafault version
sudo ln -svf /home/jdk-17.0.6+10/bin/java /usr/bin/java
# version
java -version
# tag version
sudo ln -svf /home/jdk-17.0.6+10/bin/java /usr/bin/java17
# version
java -version17
```

#### Add code
1. #IL_add **Добавим дату**