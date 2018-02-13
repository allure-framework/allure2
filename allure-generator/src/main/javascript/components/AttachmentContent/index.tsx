import "./styles.scss";
import {AllureAttachmentLink} from "../TestResult/interfaces";
import * as React from "react";
import * as bem from "b_";

const b = bem.with("Attachment");

interface AttachmentType {
    type: string;
    icon: string;
    parser?: (data: any) => (any)
}

function typeByMime(type: string): AttachmentType {
    switch (type) {
        case 'image/bmp':
        case 'image/gif':
        case 'image/tiff':
        case 'image/jpeg':
        case 'image/jpg':
        case 'image/png':
        case 'image/*':
            return {
                type: 'image',
                icon: 'fa fa-file-image-o'
            };
        case 'text/xml':
        case 'application/xml':
        case 'application/json':
        case 'text/json':
        case 'text/yaml':
        case 'application/yaml':
        case 'application/x-yaml':
        case 'text/x-yaml':
            return {
                type: 'code',
                icon: 'fa fa-file-code-o',
                parser: d => d
            };
        case 'text/plain':
        case 'text/*':
            return {
                type: 'text',
                icon: 'fa fa-file-text-o',
                parser: d => d
            };
        case 'text/html':
            return {
                type: 'html',
                icon: 'fa fa-file-code-o',
            };
        case 'image/svg+xml':
            return {
                type: 'svg',
                icon: 'fa fa-file-image-o'
            };
        case 'video/mp4':
        case 'video/ogg':
        case 'video/webm':
            return {
                type: 'video',
                icon: 'fa fa-file-video-o'
            };
        case 'application/x-tar':
        case 'application/x-gtar':
        case 'application/x-bzip2':
        case 'application/gzip':
        case 'application/zip':
            return {
                type: 'archive',
                icon: 'fa fa-file-archive-o'
            };
        default:
            return {
                type: '',
                icon: 'fa fa-file-o'
            };
    }
}

interface AttachmentContentProps {
    attachment: AllureAttachmentLink;
}

const AttachmentContent: React.SFC<AttachmentContentProps> = ({attachment}) => {
    const attachmentType = typeByMime(attachment.type);
    switch (attachmentType.type) {
        case "table":
            return <div>Table</div>;
        case "image":
            return <img className={b("media")} src={`/data/attachments/${attachment.source}`}/>;
        case "svg":
            return <div>SVG</div>;
        case "video":
            return <div>Video</div>;
        case "uri":
            return <div>URI</div>;
        case "html":
            return <div>Html</div>;
        default:
            return <div>Default</div>
    }
};

export default AttachmentContent;