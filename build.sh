# del build
rm -rf build
rm -rf ./allure-commandline/build
rm -rf ./allure-generator/build
rm -rf ./allure-jira-commons/build
rm -rf ./allure-plugin-api/build
rm -rf allure-report

./gradlew build buildRpm buildDeb -Pversion=2.30.0-il

cd ./allure-commandline/build/distributions
sudo dpkg -i allure_2.30.0~il-1_all.deb
allure --version

cd ../../../
allure generate logs -c - report
allure open -p 9000 allure-report