import { fetchReportJson } from "../../../core/services/reportData.mts";
import { flatten, omit, uniq } from "../../../shared/utils/collections.mts";

type RawTrendPoint = import("../../../types/report.mts").RawTrendPoint;
type TrendPoint = import("../../../types/report.mts").TrendPoint;

export const loadTrendData = async (name: string): Promise<TrendPoint[]> => {
  const response = await fetchReportJson<RawTrendPoint[]>(`widgets/${name}.json`);

  return response
    .slice()
    .reverse()
    .map((item, id) => {
      const data = omit(item.data, "total") as Record<string, number>;

      return {
        ...item,
        id,
        name: item.buildOrder ? `#${item.buildOrder}` : "",
        total: Object.values(data).reduce((prev, curr) => prev + curr, 0),
        data,
      };
    });
};

const getTrendKeys = (items: TrendPoint[]): string[] =>
  uniq(flatten(items.map((item) => Object.keys(item.data)))) as string[];

export const getSortedTrendKeysByLastValue = (items: TrendPoint[]): string[] => {
  const allKeys = getTrendKeys(items);
  const lastData = items[items.length - 1]?.data || {};

  return allKeys.sort((a, b) => (lastData[b] || 0) - (lastData[a] || 0));
};
