import * as React from "react";
import {AllureAttachmentLink} from "../../interfaces";
import axios from "axios";
import Loader from "../Loader";
import ErrorSplash from "../ErrorSplash";
import AttachmentList from "../AttachmentList";

interface TestResultAttachmentsProps {
    testResultId: number;
}

interface TestResultAttachmentsState {
    data?: Array<AllureAttachmentLink>;
    error?: Error;
}

export default class TestResultAttachments
    extends React.Component<TestResultAttachmentsProps, TestResultAttachmentsState> {

    state: TestResultAttachmentsState = {};

    async componentDidMount() {
        this.loadData();
    }

    async loadData() {
        try {
            const {data} = await axios.get(`data/results/${this.props.testResultId}-attachments.json`);
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

        if (!data.length) {
            return <>No content</>
        }

        return (
            <AttachmentList attachments={data}/>
        );
    }
}