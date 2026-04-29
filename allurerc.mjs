const { ALLURE_SERVICE_TOKEN } = process.env;

const allureService = ALLURE_SERVICE_TOKEN
    ? {
        accessToken: ALLURE_SERVICE_TOKEN,
    }
    : undefined;

export default {
    name: "Allure Report",
    output: "./build/allure-report",
    plugins: {
        awesome: {
            options: {
                publish: true,
            },
        },
    },
    ...(allureService ? { allureService } : {}),
};
