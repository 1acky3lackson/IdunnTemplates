# IdunnApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**rootGet**](#rootget) | **GET** / | Idunn 插件权限桥接|
|[**rootPost**](#rootpost) | **POST** / | Idunn 插件权限桥接（批量查询）|
|[**userinfoPost**](#userinfopost) | **POST** /userinfo | 批量查询用户信息|

# **rootGet**
> Get200Response rootGet()



### Example

```typescript
import {
    IdunnApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new IdunnApi(configuration);

let uuid: string; //玩家的 UUID (default to undefined)
let permission: string; //查询的 Permission (default to undefined)
let username: string; // (optional) (default to undefined)

const { status, data } = await apiInstance.rootGet(
    uuid,
    permission,
    username
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **uuid** | [**string**] | 玩家的 UUID | defaults to undefined|
| **permission** | [**string**] | 查询的 Permission | defaults to undefined|
| **username** | [**string**] |  | (optional) defaults to undefined|


### Return type

**Get200Response**

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

# **rootPost**
> Get200Response rootPost()



### Example

```typescript
import {
    IdunnApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new IdunnApi(configuration);

let uuid: string; //玩家的 UUID (default to undefined)
let username: string; // (optional) (default to undefined)
let requestBody: Array<string>; // (optional)

const { status, data } = await apiInstance.rootPost(
    uuid,
    username,
    requestBody
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestBody** | **Array<string>**|  | |
| **uuid** | [**string**] | 玩家的 UUID | defaults to undefined|
| **username** | [**string**] |  | (optional) defaults to undefined|


### Return type

**Get200Response**

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

# **userinfoPost**
> object userinfoPost()



### Example

```typescript
import {
    IdunnApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new IdunnApi(configuration);

let requestBody: Array<string>; // (optional)

const { status, data } = await apiInstance.userinfoPost(
    requestBody
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestBody** | **Array<string>**|  | |


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

