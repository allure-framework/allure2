import autoprefixer from "autoprefixer";
import fs from "node:fs";
import { readFile, stat } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig, type Plugin } from "vite";

const projectRoot = fileURLToPath(new URL(".", import.meta.url));
const sourceRoot = path.resolve(projectRoot, "src/main/javascript");
const demoReportRoot = path.resolve(projectRoot, "build/demo-report");
const faviconPath = path.resolve(sourceRoot, "favicon.ico");

const mimeTypes = new Map([
  [".css", "text/css; charset=utf-8"],
  [".csv", "text/csv; charset=utf-8"],
  [".gif", "image/gif"],
  [".html", "text/html; charset=utf-8"],
  [".ico", "image/x-icon"],
  [".imagediff", "application/vnd.allure.image.diff; charset=utf-8"],
  [".httpexchange", "application/vnd.allure.http+json; charset=utf-8"],
  [".js", "text/javascript; charset=utf-8"],
  [".json", "application/json; charset=utf-8"],
  [".png", "image/png"],
  [".svg", "image/svg+xml"],
  [".txt", "text/plain; charset=utf-8"],
  [".webm", "video/webm"],
  [".xml", "application/xml; charset=utf-8"],
]);

const isPathInside = (parent: string, child: string) => {
  const relativePath = path.relative(parent, child);

  return relativePath === "" || (!relativePath.startsWith("..") && !path.isAbsolute(relativePath));
};

const getRequestPath = (requestUrl = "/") => {
  const { pathname } = new URL(requestUrl, "http://allure.local");

  return decodeURIComponent(pathname);
};

const rewriteDemoHtml = (html: string) =>
  html
    .replace(/<!-- allure-core-head:start -->[\s\S]*?<!-- allure-core-head:end -->/u, "")
    .replace(
      /<!-- allure-core-body:start -->[\s\S]*?<!-- allure-core-body:end -->/u,
      `<!-- allure-core-body:start -->
    <script>
        window.__allureCoreLoaded = new Promise(function (resolve, reject) {
            window.__allureResolveCoreLoaded = resolve;
            window.__allureRejectCoreLoaded = reject;
        });
    </script>
    <script type="module">
        import "/src/main/javascript/index.mts";

        if (typeof window.__allureResolveCoreLoaded === "function") {
            window.__allureResolveCoreLoaded([]);
        }
    </script>
    <!-- allure-core-body:end -->`,
    );

const demoReportDevServer = (): Plugin => ({
  name: "allure-demo-report-dev-server",
  apply: "serve",
  transformIndexHtml: {
    order: "pre",
    handler: rewriteDemoHtml,
  },
  configureServer(server) {
    server.middlewares.use(async (request, response, next) => {
      if (request.method !== "GET" && request.method !== "HEAD") {
        next();
        return;
      }

      const requestPath = getRequestPath(request.url);
      const demoFilePath = path.resolve(
        demoReportRoot,
        requestPath === "/" ? "index.html" : requestPath.replace(/^\/+/u, ""),
      );

      if (!isPathInside(demoReportRoot, demoFilePath)) {
        next();
        return;
      }

      let fileStats;

      try {
        fileStats = await stat(demoFilePath);
      } catch {
        next();
        return;
      }

      if (!fileStats.isFile()) {
        next();
        return;
      }

      const extension = path.extname(demoFilePath);
      const contentType = mimeTypes.get(extension) ?? "application/octet-stream";

      response.setHeader("Content-Type", contentType);

      if (path.basename(demoFilePath) === "index.html") {
        const html = await readFile(demoFilePath, "utf8");
        const transformedHtml = await server.transformIndexHtml(request.url ?? "/", html);

        response.end(request.method === "HEAD" ? "" : transformedHtml);
        return;
      }

      if (request.method === "HEAD") {
        response.end();
        return;
      }

      fs.createReadStream(demoFilePath).pipe(response);
    });
  },
});

const emitFavicon = (): Plugin => ({
  name: "allure-emit-favicon",
  apply: "build",
  buildStart() {
    this.emitFile({
      type: "asset",
      name: "favicon.ico",
      source: fs.readFileSync(faviconPath),
    });
  },
});

export default defineConfig(({ mode }) => ({
  root: projectRoot,
  base: "",
  define: {
    "process.env.DEBUG_INFO_ENABLED": JSON.stringify(mode === "development"),
  },
  css: {
    postcss: {
      plugins: [autoprefixer()],
    },
  },
  build: {
    outDir: path.resolve(projectRoot, "build/www"),
    emptyOutDir: true,
    target: "es2022",
    assetsDir: "assets",
    assetsInlineLimit: Number.MAX_SAFE_INTEGER,
    cssCodeSplit: true,
    manifest: "vite-manifest.json",
    rollupOptions: {
      input: path.resolve(sourceRoot, "index.mts"),
      output: {
        format: "iife",
        name: "allureReport",
        entryFileNames: "assets/[name]-[hash].js",
        assetFileNames: "assets/[name]-[hash][extname]",
      },
    },
  },
  server: {
    strictPort: true,
  },
  plugins: [demoReportDevServer(), emitFavicon()],
}));
