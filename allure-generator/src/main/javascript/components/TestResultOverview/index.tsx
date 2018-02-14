import * as React from "react";
import StatusDetails from "../StatusDetails";
import ParameterTable from "../ParameterTable";
import {AllureTestResult} from "../../interfaces";
import AttachmentList from "../AttachmentList";
import StepList from "../StepList";

interface TestResultOverviewProps {
    testResult: AllureTestResult
}

interface TestResultOverviewState {
}

export default class TestResultOverview extends React.Component<TestResultOverviewProps, TestResultOverviewState> {

    render() {
        const {message, trace, status, parameters} = this.props.testResult;

        const statusDetailsBlock = message
            ? <StatusDetails message={message} trace={trace} status={status}/>
            : null;

        const parametersBlock = parameters
            ? <ParameterTable parameters={parameters}/>
            : null;

        // const attachmentsBlock = attachments
        //     ? <AttachmentList attachments={attachments}/>
        //     : null;
        //
        // const stepsBlock = steps
        //     ? <StepList steps={steps}/>
        //     : null;

        return (
            <>
                {statusDetailsBlock}
                {parametersBlock}
                {/*{stepsBlock}*/}
                {/*{attachmentsBlock}*/}
            </>
        );
    }
}