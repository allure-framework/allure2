# Allure Report

## Build Allure project
```bash
 ./gradlew build buildDeb buildRpm
```

## Install Allure
```bash
# allure2/allure-commandline/build/distributions
sudo dpkg -i allure_.deb
```

# Version Allure
```bash
allure --version
```

# Allure generate report
```bash
allure generate logs -c - report
```

# Allure server
```bash
allure open -p 9000 allure-report
```