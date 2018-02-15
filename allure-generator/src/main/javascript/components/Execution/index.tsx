import * as React from "react";
import * as bem from "b_";
import {AllureTestResultExecution} from "../../interfaces";
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

    async componentDidMount() {
        this.loadData();
    }

    async loadData() {
        try {
            const {data} = await axios.get(`data/results/${this.props.testResultId}-execution.json`);
            this.setState({data, error: undefined});
        } catch (error) {
            this.setState({data: undefined, error});
        }
    }

    render() {
        const {data, error} = this.state;

        if (error) {
            return <ErrorSplash name={error.name} message={error.message} stack={error.stack}/>
        }

        if (!data) {
            return <Loader/>
        }

        if (!data.steps && !data.attachments) {
            return <>No content</>
        }

        return (
            <>
                {data.steps && <StepList steps={data.steps}/>}
                {data.attachments && <AttachmentList attachments={data.attachments}/>}
            </>
        );
    }
}