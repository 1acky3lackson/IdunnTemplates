# PageNeteaseProductDto


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**content** | [**Array&lt;NeteaseProductDto&gt;**](NeteaseProductDto.md) |  | [default to undefined]
**totalElements** | **number** | 总记录数 | [default to undefined]
**totalPages** | **number** | 总页数 | [optional] [default to undefined]
**last** | **boolean** | 是否为最后一页 | [default to undefined]
**number** | **number** | 当前页码（从0开始） | [default to undefined]
**size** | **number** | 每页大小 | [optional] [default to undefined]
**numberOfElements** | **number** | 当前页实际元素个数 | [optional] [default to undefined]
**first** | **boolean** | 是否为第一页 | [optional] [default to undefined]
**empty** | **boolean** | 是否为空页 | [optional] [default to undefined]

## Example

```typescript
import { PageNeteaseProductDto } from './api';

const instance: PageNeteaseProductDto = {
    content,
    totalElements,
    totalPages,
    last,
    number,
    size,
    numberOfElements,
    first,
    empty,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
