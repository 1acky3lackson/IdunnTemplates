# ApiRenderPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**schematicUrl** | **string** | 必须能够直链访问，且没有 CORS 问题 | [default to undefined]
**width** | **number** |  | [default to undefined]
**height** | **number** |  | [default to undefined]
**alpha** | **number** | 默认 Math.PI / 4 | [default to undefined]
**beta** | **number** | 默认 atan(2); | [default to undefined]
**radius** | **number** | 默认 0.5 | [default to undefined]

## Example

```typescript
import { ApiRenderPostRequest } from './api';

const instance: ApiRenderPostRequest = {
    schematicUrl,
    width,
    height,
    alpha,
    beta,
    radius,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
