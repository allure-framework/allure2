import { values } from "../../utils/statuses";

function byOrder(a, b) {
  return a.order < b.order ? -1 : 1;
}

function byName(a, b) {
  return String(a.name).toLowerCase() < String(b.name).toLowerCase() ? -1 : 1;
}

function byDuration(a, b) {
  if (a.time && a.time.duration && b.time && b.time.duration) {
    return a.time.duration < b.time.duration ? -1 : 1;
  }
  return 1;
}

function byMaxDuration(a, b) {
  if (a.time && a.time.maxDuration && b.time && b.time.maxDuration) {
    return a.time.maxDuration < b.time.maxDuration ? -1 : 1;
  }
  return 1;
}

function byNodeStatus(a, b) {
  return values.indexOf(a.status) > values.indexOf(b.status) ? -1 : 1;
}

function byGroupStatuses(a, b) {
  return values.reduce((all, cur) => {
    return a.statistic[cur] !== b.statistic[cur] && all === 0
      ? a.statistic[cur] - b.statistic[cur]
      : all;
  }, 0);
}

function compare(a, b, nodeCmp, groupCmp, direction) {
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

export default function getComparator({ sorter, ascending }) {
  const direction = ascending ? 1 : -1;
  switch (sorter) {
    case "sorter.order":
      return (a, b) => compare(a, b, byOrder, byName, direction);
    case "sorter.name":
      return (a, b) => compare(a, b, byName, byName, direction);
    case "sorter.duration":
      return (a, b) => compare(a, b, byDuration, byMaxDuration, direction);
    case "sorter.status":
      return (a, b) => compare(a, b, byNodeStatus, byGroupStatuses, direction);
    default:
      return 0;
  }
}
