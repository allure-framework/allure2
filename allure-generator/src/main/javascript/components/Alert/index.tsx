import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Alert");

interface AlertProps {
    status?: string,
    center?: boolean
}

const Alert: React.SFC<AlertProps> = ({status = "default", center = false, children}) => (
    <div className={b("", {status, center})}>
        {children}
    </div>
);

export default Alert;