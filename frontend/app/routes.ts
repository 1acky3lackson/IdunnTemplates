import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route("templates", "templates/templates.tsx"),
  route("templates/:uuid", "templates/templates-detail.tsx"),
] satisfies RouteConfig;
