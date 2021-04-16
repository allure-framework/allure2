import filesize from "filesize";

export default function(size) {
  return filesize(size, { base: 2, round: 1 });
}
