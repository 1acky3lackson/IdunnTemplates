# RecalculatePreviewResponse

占比重算预览对象

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**role** | [**RoleType**](RoleType.md) |  | [optional] [default to undefined]
**totalPoints** | **number** | 该角色当前未删除记录的总分数 | [optional] [default to undefined]
**contributions** | [**Array&lt;UserProjectContribution&gt;**](UserProjectContribution.md) | 重新计算了 contributeRatio 但未保存到数据库的记录列表 | [optional] [default to undefined]

## Example

```typescript
import { RecalculatePreviewResponse } from './api';

const instance: RecalculatePreviewResponse = {
    role,
    totalPoints,
    contributions,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
