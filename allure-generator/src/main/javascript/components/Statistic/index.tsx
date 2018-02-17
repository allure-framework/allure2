import * as React from "react";
import {AllureStatistic, statuses} from "../../interfaces";
import Label from "../Label";

const Statistic: React.SFC<{ statistic: AllureStatistic }> = ({statistic}) => (
    <>
        {statuses.map(status => {
            const count = statistic[status];
            return !!count && <Label key={status} status={status} text={count.toString()}/>;
        })}
    </>
);

export default Statistic;
