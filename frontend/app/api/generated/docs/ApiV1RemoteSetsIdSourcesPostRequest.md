# ApiV1RemoteSetsIdSourcesPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **string** | PATH | REMOTE_SET | [default to undefined]
**path** | **string** | type &#x3D; PATH 时填写，否则留空 | [optional] [default to undefined]
**targetSetId** | **string** | type&#x3D;REMOTE_SET 时填写，否则留空 | [default to undefined]
**weight** | **number** | 权重，默认 1.0 | [optional] [default to undefined]

## Example

```typescript
import { ApiV1RemoteSetsIdSourcesPostRequest } from './api';

const instance: ApiV1RemoteSetsIdSourcesPostRequest = {
    type,
    path,
    targetSetId,
    weight,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
