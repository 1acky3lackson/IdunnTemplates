import { IDUNN_API } from "~/api";
import {
  GlobalParamManager,
  type GlobalCheckoutParam,
} from "~/common/checkout-params/CheckoutParamsView";

export default function App() {
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
      taixueRatio: newConfig.taixueRatio,
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
    <div>
      <GlobalParamManager
        fetchCurrentConfig={fetchCurrent}
        fetchHistoryConfigs={fetchHistory}
        updateConfig={update}
      />
    </div>
  );
}
