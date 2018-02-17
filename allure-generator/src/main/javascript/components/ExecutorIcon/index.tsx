import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("ExecutorIcon");

const ExecutorIcon: React.SFC<{ type?: string }> = ({type = "default"}) => (
    <span className={b("", {type})}/>
);

export default ExecutorIcon;
