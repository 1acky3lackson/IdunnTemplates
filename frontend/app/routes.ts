import { type RouteConfig, index, layout, route } from "@react-router/dev/routes";

export default [
  layout("routes/root-layout.tsx", [
    // TOPBAR LAYOUT
    layout("routes/topbar-layout.tsx", [
      // 本地化路由
    route("/:lang?", "routes/home.tsx"), // 本地化主页
    route("/:lang?/templates", "templates/templates.tsx"),
    route("/:lang?/templates/:uuid", "templates/templates-detail.tsx"),
    ]),
  ]),
] satisfies RouteConfig;
