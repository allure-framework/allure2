import { fetchReportJson } from "../../../core/services/reportData.mts";

type GlobalsData = import("../../../types/report.mts").GlobalsData;

let globalsDataPromise: Promise<GlobalsData> | undefined;

export const loadGlobalsData = (): Promise<GlobalsData> => {
  if (!globalsDataPromise) {
    globalsDataPromise = fetchReportJson<GlobalsData>("widgets/globals.json").catch(
      (error: unknown) => {
        globalsDataPromise = undefined;
        throw error;
      },
    );
  }

  return globalsDataPromise;
};
