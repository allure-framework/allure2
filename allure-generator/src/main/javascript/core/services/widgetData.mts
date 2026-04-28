import { fetchReportJson } from "./reportData.mts";

type WidgetAttributes = import("../../types/report.mts").WidgetAttributes;

const loadWidgetData = async (name: string): Promise<WidgetAttributes> => {
  const data = await fetchReportJson<WidgetAttributes | unknown[]>(`widgets/${name}.json`);

  return Array.isArray(data) ? { items: data } : data;
};

export const createWidgetDataLoader =
  <TData,>(name: string) =>
  () =>
    loadWidgetData(name) as Promise<TData>;
