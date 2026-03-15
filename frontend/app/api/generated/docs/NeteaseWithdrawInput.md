# NeteaseWithdrawInput


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**originalValue** | **number** | 原始金额（必填） | [default to undefined]
**withdrawValue** | **number** | 实际提现金额（必填） | [default to undefined]
**saveTimeMs** | **number** | 记录时间（可选，默认当前时间） | [optional] [default to undefined]

## Example

```typescript
import { NeteaseWithdrawInput } from './api';

const instance: NeteaseWithdrawInput = {
    originalValue,
    withdrawValue,
    saveTimeMs,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
