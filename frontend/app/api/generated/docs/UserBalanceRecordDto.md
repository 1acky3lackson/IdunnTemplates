# UserBalanceRecordDto


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** |  | [optional] [default to undefined]
**username** | **string** |  | [optional] [default to undefined]
**type** | **string** | The type of the transaction (e.g., DEPOSIT, WITHDRAW, PROFIT). | [optional] [default to undefined]
**amount** | **number** |  | [optional] [default to undefined]
**balanceBefore** | **number** |  | [optional] [default to undefined]
**balanceAfter** | **number** |  | [optional] [default to undefined]
**relatedId** | **number** | The ID of the related entity (e.g., checkout detail ID, withdrawal ID). | [optional] [default to undefined]
**description** | **string** |  | [optional] [default to undefined]
**createTimeMs** | **number** |  | [optional] [default to undefined]

## Example

```typescript
import { UserBalanceRecordDto } from './api';

const instance: UserBalanceRecordDto = {
    id,
    username,
    type,
    amount,
    balanceBefore,
    balanceAfter,
    relatedId,
    description,
    createTimeMs,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
