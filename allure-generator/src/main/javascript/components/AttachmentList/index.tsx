import "./styles.scss";
import * as React from "react";
import {AllureAttachmentLink} from "../../interfaces";
import * as bem from "b_";
import Arrow from "../Arrow";
import AttachmentContent from "../AttachmentContent";

const b = bem.with("AttachmentList");

interface AttachmentRowProps {
    attachment: AllureAttachmentLink
}

interface AttachmentRowState {
    expanded: boolean;
}

class AttachmentRow extends React.Component<AttachmentRowProps, AttachmentRowState> {
    state = {
        expanded: false
    };

    handleRowClick = () => {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }))
    };

    handleExpandClick = (e: any) => {
        e.preventDefault();
        //TODO
    };

    render() {
        const {attachment} = this.props;
        return (
            <li className={b()}>
                <div className={b("row", {expanded: this.state.expanded})} onClick={this.handleRowClick}>
                    <div className={b("arrow", {expanded: this.state.expanded})}>
                        <Arrow/>
                    </div>
                    <div className={b("name")}>{attachment.name}</div>
                    <div className={b("type")}>{attachment.type}</div>
                    <div className={b("links")}>
                        <div className={b("link")}>
                            <a
                                className={"link__no-decoration"}
                                href={`/data/attachments/${attachment.source}`}
                                target="_blank"
                            >
                                {attachment.size}&nbsp;&nbsp;
                                <span className={"fa fa-save fa-lg"}/>
                            </a>
                        </div>
                        <div className={b("link")} onClick={this.handleExpandClick}>
                            &nbsp;
                            <span className={"fa fa-expand fa-lg"}/>
                        </div>
                    </div>
                </div>
                {this.state.expanded
                    ? <div className={b("content")}><AttachmentContent attachment={attachment}/></div>
                    : null
                }
            </li>
        );
    }
}

interface AttachmentListProps {
    attachments: Array<AllureAttachmentLink>;
}

const AttachmentList: React.SFC<AttachmentListProps> = ({attachments}) => (
    <ul className={b()}>
        {attachments.map(attachment => (
            <AttachmentRow key={`attachment-row-${attachment.id}`} attachment={attachment}/>
        ))}
    </ul>
);

export default AttachmentList;