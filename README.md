# Allure Report (Linux)

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
sudo dpkg -i allure_.deb
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