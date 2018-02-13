import * as React from "react";
import StatusDetails from "../StatusDetails";
import ParameterTable from "../ParameterTable";
import {AllureTestResult} from "../TestResult/interfaces";
import AttachmentList from "../AttachmentList";
import StepList from "../StepList";

interface TestResultOverviewProps {
    testResult: AllureTestResult
}

interface TestResultOverviewState {
}

export default class TestResultOverview extends React.Component<TestResultOverviewProps, TestResultOverviewState> {

    render() {
        const {statusMessage, statusTrace, status, parameters, testStage} = this.props.testResult;

        const statusDetailsBlock = statusMessage
            ? <StatusDetails message={statusMessage} trace={statusTrace} status={status}/>
            : null;

        const parametersBlock = parameters
            ? <ParameterTable parameters={parameters}/>
            : null;

        const attachmentsBlock = (testStage && testStage.attachments)
            ? <AttachmentList attachments={testStage.attachments}/>
            : null;

        const stepsBlock = (testStage && testStage.steps)
            ? <StepList steps={testStage.steps}/>
            : null;

        return (
            <>
                {statusDetailsBlock}
                {parametersBlock}
                {stepsBlock}
                {attachmentsBlock}
            </>
        );
    }
}