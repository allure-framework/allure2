import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Label");

const Label: React.SFC<{ status: string , text: string}> = ({status, text}) => (
    <span className={b("", {status})}>{text}</span>
);

export default Label;