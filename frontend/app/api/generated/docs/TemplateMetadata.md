# TemplateMetadata


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**templateId** | **string** |  | [default to undefined]
**creatorId** | **string** |  | [default to undefined]
**creationTime** | **number** |  | [default to undefined]
**worldId** | **string** |  | [default to undefined]
**anchorX** | **number** |  | [default to undefined]
**anchorY** | **number** |  | [default to undefined]
**anchorZ** | **number** |  | [default to undefined]
**width** | **number** |  | [default to undefined]
**height** | **number** |  | [default to undefined]
**length** | **number** |  | [default to undefined]
**locked** | **boolean** |  | [default to undefined]
**lockedTimestamp** | **number** |  | [default to undefined]
**versions** | **Array&lt;string&gt;** |  | [default to undefined]
**deleted** | **boolean** |  | [default to undefined]
**stagedChanges** | [**TemplateMetadataStagedChanges**](TemplateMetadataStagedChanges.md) |  | [default to undefined]
**parentTemplateInstances** | **object** |  | [default to undefined]
**childTemplateInstances** | **object** |  | [default to undefined]

## Example

```typescript
import { TemplateMetadata } from './api';

const instance: TemplateMetadata = {
    templateId,
    creatorId,
    creationTime,
    worldId,
    anchorX,
    anchorY,
    anchorZ,
    width,
    height,
    length,
    locked,
    lockedTimestamp,
    versions,
    deleted,
    stagedChanges,
    parentTemplateInstances,
    childTemplateInstances,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
