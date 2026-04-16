import { IDUNN_API } from "~/api";
import { neteaseOrderSchema } from "~/common/netease-order/config";
import { OrderDisplay } from "~/common/netease-order/OrderDisplay";
import {
  buildSearchString,
  buildSortString,
} from "~/common/util/search-test-utils";

export default function OrderListPage() {
  document.title = "销售订单";

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">销售订单</h1>
      <OrderDisplay />
    </div>
  );
}
