# CheckoutWithdrawAllocation


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** |  | [optional] [default to undefined]
**allocatedOriginal** | **number** | 从该笔提现记录中扣除的原始 PE 金额 | [optional] [default to undefined]
**actualAmount** | **number** | 实际折算后的到账金额（已应用 Ratio） | [optional] [default to undefined]
**createTimeMs** | **number** |  | [optional] [default to undefined]
**checkoutDetail** | **object** |  | [optional] [default to undefined]

## Example

```typescript
import { CheckoutWithdrawAllocation } from './api';

const instance: CheckoutWithdrawAllocation = {
    id,
    allocatedOriginal,
    actualAmount,
    createTimeMs,
    checkoutDetail,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
