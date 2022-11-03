import {values as marksValues} from "../../utils/marks";

function byStatuses(statuses) {
  return (child) => {
    if (child.children) {
      return child.children.length > 0;
    }
    return statuses[child.status];
  };
}

function byDuration(min, max) {
  return (child) => {
    if (child.children) {
      return child.children.length > 0;
    }
    return min <= child.time.duration && child.time.duration <= max;
  };
}

function byCriteria(searchQuery) {
  if (searchQuery && searchQuery.startsWith("tag:")) {
    return byTags(searchQuery.substring(4));
  } else {
    return byText(searchQuery);
  }
}

function byText(text) {
  text = (text && text.toLowerCase()) || "";
  return (child) => {
    return (
      !text ||
      child.name.toLowerCase().indexOf(text) > -1 ||
      (child.children && child.children.some(byText(text)))
    );
  };
}

function byTags(tag) {
  tag = (tag && tag.toLowerCase().trim()) || "";
  const tags = tag.split(/\s*,\s*/).filter((t) => t);
  return (child) => {
    const childTags = Array.isArray(child.tags) ? child.tags.filter(t => t).map(t => t.toLowerCase().trim()) : [];
    return (
      !tag ||
      tags.every((t) => childTags.indexOf(t) > -1) ||
      (child.children && child.children.some(byTags(tag)))
    );
  };
}

function byMark(marks) {
  return (child) => {
    if (child.children) {
      return child.children.length > 0;
    }
    return marksValues
        .map(k => !marks[k] || child[k])
        .reduce((a, b) => a && b, true);
  };
}

function mix(...filters) {
  return (child) => {
    let result = true;
    filters.forEach((filter) => {
      result = result && filter(child);
    });
    return result;
  };
}

export { byStatuses, byDuration, byCriteria, byText, byTags, byMark, mix };
