import translate from "../../helpers/t.mts";
import { normalizeReportDataError, type ReportDataError } from "../services/reportData.mts";
import ErrorSplashView from "../../shared/ui/ErrorSplashView.mts";
import LoaderView from "../../shared/ui/LoaderView.mts";

type Mountable = import("./types.mts").Mountable;

type AsyncMountOptions<TData> = {
  createError?: (error: ReportDataError) => Mountable;
  createLoader?: () => Mountable;
  createSuccess: (data: TData) => Mountable;
  fallbackMessage?: string;
  fallbackStatus?: number;
  load: () => Promise<TData> | TData;
  mount: (view: Mountable) => void;
  shouldIgnore?: () => boolean;
};

export const createReportLoadErrorView = (
  error: unknown,
  {
    fallbackMessage = translate("errors.loadingFailed"),
    fallbackStatus = 500,
  }: { fallbackMessage?: string; fallbackStatus?: number } = {},
) => {
  const normalized = normalizeReportDataError(error, {
    message: fallbackMessage,
    status: fallbackStatus,
  });

  return ErrorSplashView({
    code: normalized.status,
    message: normalized.message,
  });
};

export const mountAsyncView = async <TData,>({
  createError,
  createLoader,
  createSuccess,
  fallbackMessage = translate("errors.loadingFailed"),
  fallbackStatus = 500,
  load,
  mount,
  shouldIgnore = () => false,
}: AsyncMountOptions<TData>) => {
  mount(createLoader ? createLoader() : LoaderView());

  try {
    const data = await Promise.resolve(load());
    if (shouldIgnore()) {
      return undefined;
    }

    mount(createSuccess(data));
    return data;
  } catch (error: unknown) {
    if (shouldIgnore()) {
      return undefined;
    }

    const normalized = normalizeReportDataError(error, {
      message: fallbackMessage,
      status: fallbackStatus,
    });

    mount(
      createError
        ? createError(normalized)
        : ErrorSplashView({
            code: normalized.status,
            message: normalized.message,
          }),
    );

    return undefined;
  }
};
