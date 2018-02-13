import './styles.scss';
import * as React from "react";
import {ReactChild} from "react";
import * as bem from "b_";
import * as split from 'split.js';

const b = bem.with("SideBySide");

interface SideBySideProps {
    gutterSize?: number;
    left: ReactChild;
    right: ReactChild;
}

interface State {
}

export default class SideBySide extends React.Component<SideBySideProps, State> {

    componentDidMount() {
        const gutterSize = this.props.gutterSize || 10;
        split(['.SideBySide__left', '.SideBySide__right'], {gutterSize: gutterSize});
    }

    render() {
        return (
            <div className={b()}>
                <div className={b("left")}>{this.props.left}</div>
                <div className={b("right")}>{this.props.right}</div>
            </div>
        );
    }
}