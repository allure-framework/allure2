import { filesize } from "filesize";

const formatFilesize = (size: number | null | undefined): string =>
  filesize(size ?? 0, { base: 2, round: 1 });

export default formatFilesize;
