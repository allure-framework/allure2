import './styles.scss';
import * as React from "react";
import * as Modal from "react-modal";
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
    showModal: boolean;
}

export default class StatusDetails extends React.Component<StatusDetailsProps, StatusDetailsState> {
    state = {
        expanded: false,
        showModal: false
    };

    componentDidMount() {
        Modal.setAppElement("#app");
    };

    handleTraceButtonClick = () => {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }))
    };

    handleExpandButtonClick = () => {
        this.setState(prevState => ({
            showModal: !prevState.showModal
        }))
    };

    closeModal = () => {
        this.setState({
            showModal: false
        })
    };

    render() {
        const {expanded, showModal} = this.state;
        const {status} = this.props;
        const messageBlock = (
            <div className={b("message")}>
                <pre><code>{this.props.message}</code></pre>
            </div>
        );
        const traceBlock = (
            <div className={b('trace')}>
                <pre><code>{this.props.trace || 'Empty status details'}</code></pre>
            </div>
        );

        return (
            <>
                <div className={b("", {status})}>
                    <div className={b("controls")}>
                        <div className={b("strut")}/>
                        <div>
                            <Button size={ButtonSize.Large} onClick={this.handleExpandButtonClick}>
                                <span className={"fa fa-expand fa-lg"}/>
                            </Button>
                        </div>
                        <div>
                            <Button size={ButtonSize.Large} onClick={this.handleTraceButtonClick}>
                                {expanded ? 'Hide trace' : 'Show trace'}
                            </Button>
                        </div>
                    </div>
                    {messageBlock}
                    {expanded ? traceBlock : null}
                </div>
                <Modal isOpen={showModal}
                       shouldCloseOnEsc
                       shouldFocusAfterRender
                       shouldReturnFocusAfterClose
                       shouldCloseOnOverlayClick={true}
                       style={{
                           overlay: {
                               backgroundColor: 'rgba(0, 0, 0, 0.75)'
                           }
                       }}
                       onRequestClose={this.closeModal}
                >
                    <div className={b("controls")}>
                        <div className={b("strut")}/>
                        <div>
                            <Button size={ButtonSize.Large} onClick={this.handleExpandButtonClick}>
                                Close
                            </Button>
                        </div>
                    </div>

                    <div className={b("", {status})}>
                        {messageBlock}
                        {traceBlock}
                    </div>
                </Modal>
            </>
        );
    }
}