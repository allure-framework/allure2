const { ALLURE_SERVICE_TOKEN } = process.env;

const allureService = ALLURE_SERVICE_TOKEN
    ? {
        accessToken: ALLURE_SERVICE_TOKEN,
        legacy: true,
    }
    : undefined;

export default {
    name: "Allure 2",
    output: "./build/allure-report",
    plugins: {
        awesome: {
            options: {
                groupBy: ["module", "parentSuite", "suite", "subSuite"],
                publish: true,
            },
        },
    },
    ...(allureService ? { allureService } : {}),
};
