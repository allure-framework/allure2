import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Arrow");
const text = bem.with("text");

const Arrow: React.SFC<{ status?: string, expanded?: boolean }> = ({status, expanded = false}) => (
    <span className={status
        ? ["fa fa-chevron-right", text("", {status}), b("", {expanded})].join(" ")
        : ["angle fa fa-angle-right fa-fw fa-lg", b("", {expanded})].join(" ")
    }/>
);

export default Arrow;