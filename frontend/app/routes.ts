import { type RouteConfig, layout, route } from "@react-router/dev/routes";

export default [
  // 1. 最外层布局 (Root Layout)
  // 负责全局 Provider (Auth, Theme, Intlayer)
  layout("routes/root-layout.tsx", [

    // ---------------------------------------------------------
    // A. 不需要 Topbar 的页面 (独立路由)
    // ---------------------------------------------------------
    // 这里的 /:lang? 确保登录页也支持类似 /zh-CN/login 的 URL
    route("/:lang?/login", "pages/login/login-page.tsx"),

    // ---------------------------------------------------------
    // B. 需要 Topbar 的页面 (嵌套在 Topbar Layout 中)
    // ---------------------------------------------------------
    layout("routes/topbar-layout.tsx", [
      // 首页
      route("/:lang?", "routes/home.tsx"), 
      // 模板列表
      route("/:lang?/templates", "routes/templates/templates.tsx"),
      // 模板详情
      route("/:lang?/templates/:uuid", "routes/templates/templates-detail.tsx"),
    ]),

  ]),
] satisfies RouteConfig;