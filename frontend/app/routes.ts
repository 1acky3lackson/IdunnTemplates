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

      // ME page
      route("/:lang?/me", "pages/me/page.tsx"),

      // 模板列表
      route("/:lang?/templates", "routes/templates/templates.tsx"),
      // 模板详情
      route("/:lang?/templates/:uuid", "routes/templates/templates-detail.tsx"),

      // 合集列表
      route("/:lang?/collections", "pages/collections/page.tsx"),
      // 合集详情
      route("/:lang?/collections/:id", "pages/collections/[id]/page.tsx"),

      // 信任服务器
      route("/:lang?/servers", "pages/trusted-servers/page.tsx"),

      // 商业化
      //
      // 商业化管理员
      route("/:lang?/commercial/admin", "pages/commercial/admin/page.tsx"),

      // 订单列表
      route("/:lang?/commercial/orders", "pages/orders/page.tsx"),

      // 全局参数管理
      route(
        "/:lang?/commercial/global-params",
        "pages/checkout-param/page.tsx",
      ),
      // 结账单管理
      route("/:lang?/commercial/checkout", "pages/checkout-details/page.tsx"),

      // Admin Withdraw Console
      route(
        "/:lang?/commercial/withdraws",
        "pages/commercial/system-withdraws/page.tsx",
      ),

      // Projects
      route(
        "/:lang?/commercial/projects",
        "pages/commercial/projects/page.tsx",
      ),
      // Project DEtails
      route(
        "/:lang?/commercial/projects/:id",
        "pages/commercial/projects/[id]/page.tsx",
      ),

      // Netease Products
      route(
        "/:lang?/commercial/netease-products",
        "pages/commercial/netease-products/page.tsx",
      ),
      // Netease Product Details
      route(
        "/:lang?/commercial/netease-products/:id",
        "pages/commercial/netease-products/[id]/page.tsx",
      ),

      // Balance List
      route(
        "/:lang?/commercial/balances",
        "pages/commercial/balances/page.tsx",
      ),
      // Balance Transaction List
      route(
        "/:lang?/commercial/balances/transactions",
        "pages/commercial/balances/transactions/page.tsx",
      ),

      // Netease Withdraw List
      route(
        "/:lang?/commercial/netease-withdraws",
        "pages/commercial/netease-withdraws/page.tsx",
      ),
      // Netease Withdraw Details
      route(
        "/:lang?/commercial/netease-withdraws/:id",
        "pages/commercial/netease-withdraws/[id]/page.tsx",
      ),
    ]),
  ]),
] satisfies RouteConfig;
