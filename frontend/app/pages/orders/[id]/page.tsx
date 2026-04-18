import type { Route } from "./+types/page";
import NeteaseOrderDetailView from "~/common/netease-order/NeteaseOrderDetailView";

export function meta({ params }: Route.MetaArgs) {
  return [{ title: `订单详情 ${params.id}` }];
}

export function clientLoader({ params }: Route.ClientLoaderArgs) {
  return { id: params.id };
}

export default function OrderDetailPage({ loaderData }: Route.ComponentProps) {
  return <NeteaseOrderDetailView id={String(loaderData.id)} />;
}
