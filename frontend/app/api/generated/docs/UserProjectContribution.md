# UserProjectContribution

贡献记录实体对象

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **number** | 记录ID | [optional] [default to undefined]
**username** | **string** | 贡献者用户名 | [optional] [default to undefined]
**role** | [**RoleType**](RoleType.md) |  | [optional] [default to undefined]
**comment** | **string** | 备注说明 | [optional] [default to undefined]
**contributePoints** | **number** | 贡献分数 | [optional] [default to undefined]
**contributeRatio** | **number** | 贡献占比 (0.0 到 1.0) | [optional] [default to undefined]
**deleteTimeMs** | **number** | 删除时间戳(毫秒)，null表示未删除 | [optional] [default to undefined]
**deleteReason** | **string** | 删除原因 | [optional] [default to undefined]
**deleteUsername** | **string** | 执行删除操作的用户名 | [optional] [default to undefined]
**createUsername** | **string** | 创建该记录的用户名 | [optional] [default to undefined]
**createTimeMs** | **string** | 创建时间戳(字符串格式) | [optional] [default to undefined]

## Example

```typescript
import { UserProjectContribution } from './api';

const instance: UserProjectContribution = {
    id,
    username,
    role,
    comment,
    contributePoints,
    contributeRatio,
    deleteTimeMs,
    deleteReason,
    deleteUsername,
    createUsername,
    createTimeMs,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
