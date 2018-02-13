import './styles.scss';
import * as React from "react";
import * as bem from "b_";
import Button, {ButtonSize} from "../Button";

const b = bem.with("StatusDetails");

interface StatusDetailsProps {
    status: string;
    message: string;
    trace?: string;
}

interface StatusDetailsState {
    expanded: boolean;
}

export default class StatusDetails extends React.Component<StatusDetailsProps, StatusDetailsState> {
    state = {
        expanded: false
    };

    constructor(props: StatusDetailsProps) {
        super(props);

        this.handleClick = this.handleClick.bind(this);
    }

    handleClick() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }))
    }

    render() {
        const traceBlock = this.state.expanded
            ? (
                <div className={b('trace')}>
                    <pre><code>{this.props.trace || 'Empty status details'}</code></pre>
                </div>
            )
            : null;

        return (
            <div className={`StatusDetails_status_${this.props.status}`}>
                <div className={b("message-wrapper")}>
                    <div className={b("message")}>
                        <pre><code>{this.props.message}</code></pre>
                    </div>
                    <div className={b("controls")}>
                        <Button size={ButtonSize.Large} onClick={this.handleClick}>
                            {this.state.expanded ? 'Hide trace' : 'Show trace'}
                        </Button>
                    </div>
                </div>
                {traceBlock}
            </div>
        );
    }
}