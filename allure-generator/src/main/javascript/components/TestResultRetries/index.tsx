import * as React from "react";
import { AllureTestResult } from "../../interfaces";
import axios from "axios";
import ErrorSplash from "../ErrorSplash";
import Loader from "../Loader";
import TestResultList from "../TestResultList";

interface TestResultRetriesProps {
  testResultId: number;
}

interface TestResultRetriesState {
  data?: Array<AllureTestResult>;
  error?: Error;
}

export default class TestResultRetries extends React.Component<
  TestResultRetriesProps,
  TestResultRetriesState
> {
  state: TestResultRetriesState = {};

  async componentDidMount() {
    this.loadData();
  }

  async loadData() {
    try {
      const { data } = await axios.get(`data/results/${this.props.testResultId}-retries.json`);
      this.setState({ data, error: undefined });
    } catch (error) {
      this.setState({ data: undefined, error });
    }
  }

  render() {
    const { data, error } = this.state;

    if (error) {
      return <ErrorSplash name={error.name} message={error.message} stack={error.stack} />;
    }

    if (!data) {
      return <Loader />;
    }

    if (!data.length) {
      return <>No content</>;
    }

    return <TestResultList testResults={data} />;
  }
}
