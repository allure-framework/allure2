import * as React from "react";

const ErrorSplash: React.SFC<Error> = ({name, message, stack}) => (
    <div className={"pane"}>
        <h1>{name}</h1>
        <p>{message}</p>
        <p>{stack}</p>
    </div>
);

export default ErrorSplash;
