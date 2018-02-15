import './styles.scss';
import * as React from "react";
import {AllureTreeGroup, AllureTreeLeaf} from "../../interfaces";
import * as bem from "b_";
import {NavLink} from "react-router-dom";
import Arrow from "../Arrow";
import StatusIcon from "../StatusIcon";
import Statistic from "../Statistic";
import Duration from "../Duration";

const b = bem.with("TestResultTree");

const TreeLeafNode: React.SFC<{ node: AllureTreeLeaf, route: string }> = ({node, route}) => (
    <div className={b("node")}>
        <NavLink
            className={[b("row"), "link__no-decoration"].join(' ')}
            activeClassName={`${b("row")}_active`}
            to={`/${route}/${node.parentUid}/${node.id}`}
        >
            <TreeLeafRow node={node}/>
        </NavLink>
    </div>
);

const TreeLeafRow: React.SFC<{ node: AllureTreeLeaf }> = ({node}) => (
    <>
        <div className={b("status")}>
            <StatusIcon status={node.status} extraClasses={"fa-lg"}/>
        </div>
        <div className={b("name")}>
            {node.name}
        </div>
        <div className={b("duration")}>
            <Duration value={node.duration}/>
        </div>
    </>
);

const TreeGroupRow: React.SFC<{ node: AllureTreeGroup, expanded: boolean }> = ({node, expanded}) => (
    <>
        <div className={b("arrow")}>
            <Arrow expanded={expanded}/>
        </div>
        <div className={b("name")}>
            {node.name}
        </div>
        <div className={b("statistic")}>
            <Statistic statistic={node.statistic}/>
        </div>
    </>
);

interface TreeGroupNodeProps {
    route: string;
    node: AllureTreeGroup;
    selectedGroupId?: string;
}

interface TreeGroupNodeState {
    expanded: boolean;
}

class TreeGroupNode extends React.Component<TreeGroupNodeProps, TreeGroupNodeState> {
    state = {
        expanded: false
    };

    handleNodeClick = () => {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }));
    };

    render(): React.ReactNode {
        const {node, route, selectedGroupId} = this.props;
        const {expanded} = this.state;

        return (
            <div className={b("node")}>
                <div className={b("row", {active: selectedGroupId === node.uid})} onClick={this.handleNodeClick}>
                    <TreeGroupRow node={node} expanded={expanded}/>
                </div>
                {
                    expanded
                        ? (
                            <div className={b("children")}>
                                <TreeChildren leafs={node.leafs} groups={node.groups} route={route}/>
                            </div>
                        )
                        : null
                }
            </div>
        );
    }
}

const TreeChildren: React.SFC<{ leafs?: Array<AllureTreeLeaf>, groups?: Array<AllureTreeGroup>, route: string }> = ({leafs, groups, route}) => (
    <>
        {groups
            ? groups.map(child => <TreeGroupNode key={child.uid} node={child} route={route}/>)
            : null
        }
        {leafs
            ? leafs.map(child => <TreeLeafNode key={child.id} node={child} route={route}/>)
            : null
        }
    </>
);

const TestResultTree: React.SFC<{ root: AllureTreeGroup, route: string }> = ({root, route}) => {
    return (
        <div className={b()}>
            <TreeChildren leafs={root.leafs} groups={root.groups} route={route}/>
        </div>
    );
};

export default TestResultTree;
