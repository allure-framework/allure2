export default function typeByMime(type) {
    switch (type) {
        case 'image/bmp':
        case 'image/gif':
        case 'image/tiff':
        case 'image/jpeg':
        case 'image/jpg':
        case 'image/png':
        case 'image/*':
            return 'image';
        case 'text/xml':
        case 'application/xml':
        case 'application/json':
        case 'text/json':
        case 'text/yaml':
        case 'application/yaml':
        case 'application/x-yaml':
        case 'text/x-yaml':
            return 'code';
        case 'text/plain':
        case 'text/*':
            return 'text';
        case 'text/html':
            return 'html';
        case 'text/csv':
            return 'csv';
        case 'image/svg+xml':
            return 'svg';
        case 'video/mp4':
        case 'video/ogg':
        case 'video/webm':
            return 'video';
        case 'text/uri-list':
            return 'uri';
        default:
    }
}
