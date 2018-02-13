import './styles.scss';
import * as React from "react";
import {AllureTreeNode} from "./interfaces";
import * as bem from "b_";
import {NavLink} from "react-router-dom";
import Arrow from "../Arrow";
import StatusIcon from "../StatusIcon";
import Statistic from "../Statistic";

const b = bem.with("TestResultTree");

const TreeGroupRow: React.SFC<{ node: AllureTreeNode, expanded: boolean }> = ({node, expanded}) => (
    <>
        <div className={b("arrow")}>
            <Arrow expanded={expanded}/>
        </div>
        <div className={b("name")}>
            {node.name}
        </div>
        <div className={b("statistic")}>
            <Statistic statistic={{failed: 2, broken: 7}}/>
        </div>
    </>
);

const TreeLeafRow: React.SFC<{ node: AllureTreeNode }> = ({node}) => (
    <>
        <div className={b("status")}>
            <StatusIcon status={node.status} extraClasses={"fa-lg"}/>
        </div>
        <div className={b("name")}>
            {node.name}
        </div>
    </>
);

const TreeChildren: React.SFC<{ children: Array<AllureTreeNode>, route: string }> = ({children, route}) => (
    <>
        {children.map(child => <TreeNode key={child.uid} node={child} route={route}/>)}
    </>
);

interface TreeNodeProps {
    route: string;
    node: AllureTreeNode;
}

interface TreeNodeState {
    expanded: boolean;
}

class TreeNode extends React.Component<TreeNodeProps, TreeNodeState> {
    state = {
        expanded: false
    };

    handleNodeClick = () => {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }));
    };

    render(): React.ReactNode {
        const {node, route} = this.props;
        const {expanded} = this.state;

        if (node.children) {
            return (
                <div className={b("node")}>
                    <div className={b("row")} onClick={this.handleNodeClick}>
                        <TreeGroupRow node={node} expanded={expanded}/>
                    </div>
                    {
                        expanded
                            ? (
                                <div className={b("children")}>
                                    <TreeChildren children={node.children} route={route}/>
                                </div>
                            )
                            : null
                    }
                </div>
            );
        }
        return (
            <div className={b("node")}>
                <NavLink
                    className={[b("row"), "link__no-decoration"].join(' ')}
                    activeClassName={`${b("row")}_active`}
                    to={`/${route}/${node.id}`}
                >
                    <TreeLeafRow node={node}/>
                </NavLink>
            </div>
        );

    }
}

const TestResultTree: React.SFC<{ root: AllureTreeNode, route: string }> = ({root, route}) => {
    return (
        <div className={b()}>
            <TreeChildren children={root.children || []} route={route}/>
        </div>
    );
};

export default TestResultTree;
