import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Pane");

const Pane: React.SFC = ({children}) => (
    <div className={b()}>
        {children}
    </div>
);

const PaneHeader: React.SFC = ({children}) => (
    <div className={b("header")}>
        {children}
    </div>
);

const PaneTitle: React.SFC = ({children}) => (
    <h2 className={b("title")}>
        {children}
    </h2>
);

const PaneTitleWrapper: React.SFC = ({children}) => (
    <h2 className={b("title-wrapper")}>
        {children}
    </h2>
);

interface PaneSubtitleProps {
    ellipsis?: boolean;
}

const PaneSubtitle: React.SFC<PaneSubtitleProps> = ({ellipsis, children}) => (
    <div className={b("subtitle", {ellipsis})}>
        {children}
    </div>
);

const PaneContent: React.SFC = ({children}) => (
    <div className={b("content")}>
        {children}
    </div>
);

export {
    Pane,
    PaneHeader,
    PaneContent,
    PaneTitle,
    PaneTitleWrapper,
    PaneSubtitle,
};