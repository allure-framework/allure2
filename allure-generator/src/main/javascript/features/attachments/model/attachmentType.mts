import { getAttachmentViewer } from "../../../core/registry/index.mts";
import {
  getBuiltinAttachmentType,
  type AttachmentTypeInfo as BuiltinAttachmentTypeInfo,
} from "./builtinAttachmentType.mts";

type AttachmentViewerDescriptor =
  import("../../../core/registry/types.mts").AttachmentViewerDescriptor;

export type AttachmentTypeInfo = BuiltinAttachmentTypeInfo & Partial<AttachmentViewerDescriptor>;

export default function attachmentType(type: string): AttachmentTypeInfo {
  const customAttachmentViewer = getAttachmentViewer(type);
  if (customAttachmentViewer) {
    return {
      type: "custom",
      ...customAttachmentViewer,
    };
  }

  return getBuiltinAttachmentType(type);
}
