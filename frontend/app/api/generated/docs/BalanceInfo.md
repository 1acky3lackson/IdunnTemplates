# BalanceInfo


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**username** | **string** | The username associated with the balance. | [optional] [default to undefined]
**balance** | **number** | The current available balance. | [optional] [default to undefined]
**frozen** | **number** | The frozen or pending balance. | [optional] [default to undefined]
**withdrawn** | **number** | The total amount withdrawn. | [optional] [default to undefined]

## Example

```typescript
import { BalanceInfo } from './api';

const instance: BalanceInfo = {
    username,
    balance,
    frozen,
    withdrawn,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
