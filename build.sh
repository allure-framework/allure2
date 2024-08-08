# del build

# rm -rf build
# rm -rf ./allure-commandline/build
# rm -rf ./allure-generator/build
# rm -rf ./allure-generator/node_modules
# rm -rf ./allure-generator/.gradle
# rm -rf ./allure-jira-commons/build
# rm -rf ./allure-plugin-api/build
rm -rf allure-report

# build deb
# ./gradlew build buildRpm buildDeb -Pversion=2.30.0-1-il
./gradlew buildDeb -Pversion=2.30.0-1-il

# install allure (LINUX)
cd ./allure-commandline/build/distributions
sudo dpkg -i allure_2.30.0~1~il-1_all.deb
allure --version

# Server Allure
cd ../../../
allure generate logs -c - report
allure open -p 9001 allure-report