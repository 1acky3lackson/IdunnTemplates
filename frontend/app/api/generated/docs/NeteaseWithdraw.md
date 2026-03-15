# NeteaseWithdraw


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** | 记录唯一标识 ID | [optional] [default to undefined]
**username** | **string** | 执行提现操作的用户名 | [optional] [default to undefined]
**saveTimeMs** | **number** | 记录保存时间戳（毫秒） | [optional] [default to undefined]
**originalValue** | **number** | 未扣除手续费前的原始金额 | [optional] [default to undefined]
**usedOriginalValue** | **number** | 已被后续业务消耗/抵扣的原始金额 | [optional] [default to undefined]
**withdrawValue** | **number** | 实际提取到的到账金额 | [optional] [default to undefined]
**ratio** | **number** | 提现费率（计算公式：withdrawValue / originalValue） | [optional] [default to undefined]

## Example

```typescript
import { NeteaseWithdraw } from './api';

const instance: NeteaseWithdraw = {
    id,
    username,
    saveTimeMs,
    originalValue,
    usedOriginalValue,
    withdrawValue,
    ratio,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
