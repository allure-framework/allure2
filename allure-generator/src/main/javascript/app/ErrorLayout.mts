import AppLayout from "../features/shell/AppLayout.mts";
import ErrorSplashView from "../shared/ui/ErrorSplashView.mts";

export default function ErrorLayout(
  options: { code: number; message: string } & Record<string, unknown>,
) {
  return AppLayout({
    ...options,
    createContentView: () => ErrorSplashView({ code: options.code, message: options.message }),
  });
}
