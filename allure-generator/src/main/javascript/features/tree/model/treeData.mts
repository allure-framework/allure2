import { fetchReportJson } from "../../../core/services/reportData.mts";
import { values } from "../../../utils/statuses.mts";

type Statistic = import("../../../types/report.mts").Statistic;
type Status = import("../../../types/report.mts").Status;
type Time = import("../../../types/report.mts").Time;
type TreeNode = import("../../../types/report.mts").TreeNode;
type TreeRoot = import("../../../types/report.mts").TreeRoot;

export type TreeFilter = (node: TreeNode) => boolean;
export type TreeSorter = (left: TreeNode, right: TreeNode) => number;

export type LoadedTreeData = {
  uid?: string;
  items: TreeNode[];
  allNodes: TreeNode[];
  allResults: TreeNode[];
  testResults: TreeNode[];
  time: Time;
  statistic: Statistic;
};

const statuses: readonly Status[] = values;

const updateTime = (
  timeA: Time,
  timeB: Time | undefined,
  field: keyof Time,
  operation: (a: number, b: number) => number,
) => {
  const current = timeA[field];
  const next = timeB?.[field];

  if (typeof current === "number" && typeof next === "number") {
    timeA[field] = operation(current, next);
  }
};

const flattenTreeResults = (children: TreeNode[] = []): TreeNode[] =>
  children.reduce<TreeNode[]>((results, child) => {
    if (child.children) {
      return results.concat(flattenTreeResults(child.children));
    }

    return results.concat(child);
  }, []);

const calculateStatistic = (items: TreeNode[]): Statistic => {
  const statistic: Statistic = {};

  statuses.forEach((value) => {
    statistic[value] = 0;
  });

  items.forEach((item) => {
    if (item.children) {
      statuses.forEach((value) => {
        statistic[value] = (statistic[value] || 0) + (item.statistic?.[value] || 0);
      });
    } else if (item.status) {
      statistic[item.status] = (statistic[item.status] || 0) + 1;
    }
  });

  return statistic;
};

const calculateTime = (items: TreeNode[]): Time => {
  const time: Time = {
    maxDuration: Number.MIN_VALUE,
    minDuration: Number.MAX_VALUE,
    sumDuration: 0,
    duration: 0,
    start: Number.MAX_VALUE,
    stop: Number.MIN_VALUE,
  };

  items.forEach((item) => {
    if (item.children) {
      updateTime(time, item.time, "maxDuration", Math.max);
      updateTime(time, item.time, "minDuration", Math.min);
      updateTime(time, item.time, "sumDuration", (a, b) => a + b);
    } else {
      const durationValue = item.time?.duration;
      if (typeof durationValue === "number" && isFinite(durationValue)) {
        time.maxDuration = Math.max(time.maxDuration || 0, durationValue);
        time.minDuration = Math.min(time.minDuration || durationValue, durationValue);
        time.sumDuration = (time.sumDuration || 0) + durationValue;
      }
    }

    updateTime(time, item.time, "start", Math.min);
    updateTime(time, item.time, "stop", Math.max);
    time.duration = (time.stop || 0) - (time.start || 0);
  });

  return time;
};

const compareExecutionOrder = (left: TreeNode, right: TreeNode) => {
  const leftStart = left.time?.start;
  const rightStart = right.time?.start;
  if (typeof leftStart === "number" && typeof rightStart === "number" && leftStart !== rightStart) {
    return leftStart - rightStart;
  }

  if (typeof leftStart === "number" && typeof rightStart !== "number") {
    return -1;
  }

  if (typeof leftStart !== "number" && typeof rightStart === "number") {
    return 1;
  }

  const leftStop = left.time?.stop;
  const rightStop = right.time?.stop;
  if (typeof leftStop === "number" && typeof rightStop === "number" && leftStop !== rightStop) {
    return leftStop - rightStop;
  }

  if (typeof leftStop === "number" && typeof rightStop !== "number") {
    return -1;
  }

  if (typeof leftStop !== "number" && typeof rightStop === "number") {
    return 1;
  }

  const nameComparison = String(left.name || "").localeCompare(
    String(right.name || ""),
    undefined,
    {
      sensitivity: "base",
    },
  );
  if (nameComparison !== 0) {
    return nameComparison;
  }

  return String(left.uid || "").localeCompare(String(right.uid || ""), undefined, {
    sensitivity: "base",
  });
};

const createLeafOrderMap = (children: TreeNode[] = []) => {
  const directLeaves = children.filter((child) => !child.children);
  const sortedLeaves = [...directLeaves].sort(compareExecutionOrder);

  return new Map(sortedLeaves.map((leaf, index) => [leaf, index + 1]));
};

const normalizeNodes = (children: TreeNode[] = []): TreeNode[] => {
  const leafOrders = createLeafOrderMap(children);

  return children.map((child) => {
    if (!child.children) {
      return {
        ...child,
        order: leafOrders.get(child),
      };
    }

    const normalizedChildren = normalizeNodes(child.children);

    return {
      ...child,
      children: normalizedChildren,
      statistic: calculateStatistic(normalizedChildren),
      time: calculateTime(normalizedChildren),
    };
  });
};

const projectTreeChildren = (
  children: TreeNode[] = [],
  filter: TreeFilter,
  sorter: TreeSorter,
): TreeNode[] =>
  children
    .map((child) => {
      if (!child.children) {
        return child;
      }

      const projectedChildren = projectTreeChildren(child.children, filter, sorter);

      return {
        ...child,
        children: projectedChildren,
        statistic: calculateStatistic(projectedChildren),
        time: calculateTime(projectedChildren),
      };
    })
    .filter(filter)
    .sort(sorter);

export const loadTreeData = async (url: string): Promise<LoadedTreeData> => {
  const { uid, children = [] } = await fetchReportJson<TreeRoot>(url);
  const allNodes = normalizeNodes(children);
  const allResults = flattenTreeResults(allNodes);

  return {
    uid,
    items: allNodes,
    allNodes,
    allResults,
    testResults: allResults,
    time: calculateTime(allResults),
    statistic: calculateStatistic(allResults),
  };
};

export const projectTreeData = (
  treeData: LoadedTreeData,
  filter: TreeFilter,
  sorter: TreeSorter,
): LoadedTreeData => {
  const items = projectTreeChildren(treeData.allNodes, filter, sorter);

  return {
    ...treeData,
    items,
    testResults: flattenTreeResults(items),
  };
};
