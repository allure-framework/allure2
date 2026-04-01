export const REPORT_MODES = {
  SINGLE_FILE: "single-file",
  DIRECTORY: "directory",
} as const;

export type ReportMode = (typeof REPORT_MODES)[keyof typeof REPORT_MODES];

const sharedTopLevelTabs = [
  "Overview",
  "Categories",
  "Suites",
  "Graphs",
  "Timeline",
  "Behaviors",
  "Packages",
] as const;

export const fixtures = {
  newDemo: {
    name: "new-demo",
    topLevelTabs: sharedTopLevelTabs,
    cases: {
      failedPullRequest: "Creating new issue for authorized user",
      passedPullRequest: "Deleting existing issue for authorized user",
      flakyNewFailedIssue: "Adding note to advertisement",
      retriesStatusChangeIssue: "Closing new issue for authorized user",
      newPassedIssue: "Deleting existing issue for authorized user",
    },
    behaviors: {
      feature: "Pull Requests",
      story: "Create new pull request",
    },
    categories: {
      group: "Product defects",
      statusMessage: "Element not found",
    },
    packages: {
      root: "io.qameta.allure",
      className: "PullRequestsWebTest",
    },
    widgets: {
      behaviors: "Features by stories",
      categories: "Categories",
      environment: "Environment",
      executors: "Executors",
      suites: "Suites",
      summary: "Allure Report",
      trend: "Trend",
    },
    graphs: [
      "Status",
      "Severity",
      "Duration",
      "Trend",
      "Duration trend",
      "Retries trend",
      "Categories trend",
    ],
    htmlAttachmentName: "Page",
    htmlAttachmentStep: "Open pull requests page `allure-framework/allure2`",
  },
  allure2: {
    name: "allure2",
    caseName: "attachmentsInStep",
    attachments: {
      csv: "CSV attachment",
      json: "JSON attachment",
      png: "PNG attachment",
      svg: "SVG attachment",
      tsv: "TSV attachment",
      uri: "URI attachment",
      video: "WEMB attachment",
      xml: "XML attachment",
    },
    steps: {
      csv: "Attach CSV file",
      json: "Attach JSON file",
      png: "Attach PNG file",
      svg: "Attach SVG file",
      tsv: "Attach TSV file",
      uri: "Attach URI file",
      video: "Attach Video file",
      xml: "Attach XML file",
    },
  },
  screenDiff: {
    name: "screen-diff",
    caseName: "buttons",
    blockTitle: "Screen Diff",
  },
  playwrightTrace: {
    name: "playwright-trace",
    caseName: "opens playwright trace attachment",
    attachmentName: "Playwright trace",
    stepName: "Record browser trace",
  },
} as const;
