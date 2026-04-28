import type { TreeMark } from "../../../core/services/settings.mts";
import { values as marksValues } from "../../../utils/marks.mts";

type Status = import("../../../types/report.mts").Status;
type TreeNode = import("../../../types/report.mts").TreeNode;
type TreeFilter = import("./treeData.mts").TreeFilter;

const markKeys = marksValues as ReadonlyArray<TreeMark>;

const byStatuses = (statuses: Record<Status, boolean>): TreeFilter => {
  return (child: TreeNode) => {
    if (child.children) {
      return child.children.length > 0;
    }
    return child.status ? statuses[child.status] : false;
  };
};

const byDuration = (min: number, max: number): TreeFilter => {
  return (child: TreeNode) => {
    if (child.children) {
      return child.children.length > 0;
    }
    const duration = child.time?.duration;
    return typeof duration === "number" && min <= duration && duration <= max;
  };
};

const byCriteria = (searchQuery?: string): TreeFilter => {
  if (searchQuery && searchQuery.startsWith("tag:")) {
    return byTags(searchQuery.substring(4));
  } else {
    return byText(searchQuery);
  }
};

const byText = (text?: string): TreeFilter => {
  const normalizedText = (text && text.toLowerCase()) || "";
  return (child: TreeNode) => {
    return (
      !normalizedText ||
      String(child.name).toLowerCase().indexOf(normalizedText) > -1 ||
      Boolean(child.children?.some(byText(normalizedText)))
    );
  };
};

const byTags = (tag?: string): TreeFilter => {
  const normalizedTag = (tag && tag.toLowerCase().trim()) || "";
  const tags = normalizedTag.split(/\s*,\s*/).filter((value) => value);
  return (child: TreeNode) => {
    const childTags = Array.isArray(child.tags)
      ? child.tags.filter((value) => value).map((value) => value.toLowerCase().trim())
      : [];
    return (
      !normalizedTag ||
      tags.every((value) => childTags.indexOf(value) > -1) ||
      Boolean(child.children?.some(byTags(normalizedTag)))
    );
  };
};

const byMark = (marks: Record<TreeMark, boolean>): TreeFilter => {
  return (child: TreeNode) => {
    if (child.children) {
      return child.children.length > 0;
    }
    return markKeys
      .map((markKey) => !marks[markKey] || Boolean(child[markKey]))
      .reduce((left, right) => left && right, true);
  };
};

const mix = (...filters: TreeFilter[]): TreeFilter => {
  return (child: TreeNode) => {
    let result = true;
    filters.forEach((filter) => {
      result = result && filter(child);
    });
    return result;
  };
};

export { byStatuses, byDuration, byCriteria, byMark, mix };
