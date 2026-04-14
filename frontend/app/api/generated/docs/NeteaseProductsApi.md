# NeteaseProductsApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1CommercialNeteaseProductsGet**](#apiv1commercialneteaseproductsget) | **GET** /api/v1/commercial/products | 分页查询产品列表|
|[**apiV1CommercialNeteaseProductsIdGet**](#apiv1commercialneteaseproductsidget) | **GET** /api/v1/commercial/products/{id} | 根据ID查询单个产品|
|[**apiV1CommercialNeteaseProductsIdProjectPatch**](#apiv1commercialneteaseproductsidprojectpatch) | **PATCH** /api/v1/commercial/products/{id}/project | 指派或清除项目关联|
|[**apiV1CommercialNeteaseProductsIdPut**](#apiv1commercialneteaseproductsidput) | **PUT** /api/v1/commercial/products/{id} | 更新产品信息（支持部分字段）|
|[**apiV1CommercialNeteaseProductsIdStatusPatch**](#apiv1commercialneteaseproductsidstatuspatch) | **PATCH** /api/v1/commercial/products/{id}/status | 修改产品状态|

# **apiV1CommercialNeteaseProductsGet**
> ApiV1CommercialNeteaseProductsGet200Response apiV1CommercialNeteaseProductsGet()

支持通过 search 参数进行动态字段过滤和模糊查询，返回分页结果。

### Example

```typescript
import {
    NeteaseProductsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NeteaseProductsApi(configuration);

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
    NeteaseProductsApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new NeteaseProductsApi(configuration);

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
    NeteaseProductsApi,
    Configuration,
    ProjectAssignmentRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new NeteaseProductsApi(configuration);

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
    NeteaseProductsApi,
    Configuration,
    NeteaseProductUpdateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new NeteaseProductsApi(configuration);

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

# **apiV1CommercialNeteaseProductsIdStatusPatch**
> NeteaseProductDto apiV1CommercialNeteaseProductsIdStatusPatch()



### Example

```typescript
import {
    NeteaseProductsApi,
    Configuration,
    StatusChangeRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new NeteaseProductsApi(configuration);

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

