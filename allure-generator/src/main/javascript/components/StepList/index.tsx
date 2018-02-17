import "./styles.scss";
import * as React from "react";
import { AllureStep } from "../../interfaces";
import * as bem from "b_";
import Arrow from "../Arrow";
import AttachmentList from "../AttachmentList";
import ParameterTable from "../ParameterTable";
import StatusDetails from "../StatusDetails";
import Duration from "../Duration";
import StatusIcon from "../StatusIcon";

const b = bem.with("StepList");

interface StepRowContentProps {
  step: AllureStep;
}

const StepRowContent: React.SFC<StepRowContentProps> = ({ step }) => (
  <div className={b("content")}>
    {step.parameters && step.parameters.length ? (
      <ParameterTable parameters={step.parameters} />
    ) : null}
    {step.steps && step.steps.length ? <StepList steps={step.steps} /> : null}
    {step.attachments && step.attachments.length ? (
      <AttachmentList attachments={step.attachments} />
    ) : null}
    {step.statusMessage ? (
      <StatusDetails status={step.status} message={step.statusMessage} />
    ) : null}
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
    expanded: false,
  };

  constructor(props: StepRowProps) {
    super(props);

    this.handleRowClick = this.handleRowClick.bind(this);
  }

  handleRowClick() {
    this.setState(prevState => ({
      expanded: !prevState.expanded,
    }));
  }

  render() {
    const { step } = this.props;
    const hasContent =
      (step.steps || []).length + (step.attachments || []).length + (step.parameters || []).length >
      0;

    if (!hasContent) {
      return (
        <>
          <div className={b("row")}>
            <div className={b("status")}>
              <StatusIcon status={step.status} extraClasses={"fa-lg"} />
            </div>
            <div className={b("name")}>{step.name}</div>
            <div className={b("duration")}>
              <Duration value={step.duration} />
            </div>
          </div>
        </>
      );
    }

    return (
      <>
        <div className={b("row", { hasContent })} onClick={this.handleRowClick}>
          <div className={b("arrow")}>
            <Arrow status={step.status} expanded={this.state.expanded} />
          </div>
          <div className={b("name")}>{step.name}</div>
          <div className={b("duration")}>
            <Duration value={step.duration} />
          </div>
        </div>
        {this.state.expanded ? <StepRowContent step={step} /> : null}
      </>
    );
  }
}

interface StepListProps {
  steps: Array<AllureStep>;
}

const StepList: React.SFC<StepListProps> = ({ steps }) => (
  <ul className={b()}>
    {steps.map((step, index) => (
      <li key={index} className={b()}>
        <StepRow step={step} />
      </li>
    ))}
  </ul>
);

export default StepList;
