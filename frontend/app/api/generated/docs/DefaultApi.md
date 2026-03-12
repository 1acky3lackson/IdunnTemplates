# DefaultApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiAuthLoginPost**](#apiauthloginpost) | **POST** /api/auth/login | Yggdrasil 登录|
|[**apiAuthLogoutPost**](#apiauthlogoutpost) | **POST** /api/auth/logout | 查看个人信息 / 验证登录 Copy|
|[**apiAuthMeGet**](#apiauthmeget) | **GET** /api/auth/me | 查看个人信息 / 验证登录|
|[**apiV1CommercialGlobalContextsCurrentGet**](#apiv1commercialglobalcontextscurrentget) | **GET** /api/v1/commercial/global-contexts/current | 获取当前激活的结算参数|
|[**apiV1CommercialGlobalContextsGet**](#apiv1commercialglobalcontextsget) | **GET** /api/v1/commercial/global-contexts | 获取全部修改历史记录|
|[**apiV1CommercialGlobalContextsPost**](#apiv1commercialglobalcontextspost) | **POST** /api/v1/commercial/global-contexts | 更新结算参数|
|[**apiV1CommercialNeteaseOrdersGet**](#apiv1commercialneteaseordersget) | **GET** /api/v1/commercial/netease-orders | 获取/筛选订单列表|
|[**apiV1CommercialNeteaseProductsGet**](#apiv1commercialneteaseproductsget) | **GET** /api/v1/commercial/netease-products | 获取/搜索商品列表|
|[**apiV1CommercialNeteaseProductsProductIdOrdersGet**](#apiv1commercialneteaseproductsproductidordersget) | **GET** /api/v1/commercial/netease-products/{productId}/orders | 获取商品的所有订单|
|[**apiV1PathsGet**](#apiv1pathsget) | **GET** /api/v1/paths | 获取子目录|
|[**apiV1RemoteSetsGet**](#apiv1remotesetsget) | **GET** /api/v1/remote-sets | 搜索 Set|
|[**apiV1RemoteSetsIdDependenciesGet**](#apiv1remotesetsiddependenciesget) | **GET** /api/v1/remote-sets/{id}/dependencies | 获取依赖此 Set 的所有 Set|
|[**apiV1RemoteSetsIdGet**](#apiv1remotesetsidget) | **GET** /api/v1/remote-sets/{id} | 获取 Set 详细信息|
|[**apiV1RemoteSetsIdRandomGet**](#apiv1remotesetsidrandomget) | **GET** /api/v1/remote-sets/{id}/random | 获取随机模板|
|[**apiV1RemoteSetsIdSourcesGet**](#apiv1remotesetsidsourcesget) | **GET** /api/v1/remote-sets/{id}/sources | 具体RemoteSet的Source列表|
|[**apiV1RemoteSetsIdSourcesPost**](#apiv1remotesetsidsourcespost) | **POST** /api/v1/remote-sets/{id}/sources | 为 Set 添加 Source|
|[**apiV1RemoteSetsPost**](#apiv1remotesetspost) | **POST** /api/v1/remote-sets | 新建 RemoteSet|
|[**apiV1TemplatesGet**](#apiv1templatesget) | **GET** /api/v1/templates | 模板列表（或根据Path）|
|[**apiV1TemplatesIdDownloadGet**](#apiv1templatesiddownloadget) | **GET** /api/v1/templates/{id}/download | 模板版本 schem 数据下载|
|[**apiV1TemplatesIdGet**](#apiv1templatesidget) | **GET** /api/v1/templates/{id} | 模板详细数据|
|[**apiV1TemplatesIdMovePatch**](#apiv1templatesidmovepatch) | **PATCH** /api/v1/templates/{id}/move | 改变模板目录|
|[**apiV1TemplatesIdThumbnailGet**](#apiv1templatesidthumbnailget) | **GET** /api/v1/templates/{id}/thumbnail | 模板最新版本的缩略图|
|[**apiV1TemplatesIdTransferPatch**](#apiv1templatesidtransferpatch) | **PATCH** /api/v1/templates/{id}/transfer | 转移模板权限|
|[**apiV1TemplatesIdVersionsGet**](#apiv1templatesidversionsget) | **GET** /api/v1/templates/{id}/versions | 模板版本列表/复杂检索|
|[**apiV1TemplatesThumbnailPost**](#apiv1templatesthumbnailpost) | **POST** /api/v1/templates/thumbnail | 上传缩略图|
|[**apiV1UserinfoCreatorsGet**](#apiv1userinfocreatorsget) | **GET** /api/v1/userinfo/creators | 获取所有模板作者的用户信息|

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

# **apiV1CommercialGlobalContextsCurrentGet**
> CheckoutCalculateContext apiV1CommercialGlobalContextsCurrentGet()



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

**CheckoutCalculateContext**

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
> Array<CheckoutCalculateContext> apiV1CommercialGlobalContextsGet()



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

**Array<CheckoutCalculateContext>**

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

分页查询网易商品信息，支持动态条件过滤。  ## 请求信息 - **URL**: `/api/netease-products` - **方法**: `GET` - **Content-Type**: `application/json`  ## 请求参数  | 参数名 | 类型 | 位置 | 必填 | 说明 | |--------|------|------|------|------| | search | string | query | 否 | 查询条件字符串，格式为 `字段名:值`（等值匹配）或 `字段名~:值`（模糊匹配），多个条件用英文逗号 `,` 分隔，条件之间为 AND 关系。 | | page | integer | query | 否 | 页码，从0开始，默认0 | | size | integer | query | 否 | 每页条数，默认20 | | sort | string | query | 否 | 排序字段，格式 `字段名,方向`，如 `id,desc`，默认按 `id,asc` |  > **注意**：`sort` 字段必须是实体类中存在的属性名，支持多个排序条件用逗号分隔（如 `createTimeMs,desc,id,asc`）。  ## search 参数详细说明  `search` 参数允许通过简单的字符串组合来构建动态查询条件。其语法规则如下：  - **基本格式**：`字段名:值` 或 `字段名~:值` - **操作符**：   - `:` ：等值查询，字段必须完全等于指定值。   - `~:` ：模糊查询，字段值包含指定子串（对应 SQL 的 `LIKE \'%值%\'`），仅对字符串类型字段有效。 - **多条件组合**：多个条件用英文逗号 `,` 分隔，逻辑关系为 **AND**。 - **字段名**：必须是实体类 `NeteaseProduct` 中定义的属性名（Java字段名），**区分大小写**。 - **值类型**：根据字段类型自动转换，支持的字段类型包括：   - 字符串（`String`）：直接使用   - 数值（`Long`/`Integer`）：转换为对应数字   - 布尔（`Boolean`）：支持 `true`/`false`   - 枚举（如 `NeteaseProductStatus`）：需使用枚举常量名称（如 `CREATED`）   - 日期时间：以毫秒时间戳表示的 `Long` 型字段（如 `updateTimeMs`）可直接传入数字  ### 支持的字段列表 以下列出常用可查询字段（完整字段列表请参考实体类 `NeteaseProduct`）：  | 字段名 | 类型 | 说明 | 支持操作符 | |--------|------|------|------------| | `id` | Long | 主键ID | `:` | | `itemId` | String | 商品ID（字符串） | `:`, `~:` | | `itemIdInt` | Long | 商品ID（整数） | `:` | | `itemName` | String | 商品名称 | `:`, `~:` | | `internalStatus` | 枚举 | 内部状态（CREATED, ONLINE, OFFLINE等） | `:` | | `status` | String | 外部状态 | `:`, `~:` | | `price` | Integer | 价格 | `:` | | `priceType` | String | 价格类型 | `:`, `~:` | | `isOriginal` | Boolean | 是否原创 | `:` | | `createTimeMs` | Long | 创建时间（毫秒时间戳） | `:` | | `updateTimeMs` | Long | 更新时间（毫秒时间戳） | `:` | | `onlineTimeMs` | Long | 上线时间（毫秒时间戳） | `:` | | `canManageServer` | Boolean | 能否管理服务器 | `:` | | `weakOffline` | Boolean | 是否弱下线 | `:` |  > **注意**：模糊查询 `~:` 仅对字符串类型字段有效，对其他类型使用会导致类型转换错误。  ## 响应格式  - **成功响应**：HTTP 状态码 `200 OK`，返回分页数据。 - **失败响应**：由全局异常处理器返回错误信息。  ### 成功响应示例 ```json {   \"content\": [     {       \"id\": 1001,       \"internalStatus\": \"CREATED\",       \"updateTimeMs\": 1700000000000,       \"itemId\": \"netease_001\",       \"itemName\": \"测试商品\",       \"price\": 2990,       \"isOriginal\": true,       \"createTimeMs\": 1699900000000,       \"status\": \"active\",       \"templates\": [         {           \"id\": 1,           \"name\": \"模板A\"         }       ]       // ... 其他字段     }   ],   \"pageable\": {     \"pageNumber\": 0,     \"pageSize\": 20,     \"sort\": {       \"sorted\": true,       \"unsorted\": false,       \"empty\": false     }   },   \"totalPages\": 5,   \"totalElements\": 100,   \"last\": false,   \"size\": 20,   \"number\": 0,   \"sort\": {     \"sorted\": true,     \"unsorted\": false,     \"empty\": false   },   \"numberOfElements\": 20,   \"first\": true,   \"empty\": false } ```  ## 请求示例  ### 1. 无条件查询（默认分页） ``` GET /api/netease-products ```  ### 2. 等值条件查询 查询内部状态为 `CREATED` 且 `isOriginal` 为 true 的商品： ``` GET /api/netease-products?search=internalStatus:CREATED,isOriginal:true ```  ### 3. 模糊查询 查询商品名称包含“测试”的商品： ``` GET /api/netease-products?search=itemName~:测试 ```  ### 4. 组合条件 + 分页 + 排序 查询价格等于 2990，且商品ID模糊包含“001”的商品，按创建时间降序排列，每页10条，查看第2页： ``` GET /api/netease-products?search=price:2990,itemId~:001&page=1&size=10&sort=createTimeMs,desc ```  ### 5. 时间范围查询（利用毫秒时间戳） 虽然当前实现不支持直接的范围操作符，但可以通过等值查询指定精确时间戳。如需范围查询，可扩展接口支持 `>`、`<` 操作符，或使用其他方式（如日期范围参数）。  ## 注意事项  - 字段名必须与实体类属性名完全一致，包括大小写。 - 枚举类型的值必须使用枚举常量名称，且区分大小写。 - 模糊查询 `~:` 仅对字符串字段生效，对数值/布尔/枚举字段使用会产生类型转换错误。 - 如果 `search` 参数中包含特殊字符（如逗号、冒号），需要进行 URL 编码。 - 当 `search` 参数格式错误或字段名不存在时，接口将返回 HTTP 400 错误。

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.apiV1CommercialNeteaseProductsGet();
```

### Parameters
This endpoint does not have any parameters.


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

