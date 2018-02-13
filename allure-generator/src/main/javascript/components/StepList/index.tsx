import "./styles.scss";
import * as React from "react";
import {AllureStep} from "../TestResult/interfaces";
import * as bem from "b_";
import Arrow from "../Arrow";
import AttachmentList from "../AttachmentList";
import ParameterTable from "../ParameterTable";
import StatusDetails from "../StatusDetails";
import * as Moment from "moment";

const b = bem.with("StepList");

interface StepRowContentProps {
    step: AllureStep;
}

const StepRowContent: React.SFC<StepRowContentProps> = ({step}) => (
    <div className={b("content")}>
        {step.parameters && step.parameters.length ? <ParameterTable parameters={step.parameters}/> : null}
        {step.steps && step.steps.length ? <StepList steps={step.steps}/> : null}
        {step.attachments && step.attachments.length ? <AttachmentList attachments={step.attachments}/> : null}
        {step.statusMessage ? <StatusDetails status={step.status} message={step.statusMessage}/> : null}
    </div>
);

interface StepRowProps {
    step: AllureStep;
}

interface StepRowState {
    expanded: boolean;
}

class StepRow extends React.Component<StepRowProps, StepRowState> {
    state = {
        expanded: false
    };

    constructor(props: StepRowProps) {
        super(props);

        this.handleRowClick = this.handleRowClick.bind(this);
    }

    handleRowClick() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }))
    }

    render() {
        const {step} = this.props;
        const durationString = Moment.duration(step.duration).milliseconds();

        return (
            <li className={b()}>
                <div className={b("row")} onClick={this.handleRowClick}>
                    <div className={b("arrow")}>
                        <Arrow status={step.status} expanded={this.state.expanded}/>
                    </div>
                    <div className={b("name")}>{step.name}</div>
                    <div className={b("duration")}>{durationString}</div>
                </div>
                {this.state.expanded
                    ? <StepRowContent step={step}/>
                    : null
                }
            </li>
        );
    }
}

interface StepListProps {
    steps: Array<AllureStep>
}

const StepList: React.SFC<StepListProps> = ({steps}) => (
    <ul className={b()}>
        {steps.map((step, index) => (
            <StepRow key={index} step={step}/>
        ))}
    </ul>
);

export default StepList;