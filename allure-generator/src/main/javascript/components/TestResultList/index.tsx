import "./styles.scss";
import * as React from "react";
import { AllureTestResult } from "../../interfaces";
import * as bem from "b_";
import Arrow from "../Arrow";
import Execution from "../Execution";
import { Unix2Date, Unix2Time } from "../DateTime";
import Duration from "../Duration";

const b = bem.with("TestResultList");

interface TestResultRowProps {
  testResult: AllureTestResult;
}

interface TestResultRowState {
  expanded: boolean;
}

class TestResultRow extends React.Component<TestResultRowProps, TestResultRowState> {
  state = {
    expanded: false,
  };

  handleRowClick = () => {
    this.setState(prevState => ({
      expanded: !prevState.expanded,
    }));
  };

  render() {
    const { testResult } = this.props;
    return (
      <li className={b()}>
        <div className={b("row", { expanded: this.state.expanded })} onClick={this.handleRowClick}>
          <div className={b("arrow", { expanded: this.state.expanded })}>
            <Arrow />
          </div>
          <div className={b("time")}>
            {testResult.status} <Unix2Date value={testResult.stop} /> at{" "}
            <Unix2Time value={testResult.stop} /> (<Duration value={testResult.duration} />)
          </div>
        </div>
        {this.state.expanded ? (
          <div className={b("content")}>
            <Execution testResultId={testResult.id} />
          </div>
        ) : null}
      </li>
    );
  }
}

const TestResultList: React.SFC<{ testResults: Array<AllureTestResult> }> = ({ testResults }) => (
  <ul className={b()}>
    {testResults.map(testResult => (
      <TestResultRow key={`test-result-row-${testResult.id}`} testResult={testResult} />
    ))}
  </ul>
);

export default TestResultList;
