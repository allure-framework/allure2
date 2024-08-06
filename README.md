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