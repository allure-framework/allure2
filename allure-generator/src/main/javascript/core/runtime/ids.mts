let nextId = 0;

export const uniqueId = (prefix = "id") => `${prefix}${++nextId}`;
