import { IDUNN_API } from "~/api";
import {
  GlobalParamManager,
  type GlobalCheckoutParam,
} from "~/common/checkout-params/CheckoutParamsView";

export default function App() {
  document.title = "全局结算参数";

  const fetchCurrent = async () => {
    const res = await IDUNN_API.apiV1CommercialGlobalContextsCurrentGet();
    return res.data;
  };
  const fetchHistory = async () => {
    const res = await IDUNN_API.apiV1CommercialGlobalContextsGet();
    return res.data;
  };
  const update = async (
    newConfig: Partial<GlobalCheckoutParam>,
    reason: string,
  ) => {
    const res = await IDUNN_API.apiV1CommercialGlobalContextsPost({
      commercialRatio: newConfig.commercialRatio,
      templateDefectParam: newConfig.templateDefectParam,
      placerRatio: newConfig.placerRatio,
      uploaderRatio: newConfig.uploaderRatio,
      releaseDelayDays: newConfig.releaseDelayDays || 7,
      updateReason: reason,
    });
    return res.data;
  };

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">全局结算参数</h1>
      <GlobalParamManager
        fetchCurrentConfig={fetchCurrent}
        fetchHistoryConfigs={fetchHistory}
        updateConfig={update}
      />
    </div>
  );
}
