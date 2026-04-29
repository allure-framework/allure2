type IconName = import("../shared/icon/index.mts").IconName;

import { attachmentType } from "../features/attachments/runtime.mts";

const fileicon = (type: string | null | undefined): IconName => attachmentType(type || "").icon;

export default fileicon;
