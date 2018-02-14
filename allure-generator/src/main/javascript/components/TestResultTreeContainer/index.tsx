import './styles.scss';
import * as React from "react";
import {Pane, PaneContent, PaneHeader, PaneTitle} from "../Pane";
import axios from "axios";
import {AllureTreeGroup, AllureTreeLeaf} from "../TestResultTree/interfaces";
import * as bem from "b_";
import SideBySide from "../SideBySide";
import ErrorSplash from "../ErrorSplash";
import Loader from "../Loader";
import {Route} from "react-router";
import TestResult from "../TestResult";
import TestResultTree from "../TestResultTree";
import {DropdownList} from "react-widgets";
import SorterGroup from "../SorterGroup";

const b = bem.with("TestResultTreeContainer");

const Empty: React.SFC = () => (
    <p>No result selected</p>
);

const calculateStatistic = (treeRoot: AllureTreeGroup): AllureTreeGroup => {
    return treeRoot;
};

type Comparator<A> = (a: A, b: A) => number;

interface Sorter {
    name: string;
    leafComparator: (asc: boolean) => Comparator<AllureTreeLeaf>;
    groupComparator: (asc: boolean) => Comparator<AllureTreeGroup>;
}

const sorterKeys = ["id", "name"];
const sorters: { [key: string]: Sorter } = {
    id: {
        name: "Id",
        leafComparator: (asc) => (a, b) => (a.id - b.id) * (asc ? 1 : -1),
        groupComparator: (asc) => (a, b) => (a.uid.localeCompare(b.uid)) * (asc ? 1 : -1),
    },
    name: {
        name: "Name",
        leafComparator: (asc) => (a, b) => (a.name.localeCompare(b.name)) * (asc ? 1 : -1),
        groupComparator: (asc) => (a, b) => (a.name.localeCompare(b.name)) * (asc ? 1 : -1),
    }
};

const sort = (treeRoot: AllureTreeGroup,
              leafComparator: Comparator<AllureTreeLeaf>,
              groupComparator: Comparator<AllureTreeGroup>): AllureTreeGroup => {
    return {
        ...treeRoot,
        groups: treeRoot.groups && treeRoot.groups
            .map(group => sort(group, leafComparator, groupComparator))
            .sort(groupComparator),
        leafs: treeRoot.leafs && treeRoot.leafs.sort(leafComparator)
    };
};

interface TestResultTreeContainerProps {
    route: string;
    name: string;
}

interface TestResultTreeContainerState {
    treeId?: string;
    treeRoot?: AllureTreeGroup;
    error?: Error;
    testResultTab?: string;
}

export default class TestResultTreeContainer extends React.Component<TestResultTreeContainerProps, TestResultTreeContainerState> {
    state: TestResultTreeContainerState = {
        treeId: 'behaviors'
    };

    async componentDidMount() {
        this.loadResult();
    }

    async loadResult() {
        try {
            const {data} = await axios.get(`data/${this.state.treeId}.json`);
            this.setState({treeRoot: data, error: undefined});
        } catch (error) {
            this.setState({error});
        }
    }

    onDropdownChange = (value: string) => {
        this.setState({
            treeId: value,
            treeRoot: undefined
        });
        this.loadResult();
    };

    handleSorterChange = (id: string, asc: boolean) => {
        this.setState(prevState => {
            if (!prevState.treeRoot) {
                return prevState;
            }

            const sorter = sorters[id];
            return {
                treeRoot: sort(prevState.treeRoot, sorter.leafComparator(asc), sorter.groupComparator(asc))
            };
        });
    };

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
                    <DropdownList defaultValue={this.state.treeId}
                                  data={["suites", "behaviors"]}
                                  onChange={this.onDropdownChange}
                    />
                    <SorterGroup
                        sorters={sorterKeys.map(id => ({id, name: sorters[id].name}))}
                        onSorterChange={this.handleSorterChange}
                    />
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
                    path={`/${route}/:groupId/:testResultId`}
                    render={props => <TestResult
                        id={props.match.params.testResultId}
                        match={props.match}
                    />}
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