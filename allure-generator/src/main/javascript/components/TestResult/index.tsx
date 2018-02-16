import "./styles.scss";
import * as React from "react";
import * as bem from "b_";
import {AllureTestResult} from "../../interfaces";
import axios from "axios";
import ErrorSplash from "../ErrorSplash";
import Loader from "../Loader";
import Tabs from "../Tabs";
import {Route} from "react-router";
import TestResultOverview from "../TestResultOverview";
import TestResultHistory from "../TestResultHistory";
import TestResultRetries from "../TestResultRetries";
import TestResultAttachments from "../TestResultAttachments";
import {Pane, PaneContent, PaneHeader, PaneSubtitle, PaneTitle} from "../Pane";
import Alert from "../Alert";
import {Unix2Date, Unix2Time} from "../DateTime";
import Duration from "../Duration";

const b = bem.with("TestResult");

const tabs = [{
    name: 'Overview',
    href: '',
    render: (testResult: AllureTestResult) => <TestResultOverview testResult={testResult}/>
}, {
    name: 'History',
    href: '/history',
    render: () => <TestResultHistory/>
}, {
    name: 'Retries',
    href: '/retries',
    render: () => <TestResultRetries/>
}, {
    name: 'Attachments',
    href: '/attachments',
    render: (testResult: AllureTestResult) => <TestResultAttachments testResultId={testResult.id}/>
}];

interface TestResultProps {
    id: string,
    match?: any
}

interface TestResultState {
    testResult?: AllureTestResult,
    error?: Error;
}

export default class TestResult extends React.Component<TestResultProps, TestResultState> {
    state: TestResultState = {};

    componentDidMount() {
        this.loadResult();
    }

    componentDidUpdate(prevProps: TestResultProps) {
        if (this.props.id !== prevProps.id) {
            this.loadResult();
        }
    }

    async loadResult() {
        try {
            const {data} = await axios.get(`data/results/${this.props.id}.json`);
            this.setState({testResult: data, error: undefined});
        } catch (error) {
            this.setState({error});
        }
    }

    render() {
        const {testResult, error} = this.state;

        if (error) {
            return <ErrorSplash name={error.name} message={error.message} stack={error.stack}/>
        }
        if (!testResult) {
            return <Loader/>
        }

        return (
            <Pane>
                <PaneHeader>
                    <PaneSubtitle ellipsis={true}>
                        {testResult.fullName}
                    </PaneSubtitle>
                    <PaneTitle>
                        {testResult.name}
                    </PaneTitle>
                    <Alert status={testResult.status} center={true}>
                        {testResult.status}
                        {' '}
                        <Unix2Date value={testResult.stop}/>
                        {' '}
                        at
                        {' '}
                        <Unix2Time value={testResult.stop}/>
                        {' '}
                        (<Duration value={testResult.duration}/>)
                    </Alert>
                    <Tabs tabs={tabs} match={this.props.match}/>
                </PaneHeader>
                <PaneContent>
                    {tabs.map(({href, render}) => (
                        <Route
                            key={`testresult-${testResult.id}-tab-${href}`}
                            path={`${this.props.match.url}${href}`}
                            render={() => render(testResult)}
                            exact={true}
                        />
                    ))}
                </PaneContent>
            </Pane>
        );
    }
}