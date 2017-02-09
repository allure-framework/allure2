import typeByMime from '../util/attachmentType';

export default function(type) {
    return typeByMime(type).icon;
}
