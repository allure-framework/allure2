import * as React from "react";
import {statuses} from "../TestResult/interfaces";
import Label from "../Label";

const Statistic: React.SFC<{ statistic: { [key: string]: number } }> = ({statistic}) => (
    <>
        {statuses.map(status => {
            const count = statistic[status];
            return !!count && <Label key={status} status={status} text={count.toString()}/>
        })}
    </>
);

export default Statistic;