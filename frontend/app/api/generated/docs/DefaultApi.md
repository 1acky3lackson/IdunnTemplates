# DefaultApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiAuthLoginPost**](#apiauthloginpost) | **POST** /api/auth/login | Yggdrasil 登录|
|[**apiAuthLogoutPost**](#apiauthlogoutpost) | **POST** /api/auth/logout | 查看个人信息 / 验证登录 Copy|
|[**apiAuthMeGet**](#apiauthmeget) | **GET** /api/auth/me | 查看个人信息 / 验证登录|
|[**apiV1CollectionsGet**](#apiv1collectionsget) | **GET** /api/v1/collections | 模板合集列表|
|[**apiV1CollectionsIdDelete**](#apiv1collectionsiddelete) | **DELETE** /api/v1/collections/{id} | 删除合集|
|[**apiV1CollectionsIdPut**](#apiv1collectionsidput) | **PUT** /api/v1/collections/{id} | 更新模板合集|
|[**apiV1CollectionsIdRandomGet**](#apiv1collectionsidrandomget) | **GET** /api/v1/collections/{id}/random | 获取随机模板|
|[**apiV1CollectionsIdTemplatesGet**](#apiv1collectionsidtemplatesget) | **GET** /api/v1/collections/{id}/templates | 分页获取/筛选合集的模板列表|
|[**apiV1CollectionsIdTemplatesPost**](#apiv1collectionsidtemplatespost) | **POST** /api/v1/collections/{id}/templates | 向合集添加模板|
|[**apiV1CollectionsIdTemplatesTemplateIdDelete**](#apiv1collectionsidtemplatestemplateiddelete) | **DELETE** /api/v1/collections/{id}/templates/{templateId} | 删除合集内的模板|
|[**apiV1CollectionsPost**](#apiv1collectionspost) | **POST** /api/v1/collections | 新建合集|
|[**apiV1CommercialAdminCalculateCheckoutDetailGet**](#apiv1commercialadmincalculatecheckoutdetailget) | **GET** /api/v1/commercial/admin/calculate/checkout-detail | 触发结算单实际金额结算|
|[**apiV1CommercialAdminCalculateOrderGet**](#apiv1commercialadmincalculateorderget) | **GET** /api/v1/commercial/admin/calculate/order | 触发订单收益分解计算|
|[**apiV1CommercialAdminCalculateReleaseGet**](#apiv1commercialadmincalculatereleaseget) | **GET** /api/v1/commercial/admin/calculate/release | 触发计算释放冻结资金|
|[**apiV1CommercialAdminGet**](#apiv1commercialadminget) | **GET** /api/v1/commercial/admin | 是否有管理员权限|
|[**apiV1CommercialAdminSyncNeOrderGet**](#apiv1commercialadminsyncneorderget) | **GET** /api/v1/commercial/admin/sync/ne-order | 同步爬虫网易订单|
|[**apiV1CommercialAdminSyncNeProductGet**](#apiv1commercialadminsyncneproductget) | **GET** /api/v1/commercial/admin/sync/ne-product | 同步爬虫网易商品|
|[**apiV1CommercialGlobalContextsCurrentGet**](#apiv1commercialglobalcontextscurrentget) | **GET** /api/v1/commercial/global-contexts/current | 获取当前激活的结算参数|
|[**apiV1CommercialGlobalContextsGet**](#apiv1commercialglobalcontextsget) | **GET** /api/v1/commercial/global-contexts | 获取全部修改历史记录|
|[**apiV1CommercialGlobalContextsPost**](#apiv1commercialglobalcontextspost) | **POST** /api/v1/commercial/global-contexts | 更新结算参数|
|[**apiV1CommercialNeteaseOrdersGet**](#apiv1commercialneteaseordersget) | **GET** /api/v1/commercial/netease-orders | 获取/筛选订单列表|
|[**apiV1CommercialNeteaseProductsGet**](#apiv1commercialneteaseproductsget) | **GET** /api/v1/commercial/products | 分页查询产品列表|
|[**apiV1CommercialNeteaseProductsIdGet**](#apiv1commercialneteaseproductsidget) | **GET** /api/v1/commercial/products/{id} | 根据ID查询单个产品|
|[**apiV1CommercialNeteaseProductsIdProjectPatch**](#apiv1commercialneteaseproductsidprojectpatch) | **PATCH** /api/v1/commercial/products/{id}/project | 指派或清除项目关联|
|[**apiV1CommercialNeteaseProductsIdPut**](#apiv1commercialneteaseproductsidput) | **PUT** /api/v1/commercial/products/{id} | 更新产品信息（支持部分字段）|
|[**apiV1CommercialNeteaseProductsIdStatsGet**](#apiv1commercialneteaseproductsidstatsget) | **GET** /api/v1/commercial/products/{id}/stats | 获取工程统计数据|
|[**apiV1CommercialNeteaseProductsIdStatusPatch**](#apiv1commercialneteaseproductsidstatuspatch) | **PATCH** /api/v1/commercial/products/{id}/status | 修改产品状态|
|[**apiV1CommercialNeteaseProductsProductIdOrdersGet**](#apiv1commercialneteaseproductsproductidordersget) | **GET** /api/v1/commercial/products/{productId}/orders | 获取商品的所有订单|
|[**apiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet**](#apiv1commercialneteasewithdrawswithdrawidallocationsget) | **GET** /api/v1/commercial/netease-withdraws/{withdrawId}/allocations | 查询提现记录使用详情|
|[**apiV1CommercialProjectsGet**](#apiv1commercialprojectsget) | **GET** /api/v1/commercial/projects | 获取/筛选项目列表|
|[**apiV1CommercialProjectsIdDelete**](#apiv1commercialprojectsiddelete) | **DELETE** /api/v1/commercial/projects/{id} | 软删除 Project|
|[**apiV1CommercialProjectsIdGet**](#apiv1commercialprojectsidget) | **GET** /api/v1/commercial/projects/{id} | 获取单个Project信息|
|[**apiV1CommercialProjectsIdPut**](#apiv1commercialprojectsidput) | **PUT** /api/v1/commercial/projects/{id} | 更新 Project 数据|
|[**apiV1CommercialProjectsPost**](#apiv1commercialprojectspost) | **POST** /api/v1/commercial/projects | 新建 Project|
|[**apiV1CommercialProjectsProjectIdContributionsContributionIdDelete**](#apiv1commercialprojectsprojectidcontributionscontributioniddelete) | **DELETE** /api/v1/commercial/projects/{projectId}/contributions/{contributionId} | 软删除贡献记录|
|[**apiV1CommercialProjectsProjectIdContributionsContributionIdPatch**](#apiv1commercialprojectsprojectidcontributionscontributionidpatch) | **PATCH** /api/v1/commercial/projects/{projectId}/contributions/{contributionId} | 更新贡献值|
|[**apiV1CommercialProjectsProjectIdContributionsGet**](#apiv1commercialprojectsprojectidcontributionsget) | **GET** /api/v1/commercial/projects/{projectId}/contributions | 获取项目贡献列表|
|[**apiV1CommercialProjectsProjectIdContributionsGroupedGet**](#apiv1commercialprojectsprojectidcontributionsgroupedget) | **GET** /api/v1/commercial/projects/{projectId}/contributions/grouped | 获取生效的最终结果|
|[**apiV1CommercialProjectsProjectIdContributionsPost**](#apiv1commercialprojectsprojectidcontributionspost) | **POST** /api/v1/commercial/projects/{projectId}/contributions | 添加项目贡献记录|
|[**apiV1CommercialProjectsProjectIdContributionsRecalculateGet**](#apiv1commercialprojectsprojectidcontributionsrecalculateget) | **GET** /api/v1/commercial/projects/{projectId}/contributions/recalculate | 重算结果|
|[**apiV1CommercialWithdrawalsGet**](#apiv1commercialwithdrawalsget) | **GET** /api/v1/commercial/withdrawals | 提现记录列表|
|[**apiV1CommercialWithdrawalsIdGet**](#apiv1commercialwithdrawalsidget) | **GET** /api/v1/commercial/withdrawals/{id} | 获取单个记录的详细信息|
|[**apiV1CommercialWithdrawalsIdStatusPatch**](#apiv1commercialwithdrawalsidstatuspatch) | **PATCH** /api/v1/commercial/withdrawals/{id}/status | 修改提现申请状态|
|[**apiV1CommercialWithdrawalsMeGet**](#apiv1commercialwithdrawalsmeget) | **GET** /api/v1/commercial/withdrawals/me | 查看我的提现记录|
|[**apiV1CommercialWithdrawalsPost**](#apiv1commercialwithdrawalspost) | **POST** /api/v1/commercial/withdrawals | 提出提现申请|
|[**apiV1PathsGet**](#apiv1pathsget) | **GET** /api/v1/paths | 获取子目录|
|[**apiV1RemoteSetsGet**](#apiv1remotesetsget) | **GET** /api/v1/remote-sets | 搜索 Set|
|[**apiV1RemoteSetsIdDependenciesGet**](#apiv1remotesetsiddependenciesget) | **GET** /api/v1/remote-sets/{id}/dependencies | 获取依赖此 Set 的所有 Set|
|[**apiV1RemoteSetsIdGet**](#apiv1remotesetsidget) | **GET** /api/v1/remote-sets/{id} | 获取 Set 详细信息|
|[**apiV1RemoteSetsIdRandomGet**](#apiv1remotesetsidrandomget) | **GET** /api/v1/remote-sets/{id}/random | 获取随机模板|
|[**apiV1RemoteSetsIdSourcesGet**](#apiv1remotesetsidsourcesget) | **GET** /api/v1/remote-sets/{id}/sources | 具体RemoteSet的Source列表|
|[**apiV1RemoteSetsIdSourcesPost**](#apiv1remotesetsidsourcespost) | **POST** /api/v1/remote-sets/{id}/sources | 为 Set 添加 Source|
|[**apiV1RemoteSetsPost**](#apiv1remotesetspost) | **POST** /api/v1/remote-sets | 新建 RemoteSet|
|[**apiV1ServersGet**](#apiv1serversget) | **GET** /api/v1/servers | 信任服务器列表|
|[**apiV1ServersIdDelete**](#apiv1serversiddelete) | **DELETE** /api/v1/servers/{id} | 删除服务器|
|[**apiV1ServersPost**](#apiv1serverspost) | **POST** /api/v1/servers | 添加服务器|
|[**apiV1TemplatesGet**](#apiv1templatesget) | **GET** /api/v1/templates | 模板列表（或根据Path）|
|[**apiV1TemplatesIdDownloadGet**](#apiv1templatesiddownloadget) | **GET** /api/v1/templates/{id}/download | 模板版本 schem 数据下载|
|[**apiV1TemplatesIdGet**](#apiv1templatesidget) | **GET** /api/v1/templates/{id} | 模板详细数据|
|[**apiV1TemplatesIdMovePatch**](#apiv1templatesidmovepatch) | **PATCH** /api/v1/templates/{id}/move | 改变模板目录|
|[**apiV1TemplatesIdThumbnailGet**](#apiv1templatesidthumbnailget) | **GET** /api/v1/templates/{id}/thumbnail | 模板最新版本的缩略图|
|[**apiV1TemplatesIdTransferPatch**](#apiv1templatesidtransferpatch) | **PATCH** /api/v1/templates/{id}/transfer | 转移模板权限|
|[**apiV1TemplatesIdVersionsGet**](#apiv1templatesidversionsget) | **GET** /api/v1/templates/{id}/versions | 模板版本列表/复杂检索|
|[**apiV1TemplatesThumbnailPost**](#apiv1templatesthumbnailpost) | **POST** /api/v1/templates/thumbnail | 上传缩略图|
|[**apiV1UserinfoCreatorsGet**](#apiv1userinfocreatorsget) | **GET** /api/v1/userinfo/creators | 获取所有模板作者的用户信息|
|[**createWithdraw**](#createwithdraw) | **POST** /api/v1/commercial/netease-withdraws | 添加提现记录|
|[**getMyBalance**](#getmybalance) | **GET** /api/v1/commercial/balance | 获取当前用户虚拟点数金额|
|[**listBalanceCheckoutDetails**](#listbalancecheckoutdetails) | **GET** /api/v1/commercial/balance/checkout-details | List Checkout Details|
|[**listBalanceRecords**](#listbalancerecords) | **GET** /api/v1/commercial/balance/records | 获取/筛选虚拟点数变动记录|
|[**listUserBalances**](#listuserbalances) | **GET** /api/v1/commercial/balance/users | 获取/筛选用户虚拟点数列表|
|[**listWithdraws**](#listwithdraws) | **GET** /api/v1/commercial/netease-withdraws | 分页查询提现记录|

# **apiAuthLoginPost**
> ApiAuthLoginPost200Response apiAuthLoginPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiAuthLoginPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiAuthLoginPostRequest: ApiAuthLoginPostRequest; // (optional)

const { status, data } = await apiInstance.apiAuthLoginPost(
    apiAuthLoginPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiAuthLoginPostRequest** | **ApiAuthLoginPostRequest**|  | |


### Return type

**ApiAuthLoginPost200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiAuthLogoutPost**
> object apiAuthLogoutPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authToken: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiAuthLogoutPost(
    authToken
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authToken** | [**string**] |  | (optional) defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiAuthMeGet**
> ApiAuthMeGet200Response apiAuthMeGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authToken: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiAuthMeGet(
    authToken
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authToken** | [**string**] |  | (optional) defaults to undefined|


### Return type

**ApiAuthMeGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsGet**
> object apiV1CollectionsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; // (optional) (default to undefined)
let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)
let sort: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CollectionsGet(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] |  | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|
| **sort** | [**string**] |  | (optional) defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsIdDelete**
> object apiV1CollectionsIdDelete()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1CollectionsIdDelete(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 删除成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsIdPut**
> TemplateCollection apiV1CollectionsIdPut()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CollectionsIdPutRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)
let apiV1CollectionsIdPutRequest: ApiV1CollectionsIdPutRequest; // (optional)

const { status, data } = await apiInstance.apiV1CollectionsIdPut(
    id,
    apiV1CollectionsIdPutRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CollectionsIdPutRequest** | **ApiV1CollectionsIdPutRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**TemplateCollection**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsIdRandomGet**
> Template apiV1CollectionsIdRandomGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1CollectionsIdRandomGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**Template**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsIdTemplatesGet**
> ApiV1CollectionsIdTemplatesGet200Response apiV1CollectionsIdTemplatesGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)
let search: number; // (optional) (default to undefined)
let page: string; // (optional) (default to undefined)
let size: string; // (optional) (default to undefined)
let sort: number; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CollectionsIdTemplatesGet(
    id,
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|
| **search** | [**number**] |  | (optional) defaults to undefined|
| **page** | [**string**] |  | (optional) defaults to undefined|
| **size** | [**string**] |  | (optional) defaults to undefined|
| **sort** | [**number**] |  | (optional) defaults to undefined|


### Return type

**ApiV1CollectionsIdTemplatesGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsIdTemplatesPost**
> object apiV1CollectionsIdTemplatesPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CollectionsIdTemplatesPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)
let apiV1CollectionsIdTemplatesPostRequest: ApiV1CollectionsIdTemplatesPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1CollectionsIdTemplatesPost(
    id,
    apiV1CollectionsIdTemplatesPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CollectionsIdTemplatesPostRequest** | **ApiV1CollectionsIdTemplatesPostRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsIdTemplatesTemplateIdDelete**
> Undefined apiV1CollectionsIdTemplatesTemplateIdDelete()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //合集 Id (default to undefined)
let templateId: string; //模板的 Uuid (default to undefined)

const { status, data } = await apiInstance.apiV1CollectionsIdTemplatesTemplateIdDelete(
    id,
    templateId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | 合集 Id | defaults to undefined|
| **templateId** | [**string**] | 模板的 Uuid | defaults to undefined|


### Return type

**Undefined**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 删除成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CollectionsPost**
> TemplateCollection apiV1CollectionsPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CollectionsPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiV1CollectionsPostRequest: ApiV1CollectionsPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1CollectionsPost(
    apiV1CollectionsPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CollectionsPostRequest** | **ApiV1CollectionsPostRequest**|  | |


### Return type

**TemplateCollection**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialAdminCalculateCheckoutDetailGet**
> object apiV1CommercialAdminCalculateCheckoutDetailGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialAdminCalculateCheckoutDetailGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialAdminCalculateOrderGet**
> object apiV1CommercialAdminCalculateOrderGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialAdminCalculateOrderGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialAdminCalculateReleaseGet**
> object apiV1CommercialAdminCalculateReleaseGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialAdminCalculateReleaseGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialAdminGet**
> object apiV1CommercialAdminGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialAdminGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |
|**406** | 无权限 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialAdminSyncNeOrderGet**
> object apiV1CommercialAdminSyncNeOrderGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialAdminSyncNeOrderGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialAdminSyncNeProductGet**
> object apiV1CommercialAdminSyncNeProductGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialAdminSyncNeProductGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialGlobalContextsCurrentGet**
> ApiV1CommercialGlobalContextsCurrentGet200Response apiV1CommercialGlobalContextsCurrentGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialGlobalContextsCurrentGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**ApiV1CommercialGlobalContextsCurrentGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialGlobalContextsGet**
> Array<ApiV1CommercialGlobalContextsGet200ResponseInner> apiV1CommercialGlobalContextsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialGlobalContextsGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ApiV1CommercialGlobalContextsGet200ResponseInner>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialGlobalContextsPost**
> CheckoutCalculateContext apiV1CommercialGlobalContextsPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CommercialGlobalContextsPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiV1CommercialGlobalContextsPostRequest: ApiV1CommercialGlobalContextsPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialGlobalContextsPost(
    apiV1CommercialGlobalContextsPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CommercialGlobalContextsPostRequest** | **ApiV1CommercialGlobalContextsPostRequest**|  | |


### Return type

**CheckoutCalculateContext**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseOrdersGet**
> ApiV1CommercialNeteaseProductsProductIdOrdersGet200Response apiV1CommercialNeteaseOrdersGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; // (optional) (default to undefined)
let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)
let sort: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialNeteaseOrdersGet(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] |  | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|
| **sort** | [**string**] |  | (optional) defaults to undefined|


### Return type

**ApiV1CommercialNeteaseProductsProductIdOrdersGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsGet**
> ApiV1CommercialNeteaseProductsGet200Response apiV1CommercialNeteaseProductsGet()

支持通过 search 参数进行动态字段过滤和模糊查询，返回分页结果。

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; //查询条件字符串，格式：`字段:值`（等值查询）或 `字段~:值`（模糊查询），多个条件用逗号分隔。 例如：`itemName~:测试,internalStatus:CREATED,project.id:123`。  (optional) (default to undefined)
let page: number; //页码，从0开始 (optional) (default to 0)
let size: number; //每页条数 (optional) (default to 20)
let sort: string; //排序字段，格式如 `id,desc` 或 `name,asc` (optional) (default to 'id,desc')

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsGet(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] | 查询条件字符串，格式：&#x60;字段:值&#x60;（等值查询）或 &#x60;字段~:值&#x60;（模糊查询），多个条件用逗号分隔。 例如：&#x60;itemName~:测试,internalStatus:CREATED,project.id:123&#x60;。  | (optional) defaults to undefined|
| **page** | [**number**] | 页码，从0开始 | (optional) defaults to 0|
| **size** | [**number**] | 每页条数 | (optional) defaults to 20|
| **sort** | [**string**] | 排序字段，格式如 &#x60;id,desc&#x60; 或 &#x60;name,asc&#x60; | (optional) defaults to 'id,desc'|


### Return type

**ApiV1CommercialNeteaseProductsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |
|**400** | 请求参数错误（如 search 格式不正确） |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsIdGet**
> NeteaseProduct apiV1CommercialNeteaseProductsIdGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; //产品ID (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] | 产品ID | defaults to undefined|


### Return type

**NeteaseProduct**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |
|**404** | 产品不存在 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsIdProjectPatch**
> NeteaseProductDto apiV1CommercialNeteaseProductsIdProjectPatch()

专用于修改产品的 project 关联。若请求体中的 projectId 为 null，则清除当前关联。

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ProjectAssignmentRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)
let projectAssignmentRequest: ProjectAssignmentRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsIdProjectPatch(
    id,
    projectAssignmentRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **projectAssignmentRequest** | **ProjectAssignmentRequest**|  | |
| **id** | [**number**] |  | defaults to undefined|


### Return type

**NeteaseProductDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 操作成功，返回更新后的产品 |  -  |
|**400** | 项目ID不存在 |  -  |
|**404** | 产品不存在 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsIdPut**
> NeteaseProductDto apiV1CommercialNeteaseProductsIdPut()

可用于更新关联项目（通过 projectId 或 clearProject 清除）、修改内部状态（internalStatus）。 未提供的字段保持不变。 

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    NeteaseProductUpdateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)
let neteaseProductUpdateRequest: NeteaseProductUpdateRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsIdPut(
    id,
    neteaseProductUpdateRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **neteaseProductUpdateRequest** | **NeteaseProductUpdateRequest**|  | |
| **id** | [**number**] |  | defaults to undefined|


### Return type

**NeteaseProductDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 更新成功，返回最新产品信息 |  -  |
|**400** | 请求参数错误（如项目ID不存在） |  -  |
|**404** | 产品不存在 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsIdStatsGet**
> ApiV1CommercialNeteaseProductsIdStatsGet200Response apiV1CommercialNeteaseProductsIdStatsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsIdStatsGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

**ApiV1CommercialNeteaseProductsIdStatsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsIdStatusPatch**
> NeteaseProductDto apiV1CommercialNeteaseProductsIdStatusPatch()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    StatusChangeRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)
let statusChangeRequest: StatusChangeRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsIdStatusPatch(
    id,
    statusChangeRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **statusChangeRequest** | **StatusChangeRequest**|  | |
| **id** | [**number**] |  | defaults to undefined|


### Return type

**NeteaseProductDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 状态更新成功 |  -  |
|**400** | 无效的状态值 |  -  |
|**404** | 产品不存在 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseProductsProductIdOrdersGet**
> ApiV1CommercialNeteaseProductsProductIdOrdersGet200Response apiV1CommercialNeteaseProductsProductIdOrdersGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let productId: string; // (default to undefined)
let search: string; // (optional) (default to undefined)
let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)
let sort: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsProductIdOrdersGet(
    productId,
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **productId** | [**string**] |  | defaults to undefined|
| **search** | [**string**] |  | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|
| **sort** | [**string**] |  | (optional) defaults to undefined|


### Return type

**ApiV1CommercialNeteaseProductsProductIdOrdersGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet**
> ApiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet200Response apiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet()

获取指定提现记录被分配到了哪些结账单（CheckoutDetail）中，包含扣除的原始金额和实际折算金额。

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let withdrawId: number; //提现记录的 ID (default to undefined)
let search: string; //动态过滤。支持嵌套字段，如 checkoutDetail.id:100 (optional) (default to undefined)
let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet(
    withdrawId,
    search,
    page,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **withdrawId** | [**number**] | 提现记录的 ID | defaults to undefined|
| **search** | [**string**] | 动态过滤。支持嵌套字段，如 checkoutDetail.id:100 | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|


### Return type

**ApiV1CommercialNeteaseWithdrawsWithdrawIdAllocationsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsGet**
> ApiV1CommercialProjectsGet200Response apiV1CommercialProjectsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; // (default to undefined)
let page: number; // (default to undefined)
let size: number; // (default to undefined)
let sort: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsGet(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] |  | defaults to undefined|
| **page** | [**number**] |  | defaults to undefined|
| **size** | [**number**] |  | defaults to undefined|
| **sort** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1CommercialProjectsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsIdDelete**
> object apiV1CommercialProjectsIdDelete()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsIdDelete(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 删除成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsIdGet**
> Project apiV1CommercialProjectsIdGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

**Project**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsIdPut**
> Project apiV1CommercialProjectsIdPut()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CommercialProjectsIdPutRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)
let apiV1CommercialProjectsIdPutRequest: ApiV1CommercialProjectsIdPutRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialProjectsIdPut(
    id,
    apiV1CommercialProjectsIdPutRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CommercialProjectsIdPutRequest** | **ApiV1CommercialProjectsIdPutRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**Project**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsPost**
> Project apiV1CommercialProjectsPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CommercialProjectsPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiV1CommercialProjectsPostRequest: ApiV1CommercialProjectsPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialProjectsPost(
    apiV1CommercialProjectsPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CommercialProjectsPostRequest** | **ApiV1CommercialProjectsPostRequest**|  | |


### Return type

**Project**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsContributionIdDelete**
> object apiV1CommercialProjectsProjectIdContributionsContributionIdDelete()

软删除指定的贡献记录，填写删除原因。删除后系统会自动重算该记录所属角色的其他剩余有效记录的占比。

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ContributionDeleteRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

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
|**200** | 成功软删除并完成剩余数据占比重算 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsContributionIdPatch**
> object apiV1CommercialProjectsProjectIdContributionsContributionIdPatch()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CommercialProjectsProjectIdContributionsContributionIdPatchRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let projectId: string; // (default to undefined)
let contributionId: string; // (default to undefined)
let apiV1CommercialProjectsProjectIdContributionsContributionIdPatchRequest: ApiV1CommercialProjectsProjectIdContributionsContributionIdPatchRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsContributionIdPatch(
    projectId,
    contributionId,
    apiV1CommercialProjectsProjectIdContributionsContributionIdPatchRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CommercialProjectsProjectIdContributionsContributionIdPatchRequest** | **ApiV1CommercialProjectsProjectIdContributionsContributionIdPatchRequest**|  | |
| **projectId** | [**string**] |  | defaults to undefined|
| **contributionId** | [**string**] |  | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsGet**
> Array<UserProjectContribution> apiV1CommercialProjectsProjectIdContributionsGet()

获取指定项目及其可能存在的父项目下，所有未被软删除的贡献记录列表。

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

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
|**200** | 成功获取贡献列表 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsGroupedGet**
> ApiV1CommercialProjectsProjectIdContributionsGroupedGet200Response apiV1CommercialProjectsProjectIdContributionsGroupedGet()

对于 BUILDER，将返回父项目的结果 对于 MODIFIER 和 UPLOADER，将返回本项目的结果

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let projectId: number; // (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsGroupedGet(
    projectId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **projectId** | [**number**] |  | defaults to undefined|


### Return type

**ApiV1CommercialProjectsProjectIdContributionsGroupedGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsPost**
> object apiV1CommercialProjectsProjectIdContributionsPost()

添加一条新的贡献记录，系统会自动汇总该记录所属角色（Role）的所有有效分数，并重新计算和更新该角色的所有人占比（contributeRatio）。

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ContributionAddRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

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
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功添加并完成占比重算 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialProjectsProjectIdContributionsRecalculateGet**
> { [key: string]: RecalculatePreviewResponse; } apiV1CommercialProjectsProjectIdContributionsRecalculateGet()

重新计算该项目下所有角色分别对应的 contributeRatio，返回计算过程的数据供前端展示，**不会写入数据库**。

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let projectId: number; //项目ID (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialProjectsProjectIdContributionsRecalculateGet(
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
|**200** | 成功获取重算预览数据 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialWithdrawalsGet**
> ApiV1CommercialWithdrawalsGet200Response apiV1CommercialWithdrawalsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)
let sort: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialWithdrawalsGet(
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|
| **sort** | [**string**] |  | (optional) defaults to undefined|


### Return type

**ApiV1CommercialWithdrawalsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialWithdrawalsIdGet**
> SystemWithdraw apiV1CommercialWithdrawalsIdGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: number; // (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialWithdrawalsIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**number**] |  | defaults to undefined|


### Return type

**SystemWithdraw**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialWithdrawalsIdStatusPatch**
> object apiV1CommercialWithdrawalsIdStatusPatch()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CommercialWithdrawalsIdStatusPatchRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)
let apiV1CommercialWithdrawalsIdStatusPatchRequest: ApiV1CommercialWithdrawalsIdStatusPatchRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialWithdrawalsIdStatusPatch(
    id,
    apiV1CommercialWithdrawalsIdStatusPatchRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CommercialWithdrawalsIdStatusPatchRequest** | **ApiV1CommercialWithdrawalsIdStatusPatchRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


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
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialWithdrawalsMeGet**
> ApiV1CommercialWithdrawalsGet200Response apiV1CommercialWithdrawalsMeGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)
let sort: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1CommercialWithdrawalsMeGet(
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|
| **sort** | [**string**] |  | (optional) defaults to undefined|


### Return type

**ApiV1CommercialWithdrawalsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1CommercialWithdrawalsPost**
> SystemWithdraw apiV1CommercialWithdrawalsPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1CommercialWithdrawalsPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiV1CommercialWithdrawalsPostRequest: ApiV1CommercialWithdrawalsPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1CommercialWithdrawalsPost(
    apiV1CommercialWithdrawalsPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1CommercialWithdrawalsPostRequest** | **ApiV1CommercialWithdrawalsPostRequest**|  | |


### Return type

**SystemWithdraw**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1PathsGet**
> Array<ApiV1PathsGet200ResponseInner> apiV1PathsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let path: string; //路径 (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1PathsGet(
    path
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **path** | [**string**] | 路径 | (optional) defaults to undefined|


### Return type

**Array<ApiV1PathsGet200ResponseInner>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsGet**
> ApiV1RemoteSetsGet200Response apiV1RemoteSetsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let name: string; // (optional) (default to undefined)
let namespace: string; // (optional) (default to undefined)
let pang: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1RemoteSetsGet(
    name,
    namespace,
    pang,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **name** | [**string**] |  | (optional) defaults to undefined|
| **namespace** | [**string**] |  | (optional) defaults to undefined|
| **pang** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|


### Return type

**ApiV1RemoteSetsGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsIdDependenciesGet**
> object apiV1RemoteSetsIdDependenciesGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1RemoteSetsIdDependenciesGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsIdGet**
> ApiV1RemoteSetsIdGet200Response apiV1RemoteSetsIdGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //set 的 id (default to undefined)

const { status, data } = await apiInstance.apiV1RemoteSetsIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | set 的 id | defaults to undefined|


### Return type

**ApiV1RemoteSetsIdGet200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsIdRandomGet**
> ApiV1RemoteSetsIdGet200ResponsePreviewTemplatesInner apiV1RemoteSetsIdRandomGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1RemoteSetsIdRandomGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1RemoteSetsIdGet200ResponsePreviewTemplatesInner**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsIdSourcesGet**
> object apiV1RemoteSetsIdSourcesGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //Set 的 Id (default to undefined)

const { status, data } = await apiInstance.apiV1RemoteSetsIdSourcesGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | Set 的 Id | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsIdSourcesPost**
> ApiV1RemoteSetsIdSourcesPost200Response apiV1RemoteSetsIdSourcesPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1RemoteSetsIdSourcesPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)
let apiV1RemoteSetsIdSourcesPostRequest: ApiV1RemoteSetsIdSourcesPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1RemoteSetsIdSourcesPost(
    id,
    apiV1RemoteSetsIdSourcesPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1RemoteSetsIdSourcesPostRequest** | **ApiV1RemoteSetsIdSourcesPostRequest**|  | |
| **id** | [**string**] |  | defaults to undefined|


### Return type

**ApiV1RemoteSetsIdSourcesPost200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1RemoteSetsPost**
> ApiV1RemoteSetsPost200Response apiV1RemoteSetsPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1RemoteSetsPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiV1RemoteSetsPostRequest: ApiV1RemoteSetsPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1RemoteSetsPost(
    apiV1RemoteSetsPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1RemoteSetsPostRequest** | **ApiV1RemoteSetsPostRequest**|  | |


### Return type

**ApiV1RemoteSetsPost200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1ServersGet**
> Array<ApiV1ServersGet200ResponseInner> apiV1ServersGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1ServersGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ApiV1ServersGet200ResponseInner>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1ServersIdDelete**
> object apiV1ServersIdDelete()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; // (default to undefined)

const { status, data } = await apiInstance.apiV1ServersIdDelete(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] |  | defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1ServersPost**
> ApiV1ServersPost200Response apiV1ServersPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1ServersPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let apiV1ServersPostRequest: ApiV1ServersPostRequest; // (optional)

const { status, data } = await apiInstance.apiV1ServersPost(
    apiV1ServersPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1ServersPostRequest** | **ApiV1ServersPostRequest**|  | |


### Return type

**ApiV1ServersPost200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesGet**
> TemplatePagenable apiV1TemplatesGet()

# 模板搜索接口文档  ## 1. 搜索模板列表  支持复合条件筛选（模糊匹配、精确匹配、范围查询）、分页以及多字段排序。  - **URL**: `/api/v1/templates` - **Method**: `GET` - **Content-Type**: `application/json`  ### 请求参数 (Query Parameters)  | 参数名 | 类型 | 必填 | 默认值 | 说明 | | :--- | :--- | :--- | :--- | :--- | | `pathPrefix` | string | 否 | - | **路径前缀匹配**<br>搜索以该路径开头的模板 (SQL: `LIKE \'val%\'`) | | `creatorId` | UUID | 否 | - | **创建者ID** (精确匹配) | | `worldId` | UUID | 否 | - | **所属世界ID** (精确匹配) | | `locked` | boolean | 否 | - | **锁定状态**<br>`true`: 仅看锁定; `false`: 仅看未锁定 | | `minWidth` | integer | 否 | - | **最小宽度** (包含) | | `maxWidth` | integer | 否 | - | **最大宽度** (包含) | | `minHeight` | integer | 否 | - | **最小高度** (包含) | | `maxHeight` | integer | 否 | - | **最大高度** (包含) | | `minLength` | integer | 否 | - | **最小长度** (包含) | | `maxLength` | integer | 否 | - | **最大长度** (包含) | | `page` | integer | 否 | `0` | **页码** (从 0 开始) | | `size` | integer | 否 | `20` | **每页条数** | | `sort` | string[] | 否 | `path,asc` | **排序规则**<br>格式: `字段名,方向`<br>方向: `asc`(升序), `desc`(降序)<br>支持传多个 sort 参数 |  ---  ### 排序字段说明 (Sort Fields)  排序参数格式为 `sort=字段名,方向`。 **注意**：元数据相关的字段必须加上 `metadata.` 前缀。  #### A. 基础属性 | 字段名 | 说明 | 示例 | | :--- | :--- | :--- | | `path` | 按文件路径/名称排序 (默认) | `sort=path,asc` | | `name` | 按模板显示名称排序 | `sort=name,desc` |  #### B. 时间与状态 (Metadata) | 字段名 | 说明 | 示例 | | :--- | :--- | :--- | | `metadata.creationTime` | 按创建时间排序 (时间戳) | `sort=metadata.creationTime,desc` (最新在前) | | `metadata.locked` | 按锁定状态排序 | `sort=metadata.locked,desc` (锁定的在前) | | `metadata.lockedTimestamp` | 按锁定时间排序 | - | | `metadata.deletedTimestamp` | 按删除时间排序 | - |  #### C. 尺寸与空间 (Metadata) | 字段名 | 说明 | 示例 | | :--- | :--- | :--- | | `metadata.width` | 按宽度 (X轴跨度) 排序 | `sort=metadata.width,desc` (最宽的在前) | | `metadata.height` | 按高度 (Y轴跨度) 排序 | `sort=metadata.height,desc` (最高的在前) | | `metadata.length` | 按长度 (Z轴跨度) 排序 | `sort=metadata.length,desc` (最长的在前) | | `metadata.anchorX` | 按锚点 X 坐标排序 | - | | `metadata.anchorY` | 按锚点 Y 坐标排序 | - | | `metadata.anchorZ` | 按锚点 Z 坐标排序 | - |  ---  ### 请求示例  **示例场景**： 查找 `users/` 目录下，宽度在 10 到 200 之间，且未锁定的模板。 结果按“创建时间倒序”排列（最新的在最前），每页 20 条。  ```http GET /api/v1/templates?pathPrefix=users/&locked=false&minWidth=10&maxWidth=200&page=0&size=20&sort=metadata.creationTime,desc

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let pathPrefix: string; // (optional) (default to undefined)
let locked: boolean; //是否锁定 (optional) (default to undefined)
let minWidth: number; //最小尺寸 (optional) (default to undefined)
let maxWidth: number; //最大尺寸 (optional) (default to undefined)
let worldId: string; //世界 uuid (optional) (default to undefined)
let page: number; // (optional) (default to undefined)
let size: number; // (optional) (default to undefined)
let sort: string; // (optional) (default to undefined)
let minLength: number; // (optional) (default to undefined)
let maxLength: number; // (optional) (default to undefined)
let minHeight: number; // (optional) (default to undefined)
let maxHeight: number; // (optional) (default to undefined)
let pathLike: string; //模糊搜索 Path (optional) (default to undefined)
let nameLike: string; //模糊搜索 Name (optional) (default to undefined)
let creatorId: string; //创建玩家的 uuid，单个，精确匹配 (optional) (default to undefined)
let sortByLatestVersionTime: boolean; //是否按照最新更新时间排序 (optional) (default to undefined)
let versionMessageLike: string; //模糊搜索更新文本 (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesGet(
    pathPrefix,
    locked,
    minWidth,
    maxWidth,
    worldId,
    page,
    size,
    sort,
    minLength,
    maxLength,
    minHeight,
    maxHeight,
    pathLike,
    nameLike,
    creatorId,
    sortByLatestVersionTime,
    versionMessageLike
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **pathPrefix** | [**string**] |  | (optional) defaults to undefined|
| **locked** | [**boolean**] | 是否锁定 | (optional) defaults to undefined|
| **minWidth** | [**number**] | 最小尺寸 | (optional) defaults to undefined|
| **maxWidth** | [**number**] | 最大尺寸 | (optional) defaults to undefined|
| **worldId** | [**string**] | 世界 uuid | (optional) defaults to undefined|
| **page** | [**number**] |  | (optional) defaults to undefined|
| **size** | [**number**] |  | (optional) defaults to undefined|
| **sort** | [**string**] |  | (optional) defaults to undefined|
| **minLength** | [**number**] |  | (optional) defaults to undefined|
| **maxLength** | [**number**] |  | (optional) defaults to undefined|
| **minHeight** | [**number**] |  | (optional) defaults to undefined|
| **maxHeight** | [**number**] |  | (optional) defaults to undefined|
| **pathLike** | [**string**] | 模糊搜索 Path | (optional) defaults to undefined|
| **nameLike** | [**string**] | 模糊搜索 Name | (optional) defaults to undefined|
| **creatorId** | [**string**] | 创建玩家的 uuid，单个，精确匹配 | (optional) defaults to undefined|
| **sortByLatestVersionTime** | [**boolean**] | 是否按照最新更新时间排序 | (optional) defaults to undefined|
| **versionMessageLike** | [**string**] | 模糊搜索更新文本 | (optional) defaults to undefined|


### Return type

**TemplatePagenable**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdDownloadGet**
> object apiV1TemplatesIdDownloadGet()

*  /api/v1/templates/{id}/download (下载最新版) * /api/v1/templates/{id}/download?version=1706781234000 (下载指定版)

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //模板的 UUID (default to undefined)
let version: string; //模板的版本号，不填则下载最新版 (optional) (default to undefined)
let format: string; //litematica | litematic | nbt | becrock | structure | structure_block | schem | sponge | bp | axiom (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdDownloadGet(
    id,
    version,
    format
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | 模板的 UUID | defaults to undefined|
| **version** | [**string**] | 模板的版本号，不填则下载最新版 | (optional) defaults to undefined|
| **format** | [**string**] | litematica | litematic | nbt | becrock | structure | structure_block | schem | sponge | bp | axiom | (optional) defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdGet**
> Template apiV1TemplatesIdGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //模板的 UUID (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdGet(
    id
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | 模板的 UUID | defaults to undefined|


### Return type

**Template**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdMovePatch**
> object apiV1TemplatesIdMovePatch()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1TemplatesIdMovePatchRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //模板id (default to undefined)
let apiV1TemplatesIdMovePatchRequest: ApiV1TemplatesIdMovePatchRequest; // (optional)

const { status, data } = await apiInstance.apiV1TemplatesIdMovePatch(
    id,
    apiV1TemplatesIdMovePatchRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1TemplatesIdMovePatchRequest** | **ApiV1TemplatesIdMovePatchRequest**|  | |
| **id** | [**string**] | 模板id | defaults to undefined|


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
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdThumbnailGet**
> object apiV1TemplatesIdThumbnailGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //模板的 UUID (default to undefined)
let angle: number; //0, 1, 2, 3 (default to undefined)
let refresh: boolean; //是否强制刷新 (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdThumbnailGet(
    id,
    angle,
    refresh
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | 模板的 UUID | defaults to undefined|
| **angle** | [**number**] | 0, 1, 2, 3 | defaults to undefined|
| **refresh** | [**boolean**] | 是否强制刷新 | (optional) defaults to undefined|


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdTransferPatch**
> object apiV1TemplatesIdTransferPatch()



### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ApiV1TemplatesIdTransferPatchRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //模板id (default to undefined)
let apiV1TemplatesIdTransferPatchRequest: ApiV1TemplatesIdTransferPatchRequest; // (optional)

const { status, data } = await apiInstance.apiV1TemplatesIdTransferPatch(
    id,
    apiV1TemplatesIdTransferPatchRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiV1TemplatesIdTransferPatchRequest** | **ApiV1TemplatesIdTransferPatchRequest**|  | |
| **id** | [**string**] | 模板id | defaults to undefined|


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
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesIdVersionsGet**
> TemplateVersionPagenable apiV1TemplatesIdVersionsGet()

# 模板版本查询接口  获取特定模板的历史版本列表。支持针对提交信息、提交者、时间的复杂筛选，以及分页排序功能。  - **URL**: `/api/v1/templates/{id}/versions` - **Method**: `GET` - **Auth**: Required (Bearer Token)  ## 路径参数 (Path Parameters)  | 参数名 | 类型 | 必填 | 说明 | | :--- | :--- | :--- | :--- | | `id` | UUID | **是** | **模板的唯一标识符**<br>例如: `500d59d1-84a8-3207-932c-e62f8a543ef7` |  ## 查询参数 (Query Parameters)  用于筛选和分页的参数，均以 Query String 形式传递 (例如 `?page=0&messageKeyword=fix`)。  ### 1. 筛选条件 (Filtering)  | 参数名 | 类型 | 说明 | 示例 | | :--- | :--- | :--- | :--- | | `messageKeyword` | String | **提交信息模糊搜索**<br>搜索包含该关键词的版本留言 (Case-insensitive)。 | `fix lighting` | | `submitterId` | UUID | **提交者 ID**<br>仅显示指定用户提交的版本。 | `0caee9cd-6987...` | | `versionId` | String | **版本业务 ID**<br>精确匹配特定的版本号 (通常是时间戳字符串)。 | `1706781234000` | | `minCreatedAt` | Long | **最小创建时间 (时间戳)**<br>筛选在此时间**之后** (>=) 提交的版本。 | `1706000000000` | | `maxCreatedAt` | Long | **最大创建时间 (时间戳)**<br>筛选在此时间**之前** (<=) 提交的版本。 | `1706999999999` |  ### 2. 分页与排序 (Pagination & Sorting)  | 参数名 | 类型 | 默认值 | 说明 | | :--- | :--- | :--- | :--- | | `page` | Integer | `0` | **页码** (从 0 开始)。 | | `size` | Integer | `20` | **每页条数**。 | | `sort` | String | `createdAt,desc` | **排序规则**<br>格式: `字段名,方向`<br>支持字段: `createdAt`, `versionId`, `submitterId` |  ---  ## 请求示例 (Request Examples)  ### 场景 A: 查看某模板最近的提交记录 (默认) ```http GET /api/v1/templates/500d59d1-84a8-3207-932c-e62f8a543ef7/versions ```  ### 场景 B: 搜索包含 \"bugfix\" 的提交，且由特定用户提交 ```http GET /api/v1/templates/500d59d1.../versions?messageKeyword=bugfix&submitterId=0caee9cd... ```  ### 场景 C: 查找 2024年1月1日 之后的版本，按时间正序排列 ```http GET /api/v1/templates/500d59d1.../versions?minCreatedAt=1704067200000&sort=createdAt,asc ```

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let id: string; //模板的 UUID (default to undefined)
let messageKeyword: string; //提交信息模糊搜索：搜索包含该关键词的版本留言 (Case-insensitive) (optional) (default to undefined)
let submitterId: string; //提交者 ID：仅显示指定用户提交的版本。 (optional) (default to undefined)
let versionId: string; //版本业务 ID：精确匹配特定的版本号 (通常是时间戳字符串) (optional) (default to undefined)
let minCreatedAt: string; //最小创建时间 (时间戳，ms)：筛选在此时间**之后** (>=) 提交的版本 (optional) (default to undefined)
let maxCreatedAt: string; //最大创建时间 (时间戳，ms)：筛选在此时间**之前** (<=) 提交的版本 (optional) (default to undefined)
let page: string; //页码：(从 0 开始) (optional) (default to undefined)
let size: string; //每页条数 (optional) (default to undefined)
let sort: string; //排序规则，参见文档 (optional) (default to undefined)

const { status, data } = await apiInstance.apiV1TemplatesIdVersionsGet(
    id,
    messageKeyword,
    submitterId,
    versionId,
    minCreatedAt,
    maxCreatedAt,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **id** | [**string**] | 模板的 UUID | defaults to undefined|
| **messageKeyword** | [**string**] | 提交信息模糊搜索：搜索包含该关键词的版本留言 (Case-insensitive) | (optional) defaults to undefined|
| **submitterId** | [**string**] | 提交者 ID：仅显示指定用户提交的版本。 | (optional) defaults to undefined|
| **versionId** | [**string**] | 版本业务 ID：精确匹配特定的版本号 (通常是时间戳字符串) | (optional) defaults to undefined|
| **minCreatedAt** | [**string**] | 最小创建时间 (时间戳，ms)：筛选在此时间**之后** (&gt;&#x3D;) 提交的版本 | (optional) defaults to undefined|
| **maxCreatedAt** | [**string**] | 最大创建时间 (时间戳，ms)：筛选在此时间**之前** (&lt;&#x3D;) 提交的版本 | (optional) defaults to undefined|
| **page** | [**string**] | 页码：(从 0 开始) | (optional) defaults to undefined|
| **size** | [**string**] | 每页条数 | (optional) defaults to undefined|
| **sort** | [**string**] | 排序规则，参见文档 | (optional) defaults to undefined|


### Return type

**TemplateVersionPagenable**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1TemplatesThumbnailPost**
> object apiV1TemplatesThumbnailPost()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let body: string; // (optional)

const { status, data } = await apiInstance.apiV1TemplatesThumbnailPost(
    body
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **string**|  | |


### Return type

**object**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: text/plain
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **apiV1UserinfoCreatorsGet**
> Array<ApiV1UserinfoCreatorsGet200ResponseInner> apiV1UserinfoCreatorsGet()



### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1UserinfoCreatorsGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ApiV1UserinfoCreatorsGet200ResponseInner>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createWithdraw**
> NeteaseWithdraw createWithdraw()

创建一条新的提现记录。系统将根据传入的原始金额和实际提现金额自动计算手续费比例（Ratio）。

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    NeteaseWithdrawInput
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let neteaseWithdrawInput: NeteaseWithdrawInput; // (optional)

const { status, data } = await apiInstance.createWithdraw(
    neteaseWithdrawInput
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **neteaseWithdrawInput** | **NeteaseWithdrawInput**|  | |


### Return type

**NeteaseWithdraw**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 记录创建成功 |  -  |
|**400** | 请求参数错误 |  -  |
|**401** | 未授权或 Token 过期 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getMyBalance**
> BalanceInfo getMyBalance()

Retrieves the balance information for the currently authenticated user. Requires authentication.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.getMyBalance();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**BalanceInfo**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successful retrieval of the user\&#39;s balance information. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listBalanceCheckoutDetails**
> PageCheckoutDetailDto listBalanceCheckoutDetails()

Searches checkout details with dynamic conditions and pagination. Requires authentication.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; //Dynamic search query string. Format: `field:value` (exact match) or `field~:value` (fuzzy match for strings). Multiple conditions can be separated by commas.  (optional) (default to undefined)
let page: number; //Zero-based page index (0..N). Default is 0. (optional) (default to 0)
let size: number; //The size of the page to be returned. Default is 20. (optional) (default to 20)
let sort: string; //Sorting criteria in the format `property,asc|desc`. Default is `id,desc`. (optional) (default to 'id,desc')

const { status, data } = await apiInstance.listBalanceCheckoutDetails(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] | Dynamic search query string. Format: &#x60;field:value&#x60; (exact match) or &#x60;field~:value&#x60; (fuzzy match for strings). Multiple conditions can be separated by commas.  | (optional) defaults to undefined|
| **page** | [**number**] | Zero-based page index (0..N). Default is 0. | (optional) defaults to 0|
| **size** | [**number**] | The size of the page to be returned. Default is 20. | (optional) defaults to 20|
| **sort** | [**string**] | Sorting criteria in the format &#x60;property,asc|desc&#x60;. Default is &#x60;id,desc&#x60;. | (optional) defaults to 'id,desc'|


### Return type

**PageCheckoutDetailDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successful retrieval of the checkout details page. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listBalanceRecords**
> PageUserBalanceRecordDto listBalanceRecords()

Searches user balance transaction records with dynamic conditions and pagination. Requires authentication.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; //Dynamic search query string. Format: `field:value` (exact match) or `field~:value` (fuzzy match for strings). Multiple conditions can be separated by commas.  (optional) (default to undefined)
let page: number; //Zero-based page index (0..N). Default is 0. (optional) (default to 0)
let size: number; //The size of the page to be returned. Default is 20. (optional) (default to 20)
let sort: string; //Sorting criteria in the format `property,asc|desc`. Default is `createTimeMs,desc`. (optional) (default to 'createTimeMs,desc')

const { status, data } = await apiInstance.listBalanceRecords(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] | Dynamic search query string. Format: &#x60;field:value&#x60; (exact match) or &#x60;field~:value&#x60; (fuzzy match for strings). Multiple conditions can be separated by commas.  | (optional) defaults to undefined|
| **page** | [**number**] | Zero-based page index (0..N). Default is 0. | (optional) defaults to 0|
| **size** | [**number**] | The size of the page to be returned. Default is 20. | (optional) defaults to 20|
| **sort** | [**string**] | Sorting criteria in the format &#x60;property,asc|desc&#x60;. Default is &#x60;createTimeMs,desc&#x60;. | (optional) defaults to 'createTimeMs,desc'|


### Return type

**PageUserBalanceRecordDto**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successful retrieval of the balance records page. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listUserBalances**
> PageBalanceInfo listUserBalances()

Retrieves a paginated list of balance information for all system users. Supports filtering by username. Requires authentication.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; //Dynamic search query string. Format: `username:value` (exact match) or `username~:value` (fuzzy match). Examples: `username:player1`, `username~:player`  (optional) (default to undefined)
let page: number; //Zero-based page index (0..N). Default is 0. (optional) (default to 0)
let size: number; //The size of the page to be returned. Default is 20. (optional) (default to 20)
let sort: string; //Sorting criteria in the format `property,asc|desc`. Default is `username,asc`. (optional) (default to 'username,asc')

const { status, data } = await apiInstance.listUserBalances(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] | Dynamic search query string. Format: &#x60;username:value&#x60; (exact match) or &#x60;username~:value&#x60; (fuzzy match). Examples: &#x60;username:player1&#x60;, &#x60;username~:player&#x60;  | (optional) defaults to undefined|
| **page** | [**number**] | Zero-based page index (0..N). Default is 0. | (optional) defaults to 0|
| **size** | [**number**] | The size of the page to be returned. Default is 20. | (optional) defaults to 20|
| **sort** | [**string**] | Sorting criteria in the format &#x60;property,asc|desc&#x60;. Default is &#x60;username,asc&#x60;. | (optional) defaults to 'username,asc'|


### Return type

**PageBalanceInfo**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Successful retrieval of the paginated list of user balances. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listWithdraws**
> ListWithdraws200Response listWithdraws()

获取所有提现记录列表。支持通过 search 参数进行动态过滤（例如按用户名、金额或比例筛选），支持标准分页参数。

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let search: string; //动态查询条件。格式：字段:值（精确），字段~:值（模糊）。多个条件用逗号分隔。示例：username:admin,originalValue:100.00 (optional) (default to undefined)
let page: number; //页码（从 0 开始） (optional) (default to 0)
let size: number; //每页条数 (optional) (default to 20)
let sort: string; //排序字段及方向。格式：字段,asc|desc。默认：saveTimeMs,desc (optional) (default to 'saveTimeMs,desc')

const { status, data } = await apiInstance.listWithdraws(
    search,
    page,
    size,
    sort
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **search** | [**string**] | 动态查询条件。格式：字段:值（精确），字段~:值（模糊）。多个条件用逗号分隔。示例：username:admin,originalValue:100.00 | (optional) defaults to undefined|
| **page** | [**number**] | 页码（从 0 开始） | (optional) defaults to 0|
| **size** | [**number**] | 每页条数 | (optional) defaults to 20|
| **sort** | [**string**] | 排序字段及方向。格式：字段,asc|desc。默认：saveTimeMs,desc | (optional) defaults to 'saveTimeMs,desc'|


### Return type

**ListWithdraws200Response**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 成功 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

