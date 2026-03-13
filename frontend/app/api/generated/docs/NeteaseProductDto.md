# NeteaseProductDto


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** |  | [default to undefined]
**itemId** | **string** |  | [default to undefined]
**itemName** | **string** |  | [default to undefined]
**internalStatus** | [**NeteaseProductStatus**](NeteaseProductStatus.md) |  | [default to undefined]
**project** | [**ProjectInfo**](ProjectInfo.md) |  | [optional] [default to undefined]
**price** | **number** |  | [optional] [default to undefined]
**priceType** | **string** |  | [optional] [default to undefined]
**createTimeMs** | **number** | 创建时间戳（毫秒） | [optional] [default to undefined]
**updateTimeMs** | **number** | 最后更新时间戳（毫秒） | [optional] [default to undefined]

## Example

```typescript
import { NeteaseProductDto } from './api';

const instance: NeteaseProductDto = {
    id,
    itemId,
    itemName,
    internalStatus,
    project,
    price,
    priceType,
    createTimeMs,
    updateTimeMs,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
