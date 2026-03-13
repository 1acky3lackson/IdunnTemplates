# Project


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** |  | [default to undefined]
**name** | **string** |  | [default to undefined]
**displayName** | **string** |  | [default to undefined]
**description** | **string** |  | [default to undefined]
**pathName** | **string** |  | [default to undefined]
**kind** | **string** |  | [default to undefined]
**modelKind** | **string** |  | [default to undefined]
**minX** | **number** |  | [default to undefined]
**minY** | **number** |  | [default to undefined]
**minZ** | **number** |  | [default to undefined]
**maxX** | **number** |  | [default to undefined]
**maxY** | **number** |  | [default to undefined]
**maxZ** | **number** |  | [default to undefined]
**tpX** | **number** |  | [default to undefined]
**tpY** | **number** |  | [default to undefined]
**tpZ** | **number** |  | [default to undefined]
**tpYaw** | **number** |  | [default to undefined]
**tpPitch** | **number** |  | [default to undefined]
**parentProjectId** | **number** |  | [optional] [default to undefined]
**createTimeMs** | **number** |  | [default to undefined]
**deleteTimeMs** | **number** |  | [default to undefined]
**world** | [**World**](World.md) |  | [default to undefined]
**parentProject** | [**Project**](Project.md) |  | [default to undefined]

## Example

```typescript
import { Project } from './api';

const instance: Project = {
    id,
    name,
    displayName,
    description,
    pathName,
    kind,
    modelKind,
    minX,
    minY,
    minZ,
    maxX,
    maxY,
    maxZ,
    tpX,
    tpY,
    tpZ,
    tpYaw,
    tpPitch,
    parentProjectId,
    createTimeMs,
    deleteTimeMs,
    world,
    parentProject,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
