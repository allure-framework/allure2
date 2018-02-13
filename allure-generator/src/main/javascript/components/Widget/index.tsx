import "./styles.scss"
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Widget");

const Widget: React.SFC<{name: string}> = ({name, children}) => (
    <div className={b()}>
        <div className={b("header")}>
            <h2 className={b("title")}>
                {name}
            </h2>
            <div className={b("controls")}>
                <span className={b("draggable-handle")}>
                    <span className={b("icon")}/>
                </span>
            </div>
        </div>
        <div className={b("content")}>
            {children}
        </div>
    </div>
);

export default Widget;