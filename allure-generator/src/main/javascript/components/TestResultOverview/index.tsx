import * as React from "react";
import StatusDetails from "../StatusDetails";
import ParameterTable from "../ParameterTable";
import {AllureTestResult} from "../../interfaces";
import Execution from "../Execution";
import * as bem from "b_";
import {PaneSection} from "../Pane";

const b = bem.with("TestResultOverview");

interface TestResultOverviewProps {
    testResult: AllureTestResult
}

export default class TestResultOverview extends React.Component<TestResultOverviewProps, any> {

    render() {
        const {id, message, trace, status, parameters} = this.props.testResult;

        const statusDetailsBlock = message
            ? <StatusDetails message={message} trace={trace} status={status}/>
            : null;

        const parametersBlock = parameters
            ? <ParameterTable parameters={parameters}/>
            : null;

        return (
            <>
                {statusDetailsBlock}
                {parametersBlock}
                <PaneSection title={"Execution"}>
                    <Execution testResultId={id}/>
                </PaneSection>
            </>
        );
    }
}