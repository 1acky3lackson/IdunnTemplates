# SchemApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiRenderPost**](#apirenderpost) | **POST** /api/render | 渲染 Schem|

# **apiRenderPost**
> object apiRenderPost()



### Example

```typescript
import {
    SchemApi,
    Configuration,
    ApiRenderPostRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new SchemApi(configuration);

let apiRenderPostRequest: ApiRenderPostRequest; // (optional)

const { status, data } = await apiInstance.apiRenderPost(
    apiRenderPostRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **apiRenderPostRequest** | **ApiRenderPostRequest**|  | |


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

