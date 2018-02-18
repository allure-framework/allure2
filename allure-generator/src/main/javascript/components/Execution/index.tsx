import * as React from "react";
import * as bem from "b_";
import { AllureTestResultExecution } from "../../interfaces";
import axios from "axios";
import ErrorSplash from "../ErrorSplash";
import Loader from "../Loader";
import StepList from "../StepList";
import AttachmentList from "../AttachmentList";

const b = bem.with("Execution");

interface ExecutionProps {
  testResultId: number;
}

interface ExecutionState {
  data?: AllureTestResultExecution;
  error?: Error;
}

export default class Execution extends React.Component<ExecutionProps, ExecutionState> {
  state: ExecutionState = {};

  componentDidMount() {
    this.loadData();
  }

  async loadData() {
    try {
      this.setState({ data: undefined });
      const { data } = await axios.get(`data/results/${this.props.testResultId}-execution.json`);
      this.setState({ data, error: undefined });
    } catch (error) {
      this.setState({ data: undefined, error });
    }
  }

  componentDidUpdate(prevProps: ExecutionProps) {
    if (prevProps.testResultId !== this.props.testResultId) {
      this.loadData();
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

    const steps = data.steps || [];
    const attachments = data.attachments || [];
    if (steps.length + attachments.length === 0) {
      return <>No content</>;
    }

    return (
      <>
        {steps.length > 0 && <StepList steps={steps} />}
        {attachments.length > 0 && <AttachmentList attachments={attachments} />}
      </>
    );
  }
}
