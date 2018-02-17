import "./styles.scss";
import * as React from "react";
import {AllureStatistic, statuses} from "../../interfaces";
import * as bem from "b_";

const b = bem.with("Bar");

const StatisticBar: React.SFC<{ statistic: AllureStatistic }> = ({statistic}) => (
    <div className={b()}>
        {statuses.map(status => {
            const count = statistic[status];
            return !!count && <div key={status} className={b("fill", {status})} style={{flexGrow: count}}>{count}</div>
        })}
    </div>
);

export default StatisticBar;
