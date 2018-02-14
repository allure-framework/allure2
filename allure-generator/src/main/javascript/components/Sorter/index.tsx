import "./styles.scss";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Sorter");

interface SorterProps {
    id: string;
    name: string;
    asc: boolean;
    enabled: boolean;
    onSorterClick: (id: string) => void;
}

const Sorter: React.SFC<SorterProps> = ({id, name, enabled, asc, onSorterClick}) => (
    <div className={b()} onClick={() => onSorterClick(id)}>
        <div className={b("name")}>
            {name}
        </div>
        <div className={b("icon")}>
            <span className={"fa fa-stack"}>
                <span
                    className={["fa fa-sort-asc fa-stack-1x", b("icon", {enabled: enabled && asc})].join(" ")}/>
                <span
                    className={["fa fa-sort-desc fa-stack-1x", b("icon", {enabled: enabled && !asc})].join(" ")}/>
            </span>
        </div>
    </div>
);

export default Sorter
