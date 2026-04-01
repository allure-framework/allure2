import { defineConfig, devices } from "playwright/test";

export default defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: process.env.CI
    ? [["github"], ["html", { open: "never" }]]
    : [["list"], ["html", { open: "never" }]],
  globalSetup: "./scripts/prepare-playwright-report.mts",
  webServer: {
    command: "tsx ./scripts/e2e-static-server.mts",
    reuseExistingServer: !process.env.CI,
    timeout: 30_000,
    url: "http://127.0.0.1:4173/health",
  },
  use: {
    viewport: { width: 1440, height: 960 },
    screenshot: "only-on-failure",
    trace: "on-first-retry",
    video: "retain-on-failure",
    ...(process.env.PLAYWRIGHT_EXECUTABLE_PATH
      ? { launchOptions: { executablePath: process.env.PLAYWRIGHT_EXECUTABLE_PATH } }
      : {}),
  },
  expect: {
    timeout: 10_000,
  },
  projects: [
    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
      },
    },
  ],
});
