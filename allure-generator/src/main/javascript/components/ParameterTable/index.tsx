import './styles.scss';
import * as React from "react";
import * as bem from "b_";
import {AllureTestParameter} from "../../interfaces";

const b = bem.with("ParameterTable");

interface ParameterTableProps {
    parameters: Array<AllureTestParameter>;
}

const ParameterTableRow: React.SFC<AllureTestParameter> = ({name, value}) => (
    <tr>
        <td className={b("name")}>{name || 'null'}</td>
        <td className={b("value")}>{value || 'null'}</td>
    </tr>
);

const ParameterTable: React.SFC<ParameterTableProps> = ({parameters}) => (

    <table className={b()}>
        <tbody>
        {parameters.map((row, index) =>
            <ParameterTableRow key={index} name={row.name} value={row.value}/>
        )}
        </tbody>
    </table>
);

export default ParameterTable;