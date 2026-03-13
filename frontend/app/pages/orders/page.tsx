import { IDUNN_API } from "~/api";
import { neteaseOrderSchema } from "~/common/netease-order/config";
import { OrderDisplay } from "~/common/netease-order/OrderDisplay";
import { buildSearchString, buildSortString } from "~/common/util/search-test-utils";

export default function OrderListPage() {
  return <OrderDisplay />
}