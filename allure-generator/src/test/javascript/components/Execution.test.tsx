import * as React from "react";
import * as moxios from "moxios";
import { shallow } from "enzyme";
import Execution from "../../../main/javascript/components/Execution";
import Loader from "../../../main/javascript/components/Loader";
import { AllureTestResultExecution } from "../../../main/javascript/interfaces";
import StepList from "../../../main/javascript/components/StepList";
import AttachmentList from "../../../main/javascript/components/AttachmentList";

const timeout = (delay?: number) => new Promise(r => setTimeout(r, delay));

beforeEach(() => moxios.install());
afterEach(() => moxios.uninstall());

function createComponent(resultId = 1, response: AllureTestResultExecution = {}) {
  const component = shallow(<Execution testResultId={resultId} />);
  moxios.stubRequest(`data/results/${resultId}-execution.json`, { response });
  return component;
}

test("should load data after first render", async () => {
  const component = createComponent();
  expect(component.find(Loader)).toHaveLength(1);
  await timeout();
  expect(moxios.requests.count()).toBe(1);
  expect(moxios.requests.mostRecent().url).toBe("data/results/1-execution.json");
});

test("should render empty state of response is empty", async () => {
  const component = createComponent(1, {});
  await timeout();
  component.update();
  expect(component.find(StepList)).toHaveLength(0);
  expect(component.find(AttachmentList)).toHaveLength(0);
  expect(component.text()).toEqual("No content");
});

test("should render the data if it is received", async () => {
  const component = createComponent(2, {
    steps: [{ name: "step one", status: "passed" }, { name: "step two", status: "failed" }],
    attachments: [{ id: "1", name: "test", source: "", type: "", size: 0 }],
  });
  await timeout();
  component.update();
  expect(component.find(StepList)).toHaveLength(1);
  expect(component.find(AttachmentList)).toHaveLength(1);
});

test("should reload testcase data on id change", async () => {
  const component = createComponent(1);
  await timeout();
  component.update();
  expect(component.find(Loader)).toHaveLength(0);
  component.setProps({testResultId: 3});
  component.update();
  expect(component.find(Loader)).toHaveLength(1);
  await timeout();
  expect(moxios.requests.count()).toBe(2);
  expect(moxios.requests.mostRecent().url).toBe("data/results/3-execution.json");
});
