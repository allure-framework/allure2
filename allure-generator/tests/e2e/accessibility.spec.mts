import { createRequire } from "node:module";
import { attachment, step } from "allure-js-commons";
import { expect, test, type Page } from "playwright/test";
import { fixtures, REPORT_MODES } from "./support/fixtures.mts";
import { openCaseFromTree, openReport } from "./support/report.mts";
import { stepLocator } from "./support/ui.mts";

const require = createRequire(import.meta.url);
const axeScriptPath = require.resolve("axe-core/axe.min.js");

const uiDemo = fixtures.uiDemo;

const AXE_OPTIONS = {
  runOnly: {
    type: "tag",
    values: ["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"],
  },
  resultTypes: ["violations"],
};

type AccessibilityTargetName =
  | "attachment-modal"
  | "categories"
  | "graph"
  | "overview"
  | "suites-selected-test"
  | "timeline";

interface AccessibilityTarget {
  name: AccessibilityTargetName;
  open: (page: Page) => Promise<void>;
}

interface AxeNode {
  target: string[];
  html: string;
  failureSummary?: string;
}

interface AxeViolation {
  id: string;
  impact: string | null;
  help: string;
  helpUrl: string;
  nodes: AxeNode[];
}

interface AxeResults {
  violations: AxeViolation[];
}

interface AxeRunner {
  run: (context: Document, options: typeof AXE_OPTIONS) => Promise<AxeResults>;
}

const scanTargets: AccessibilityTarget[] = [
  {
    name: "overview",
    open: async (page) => {
      await openReport(page, { fixture: uiDemo.name, mode: REPORT_MODES.DIRECTORY });
      await expect(
        page.locator(".widget__title", { hasText: uiDemo.widgets.summary }),
      ).toBeVisible();
      await expect(page.locator(".history-trend__chart .chart__svg")).toBeVisible();
    },
  },
  {
    name: "categories",
    open: async (page) => {
      await openReport(page, {
        fixture: uiDemo.name,
        mode: REPORT_MODES.DIRECTORY,
        route: "categories",
      });
      await expect(page.locator(".pane__title-text")).toHaveText("Categories");
      await expect(page.locator(".search__input")).toBeVisible();
    },
  },
  {
    name: "graph",
    open: async (page) => {
      await openReport(page, {
        fixture: uiDemo.name,
        mode: REPORT_MODES.DIRECTORY,
        route: "graph",
      });
      await expect(page.getByRole("heading", { name: "Status" })).toBeVisible();
      await expect(page.locator(".chart__svg")).toHaveCount(7);
    },
  },
  {
    name: "timeline",
    open: async (page) => {
      await openReport(page, {
        fixture: uiDemo.name,
        mode: REPORT_MODES.DIRECTORY,
        route: "timeline",
      });
      await expect
        .poll(() => page.locator(".timeline__plot a").count())
        .toBeGreaterThan(10);
    },
  },
  {
    name: "suites-selected-test",
    open: async (page) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode: REPORT_MODES.DIRECTORY,
        tab: "suites",
        caseName: uiDemo.cases.failedPullRequest,
      });
      await expect(page.locator(".test-result__name")).toContainText(
        uiDemo.cases.failedPullRequest,
      );
    },
  },
  {
    name: "attachment-modal",
    open: async (page) => {
      await openCaseFromTree(page, {
        fixture: uiDemo.name,
        mode: REPORT_MODES.DIRECTORY,
        tab: "suites",
        caseName: uiDemo.cases.passedPullRequest,
      });

      const attachmentStep = stepLocator(page, uiDemo.htmlAttachmentStep);
      await attachmentStep.locator(".step__name").first().click();
      await expect(attachmentStep).toHaveClass(/step_expanded/);

      const attachmentRow = attachmentStep
        .locator(".attachment-row", { hasText: uiDemo.htmlAttachmentName })
        .first();
      await expect(attachmentRow).toBeVisible();
      await attachmentRow.locator(".attachment-row__fullscreen").click();

      await expect(page).toHaveURL(/attachment=/);
      await expect(page.locator(".attachment__iframe")).toBeVisible();
    },
  },
];

const runAxe = async (page: Page): Promise<AxeResults> => {
  await page.addScriptTag({ path: axeScriptPath });

  return page.evaluate(async (options) => {
    const axe = (window as Window & typeof globalThis & { axe: AxeRunner }).axe;

    return axe.run(document, options);
  }, AXE_OPTIONS);
};

const summarizeViolations = (violations: AxeViolation[]): Record<string, number> =>
  Object.fromEntries(violations.map((violation) => [violation.id, violation.nodes.length]));

const formatViolation = (violation: AxeViolation): string => {
  const targets = violation.nodes
    .slice(0, 3)
    .map((node) => node.target.join(" "))
    .join(", ");

  return `${violation.id} (${violation.impact ?? "unknown"}): ${
    violation.nodes.length
  } nodes, ${targets}`;
};

const attachAxeResults = async (target: AccessibilityTargetName, results: AxeResults) => {
  const body = JSON.stringify(
    {
      target,
      summary: summarizeViolations(results.violations),
      violations: results.violations,
    },
    null,
    2,
  );

  await attachment(`axe-${target}.json`, body, "application/json");
};

const attachViolationSteps = async (
  target: AccessibilityTargetName,
  violations: AxeViolation[],
) => {
  for (const violation of violations) {
    await step(
      `axe ${violation.id}: ${violation.nodes.length} affected node${
        violation.nodes.length === 1 ? "" : "s"
      }`,
      async (context) => {
        await context.parameter("target", target);
        await context.parameter("impact", violation.impact ?? "unknown");
        await context.parameter("help", violation.help);
        await context.parameter("helpUrl", violation.helpUrl);
        await context.parameter("nodeCount", violation.nodes.length.toString());
        await context.parameter(
          "targets",
          violation.nodes.map((node) => node.target.join(" ")).join("\n"),
        );

        await attachment(
          `axe-${target}-${violation.id}.json`,
          JSON.stringify(
            {
              target,
              violation,
            },
            null,
            2,
          ),
          "application/json",
        );
      },
    );
  }
};

const assertNoViolations = (target: AccessibilityTargetName, violations: AxeViolation[]) => {
  const summary = violations.map(formatViolation);

  expect(summary, `Accessibility violations found on ${target}`).toEqual([]);
};

test.describe("Accessibility Smoke", () => {
  for (const target of scanTargets) {
    test(`${target.name} has no automated accessibility violations`, async ({ page }) => {
      await target.open(page);

      const results = await runAxe(page);
      await attachAxeResults(target.name, results);
      await attachViolationSteps(target.name, results.violations);

      assertNoViolations(target.name, results.violations);
    });
  }
});
