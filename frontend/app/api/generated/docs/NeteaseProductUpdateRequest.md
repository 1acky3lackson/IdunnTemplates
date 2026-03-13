# NeteaseProductUpdateRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**projectId** | **number** | 要关联的项目ID。如果为 null 且 clearProject 为 false，则项目保持不变。 | [optional] [default to undefined]
**clearProject** | **boolean** | 设置为 true 时，将强制清除项目关联（此时忽略 projectId）。 | [optional] [default to false]
**internalStatus** | [**NeteaseProductStatus**](NeteaseProductStatus.md) |  | [optional] [default to undefined]

## Example

```typescript
import { NeteaseProductUpdateRequest } from './api';

const instance: NeteaseProductUpdateRequest = {
    projectId,
    clearProject,
    internalStatus,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
