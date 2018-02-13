import './styles.scss';
import * as React from "react";
import {Pane, PaneContent, PaneHeader, PaneTitle} from "../Pane";
import axios from "axios";
import {AllureTreeNode} from "../TestResultTree/interfaces";
import * as bem from "b_";
import SideBySide from "../SideBySide";
import ErrorSplash from "../ErrorSplash";
import Loader from "../Loader";
import {Route} from "react-router";
import TestResult from "../TestResult";
import TestResultTree from "../TestResultTree";

const b = bem.with("TestResultTreeContainer");

const Empty: React.SFC = () => (
    <p>No result selected</p>
);

interface TestResultTreeContainerProps {
    route: string;
    name: string;
    dataPath: string;
}

interface TestResultTreeContainerState {
    treeRoot?: AllureTreeNode;
    error?: Error;
}

export default class TestResultTreeContainer extends React.Component<TestResultTreeContainerProps, TestResultTreeContainerState> {
    state: TestResultTreeContainerState = {};

    async componentDidMount() {
        try {
            const {data} = await axios.get(this.props.dataPath);
            this.setState({
                treeRoot: data
            });
        } catch (e) {
            this.setState({
                error: e
            });
        }
    }

    render() {
        const {treeRoot, error} = this.state;

        if (error) {
            return <ErrorSplash name={error.name} message={error.message} stack={error.stack}/>
        }

        if (!treeRoot) {
            return <Loader/>
        }

        const {name, route} = this.props;
        const leftPane = (
            <Pane>
                <PaneHeader>
                    <PaneTitle>{name}</PaneTitle>
                </PaneHeader>
                <PaneContent>
                    <TestResultTree root={treeRoot} route={route}/>
                </PaneContent>
            </Pane>
        );

        const rightPane = (
            <>
                <Route
                    path={`/${route}`}
                    render={() => <Empty/>}
                    exact={true}
                />
                <Route
                    path={`/${route}/:testResultId`}
                    render={props => <TestResult id={props.match.params.testResultId} match={props.match}/>}
                />
            </>
        );

        return (
            <div className={b()}>
                <SideBySide left={leftPane} right={rightPane}/>
            </div>
        );
    }
}