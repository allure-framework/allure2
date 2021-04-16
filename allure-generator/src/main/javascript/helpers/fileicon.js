import typeByMime from "../utils/attachmentType";

export default function(type) {
  return typeByMime(type).icon;
}
