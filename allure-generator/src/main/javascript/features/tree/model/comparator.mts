import type { TreeSorting } from "../../../core/services/settings.mts";
import { values } from "../../../utils/statuses.mts";

type Status = import("../../../types/report.mts").Status;
type TreeNode = import("../../../types/report.mts").TreeNode;
type TreeSorter = import("./treeData.mts").TreeSorter;

const statuses = values as ReadonlyArray<Status>;

const getNodeDuration = (node: TreeNode, field: "duration" | "maxDuration") => node.time?.[field];

const byOrder = (a: TreeNode, b: TreeNode) => ((a.order || 0) < (b.order || 0) ? -1 : 1);

const byName = (a: TreeNode, b: TreeNode) =>
  String(a.name).toLowerCase() < String(b.name).toLowerCase() ? -1 : 1;

function byDuration(a: TreeNode, b: TreeNode) {
  const left = getNodeDuration(a, "duration");
  const right = getNodeDuration(b, "duration");
  if (typeof left === "number" && typeof right === "number") {
    return left < right ? -1 : 1;
  }
  return 1;
}

function byMaxDuration(a: TreeNode, b: TreeNode) {
  const left = getNodeDuration(a, "maxDuration");
  const right = getNodeDuration(b, "maxDuration");
  if (typeof left === "number" && typeof right === "number") {
    return left < right ? -1 : 1;
  }
  return 1;
}

const byNodeStatus = (a: TreeNode, b: TreeNode) =>
  statuses.indexOf(a.status as Status) > statuses.indexOf(b.status as Status) ? -1 : 1;

const byGroupStatuses = (a: TreeNode, b: TreeNode) =>
  statuses.reduce((all, cur) => {
    return a.statistic?.[cur] !== b.statistic?.[cur] && all === 0
      ? (a.statistic?.[cur] || 0) - (b.statistic?.[cur] || 0)
      : all;
  }, 0);

function compare(
  a: TreeNode,
  b: TreeNode,
  nodeCmp: TreeSorter,
  groupCmp: TreeSorter,
  direction: number,
) {
  if (a.children && !b.children) {
    return -1;
  } else if (!a.children && b.children) {
    return 1;
  } else if (a.children && b.children) {
    return direction * groupCmp(a, b);
  } else if (!a.children && !b.children) {
    return direction * nodeCmp(a, b);
  } else {
    return 0;
  }
}

export default function getComparator({
  sorter,
  ascending,
}: Pick<TreeSorting, "sorter" | "ascending">) {
  const direction = ascending ? 1 : -1;
  switch (sorter) {
    case "sorter.order":
      return (a: TreeNode, b: TreeNode) => compare(a, b, byOrder, byName, direction);
    case "sorter.name":
      return (a: TreeNode, b: TreeNode) => compare(a, b, byName, byName, direction);
    case "sorter.duration":
      return (a: TreeNode, b: TreeNode) => compare(a, b, byDuration, byMaxDuration, direction);
    case "sorter.status":
      return (a: TreeNode, b: TreeNode) => compare(a, b, byNodeStatus, byGroupStatuses, direction);
    default:
      return () => 0;
  }
}
