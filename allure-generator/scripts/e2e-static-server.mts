import fs from "node:fs";
import http, { type IncomingMessage, type ServerResponse } from "node:http";
import path from "node:path";
import { fileURLToPath } from "node:url";

const host = process.env.PLAYWRIGHT_E2E_SERVER_HOST || "127.0.0.1";
const port = Number(process.env.PLAYWRIGHT_E2E_SERVER_PORT || "4173");
const moduleDirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(moduleDirname, "..", "build", "e2e");

const contentTypes: Record<string, string> = {
  ".css": "text/css; charset=utf-8",
  ".csv": "text/csv; charset=utf-8",
  ".gif": "image/gif",
  ".html": "text/html; charset=utf-8",
  ".ico": "image/x-icon",
  ".imagediff": "application/vnd.allure.image.diff; charset=utf-8",
  ".httpexchange": "application/vnd.allure.http+json; charset=utf-8",
  ".jpeg": "image/jpeg",
  ".jpg": "image/jpeg",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".tsv": "text/tab-separated-values; charset=utf-8",
  ".txt": "text/plain; charset=utf-8",
  ".uri": "text/uri-list; charset=utf-8",
  ".webm": "video/webm",
  ".xml": "application/xml; charset=utf-8",
  ".zip": "application/zip",
};

const sendError = (
  response: ServerResponse<IncomingMessage>,
  statusCode: number,
  message: string,
): void => {
  response.writeHead(statusCode, { "Content-Type": "text/plain; charset=utf-8" });
  response.end(message);
};

const resolvePath = (pathname: string): string | null => {
  const relativePath = pathname.replace(/^\/+/, "");
  const resolvedPath = path.resolve(rootDir, relativePath);

  if (!resolvedPath.startsWith(rootDir)) {
    return null;
  }

  if (fs.existsSync(resolvedPath) && fs.statSync(resolvedPath).isDirectory()) {
    return path.join(resolvedPath, "index.html");
  }

  return resolvedPath;
};

const server = http.createServer((request, response) => {
  const baseOrigin = `http://${request.headers.host ?? `${host}:${port}`}`;
  const url = new URL(request.url ?? "/", baseOrigin);

  if (url.pathname === "/health") {
    response.writeHead(200, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("ok");
    return;
  }

  const filePath = resolvePath(decodeURIComponent(url.pathname));
  if (!filePath) {
    sendError(response, 403, "Forbidden");
    return;
  }

  if (!fs.existsSync(filePath) || !fs.statSync(filePath).isFile()) {
    sendError(response, 404, "Not found");
    return;
  }

  const contentType =
    contentTypes[path.extname(filePath).toLowerCase()] || "application/octet-stream";
  response.writeHead(200, {
    "Cache-Control": "no-store",
    "Content-Type": contentType,
  });

  if (request.method === "HEAD") {
    response.end();
    return;
  }

  fs.createReadStream(filePath).pipe(response);
});

server.listen(port, host, () => {
  process.stdout.write(`Serving Playwright reports from ${rootDir} at http://${host}:${port}\n`);
});

const closeServer = (): void => {
  server.close(() => process.exit(0));
};

process.on("SIGINT", closeServer);
process.on("SIGTERM", closeServer);
