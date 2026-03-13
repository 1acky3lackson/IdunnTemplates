# ProjectApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1CommercialProjectsProjectIdContributionsContributionIdDelete**](#apiv1commercialprojectsprojectidcontributionscontributioniddelete) | **DELETE** /api/v1/commercial/projects/{projectId}/contributions/{contributionId} | 软删除贡献记录|
|[**apiV1CommercialProjectsProjectIdContributionsGet**](#apiv1commercialprojectsprojectidcontributionsget) | **GET** /api/v1/commercial/projects/{projectId}/contributions | 获取项目贡献列表|
|[**apiV1CommercialProjectsProjectIdContributionsPost**](#apiv1commercialprojectsprojectidcontributionspost) | **POST** /api/v1/commercial/projects/{projectId}/contributions | 添加项目贡献记录|
|[**apiV1CommercialProjectsProjectIdContributionsPreviewRecalculateGet**](#apiv1commercialprojectsprojectidcontributionspreviewrecalculateget) | **GET** /api/v1/commercial/projects/{projectId}/contributions/preview-recalculate | 预览重算结果|

# **apiV1CommercialProjectsProjectIdContributionsContributionIdDelete**
> object apiV1CommercialProjectsProjectIdContributionsContributionIdDelete()

软删除指定的贡献记录，填写删除原因。删除后系统会自动重算该记录所属角色的其他剩余有效记录的占比。

### Example

```typescript
import {
    ProjectApi,
    Configuration,
    ContributionDeleteRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new ProjectApi(configuration);

let projectId: number; //项目ID (default to undefined)
let contributionId: number; //要删除的贡献记录ID (default to undefined)
let contributionDeleteRequest: ContributionDeleteRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsContributionIdDelete(
    projectId,
    contributionId,
    contributionDeleteRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **contributionDeleteRequest** | **ContributionDeleteRequest**|  | |
| **projectId** | [**number**] | 项目ID | defaults to undefined|
| **contributionId** | [**number**] | 要删除的贡献记录ID | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** |  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsGet**
> Array<UserProjectContribution> apiV1CommercialProjectsProjectIdContributionsGet()

获取指定项目及其可能存在的父项目下，所有未被软删除的贡献记录列表。

### Example

```typescript
import {
    ProjectApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProjectApi(configuration);

let projectId: number; //项目ID (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsGet(
    projectId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **projectId** | [**number**] | 项目ID | defaults to undefined|


### Return type

**Array<UserProjectContribution>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** |  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsPost**
> object apiV1CommercialProjectsProjectIdContributionsPost()

添加一条新的贡献记录，系统会自动汇总该记录所属角色（Role）的所有有效分数，并重新计算和更新该角色的所有人占比（contributeRatio）。

### Example

```typescript
import {
    ProjectApi,
    Configuration,
    ContributionAddRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new ProjectApi(configuration);

let projectId: number; //项目ID (default to undefined)
let contributionAddRequest: ContributionAddRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsPost(
    projectId,
    contributionAddRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **contributionAddRequest** | **ContributionAddRequest**|  | |
| **projectId** | [**number**] | 项目ID | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** |  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsPreviewRecalculateGet**
> { [key: string]: RecalculatePreviewResponse; } apiV1CommercialProjectsProjectIdContributionsPreviewRecalculateGet()

重新计算该项目下所有角色分别对应的 contributeRatio，返回计算过程的数据供前端展示，**不会写入数据库**。

### Example

```typescript
import {
    ProjectApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new ProjectApi(configuration);

let projectId: number; //项目ID (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsPreviewRecalculateGet(
    projectId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **projectId** | [**number**] | 项目ID | defaults to undefined|


### Return type

**{ [key: string]: RecalculatePreviewResponse; }**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** |  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

