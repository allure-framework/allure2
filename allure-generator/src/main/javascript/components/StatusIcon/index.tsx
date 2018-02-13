import * as React from "react";

const icons: { [key: string]: string } = {
    failed: 'fa fa-times-circle fa-fw text_status_failed',
    broken: 'fa fa-exclamation-circle fa-fw text_status_broken',
    passed: 'fa fa-check-circle fa-fw text_status_passed',
    skipped: 'fa fa-minus-circle fa-fw text_status_skipped',
    unknown: 'fa fa-question-circle fa-fw text_status_unknown',
};


const StatusIcon: React.SFC<{ status: string, extraClasses?: string }> = ({status, extraClasses = ""}) => (
    <span className={[icons[status], extraClasses].join(' ')}/>
);

export default StatusIcon;