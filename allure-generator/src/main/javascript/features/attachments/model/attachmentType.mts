import {
  getBuiltinAttachmentType,
  type AttachmentTypeInfo as BuiltinAttachmentTypeInfo,
} from "./builtinAttachmentType.mts";

export type AttachmentTypeInfo = BuiltinAttachmentTypeInfo;

export default function attachmentType(contentType: string): AttachmentTypeInfo {
  return getBuiltinAttachmentType(contentType);
}
