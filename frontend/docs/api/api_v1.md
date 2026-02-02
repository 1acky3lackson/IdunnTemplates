---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 默认模块

Base URLs:

# Authentication

# 后端/登录&授权

## POST Yggdrasil 登录

POST /api/auth/login

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» username|body|string| 是 |none|
|» password|body|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "username": "string",
  "uuid": "string",
  "JWT": "string",
  "expiresTime": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» username|string|true|none||none|
|» uuid|string|true|none||none|
|» JWT|string|true|none||none|
|» expiresTime|integer|true|none||none|

## GET 查看个人信息 / 验证登录

GET /api/auth/me

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|auth_token|cookie|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "username": "string",
  "uuid": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» username|string|true|none||none|
|» uuid|string|true|none||none|

# 后端/模板

## GET 模板列表（或根据Path）

GET /api/v1/templates

# 模板搜索接口文档

## 1. 搜索模板列表

支持复合条件筛选（模糊匹配、精确匹配、范围查询）、分页以及多字段排序。

- **URL**: `/api/v1/templates`
- **Method**: `GET`
- **Content-Type**: `application/json`

### 请求参数 (Query Parameters)

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `pathPrefix` | string | 否 | - | **路径前缀匹配**<br>搜索以该路径开头的模板 (SQL: `LIKE 'val%'`) |
| `creatorId` | UUID | 否 | - | **创建者ID** (精确匹配) |
| `worldId` | UUID | 否 | - | **所属世界ID** (精确匹配) |
| `locked` | boolean | 否 | - | **锁定状态**<br>`true`: 仅看锁定; `false`: 仅看未锁定 |
| `minWidth` | integer | 否 | - | **最小宽度** (包含) |
| `maxWidth` | integer | 否 | - | **最大宽度** (包含) |
| `minHeight` | integer | 否 | - | **最小高度** (包含) |
| `maxHeight` | integer | 否 | - | **最大高度** (包含) |
| `minLength` | integer | 否 | - | **最小长度** (包含) |
| `maxLength` | integer | 否 | - | **最大长度** (包含) |
| `page` | integer | 否 | `0` | **页码** (从 0 开始) |
| `size` | integer | 否 | `20` | **每页条数** |
| `sort` | string[] | 否 | `path,asc` | **排序规则**<br>格式: `字段名,方向`<br>方向: `asc`(升序), `desc`(降序)<br>支持传多个 sort 参数 |

---

### 排序字段说明 (Sort Fields)

排序参数格式为 `sort=字段名,方向`。
**注意**：元数据相关的字段必须加上 `metadata.` 前缀。

#### A. 基础属性
| 字段名 | 说明 | 示例 |
| :--- | :--- | :--- |
| `path` | 按文件路径/名称排序 (默认) | `sort=path,asc` |
| `name` | 按模板显示名称排序 | `sort=name,desc` |

#### B. 时间与状态 (Metadata)
| 字段名 | 说明 | 示例 |
| :--- | :--- | :--- |
| `metadata.creationTime` | 按创建时间排序 (时间戳) | `sort=metadata.creationTime,desc` (最新在前) |
| `metadata.locked` | 按锁定状态排序 | `sort=metadata.locked,desc` (锁定的在前) |
| `metadata.lockedTimestamp` | 按锁定时间排序 | - |
| `metadata.deletedTimestamp` | 按删除时间排序 | - |

#### C. 尺寸与空间 (Metadata)
| 字段名 | 说明 | 示例 |
| :--- | :--- | :--- |
| `metadata.width` | 按宽度 (X轴跨度) 排序 | `sort=metadata.width,desc` (最宽的在前) |
| `metadata.height` | 按高度 (Y轴跨度) 排序 | `sort=metadata.height,desc` (最高的在前) |
| `metadata.length` | 按长度 (Z轴跨度) 排序 | `sort=metadata.length,desc` (最长的在前) |
| `metadata.anchorX` | 按锚点 X 坐标排序 | - |
| `metadata.anchorY` | 按锚点 Y 坐标排序 | - |
| `metadata.anchorZ` | 按锚点 Z 坐标排序 | - |

---

### 请求示例

**示例场景**：
查找 `users/` 目录下，宽度在 10 到 200 之间，且未锁定的模板。
结果按“创建时间倒序”排列（最新的在最前），每页 20 条。

```http
GET /api/v1/templates?pathPrefix=users/&locked=false&minWidth=10&maxWidth=200&page=0&size=20&sort=metadata.creationTime,desc

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|pathPrefix|query|string| 否 |none|
|locked|query|boolean| 否 |是否锁定|
|minWidth|query|integer| 否 |最小尺寸|
|maxWidth|query|integer| 否 |最大尺寸|
|worldId|query|string| 否 |世界 uuid|
|page|query|integer| 否 |none|
|size|query|integer| 否 |none|
|sort|query|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "content": [
    {
      "id": "string",
      "path": "string",
      "name": "string",
      "metadata": {
        "templateId": "string",
        "creatorId": "string",
        "creationTime": 0,
        "worldId": "string",
        "anchorX": 0,
        "anchorY": 0,
        "anchorZ": 0,
        "width": 0,
        "height": 0,
        "length": 0,
        "locked": true,
        "lockedTimestamp": 0,
        "versions": [
          "string"
        ],
        "deleted": true,
        "stagedChanges": {
          "addedInstances": [
            null
          ],
          "removedInstanceIds": [
            null
          ],
          "empty": true
        },
        "parentTemplateInstances": {},
        "childTemplateInstances": {}
      },
      "locked": true,
      "latestVersion": "string",
      "usePermissionNode": "string",
      "colorSchemes": [
        "string"
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 0,
    "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": true
  },
  "totalPages": 0,
  "totalElements": 0,
  "last": true,
  "size": 0,
  "number": 0,
  "sort": {
    "empty": true,
    "unsorted": true,
    "sorted": true
  },
  "numberOfElements": 0,
  "first": true,
  "empty": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[TemplatePagenable](#schematemplatepagenable)|

## GET 模板详细数据

GET /api/v1/templates/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |模板的 UUID|

> 返回示例

> 200 Response

```json
{
  "id": "string",
  "path": "string",
  "name": "string",
  "metadata": {
    "templateId": "string",
    "creatorId": "string",
    "creationTime": 0,
    "worldId": "string",
    "anchorX": 0,
    "anchorY": 0,
    "anchorZ": 0,
    "width": 0,
    "height": 0,
    "length": 0,
    "locked": true,
    "lockedTimestamp": 0,
    "versions": [
      "string"
    ],
    "deleted": true,
    "stagedChanges": {
      "addedInstances": [
        "string"
      ],
      "removedInstanceIds": [
        "string"
      ],
      "empty": true
    },
    "parentTemplateInstances": {},
    "childTemplateInstances": {}
  },
  "locked": true,
  "latestVersion": "string",
  "usePermissionNode": "string",
  "colorSchemes": [
    "string"
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[Template](#schematemplate)|

## GET 模板版本 schem 数据下载

GET /api/v1/templates/{id}/download

*  /api/v1/templates/{id}/download (下载最新版)
* /api/v1/templates/{id}/download?version=1706781234000 (下载指定版)

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |模板的 UUID|
|version|query|string| 否 |模板的版本号，不填则下载最新版|

> 返回示例

> 200 Response

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 模板版本列表/复杂检索

GET /api/v1/templates/{id}/versions

# 模板版本查询接口

获取特定模板的历史版本列表。支持针对提交信息、提交者、时间的复杂筛选，以及分页排序功能。

- **URL**: `/api/v1/templates/{id}/versions`
- **Method**: `GET`
- **Auth**: Required (Bearer Token)

## 路径参数 (Path Parameters)

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `id` | UUID | **是** | **模板的唯一标识符**<br>例如: `500d59d1-84a8-3207-932c-e62f8a543ef7` |

## 查询参数 (Query Parameters)

用于筛选和分页的参数，均以 Query String 形式传递 (例如 `?page=0&messageKeyword=fix`)。

### 1. 筛选条件 (Filtering)

| 参数名 | 类型 | 说明 | 示例 |
| :--- | :--- | :--- | :--- |
| `messageKeyword` | String | **提交信息模糊搜索**<br>搜索包含该关键词的版本留言 (Case-insensitive)。 | `fix lighting` |
| `submitterId` | UUID | **提交者 ID**<br>仅显示指定用户提交的版本。 | `0caee9cd-6987...` |
| `versionId` | String | **版本业务 ID**<br>精确匹配特定的版本号 (通常是时间戳字符串)。 | `1706781234000` |
| `minCreatedAt` | Long | **最小创建时间 (时间戳)**<br>筛选在此时间**之后** (>=) 提交的版本。 | `1706000000000` |
| `maxCreatedAt` | Long | **最大创建时间 (时间戳)**<br>筛选在此时间**之前** (<=) 提交的版本。 | `1706999999999` |

### 2. 分页与排序 (Pagination & Sorting)

| 参数名 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `page` | Integer | `0` | **页码** (从 0 开始)。 |
| `size` | Integer | `20` | **每页条数**。 |
| `sort` | String | `createdAt,desc` | **排序规则**<br>格式: `字段名,方向`<br>支持字段: `createdAt`, `versionId`, `submitterId` |

---

## 请求示例 (Request Examples)

### 场景 A: 查看某模板最近的提交记录 (默认)
```http
GET /api/v1/templates/500d59d1-84a8-3207-932c-e62f8a543ef7/versions
```

### 场景 B: 搜索包含 "bugfix" 的提交，且由特定用户提交
```http
GET /api/v1/templates/500d59d1.../versions?messageKeyword=bugfix&submitterId=0caee9cd...
```

### 场景 C: 查找 2024年1月1日 之后的版本，按时间正序排列
```http
GET /api/v1/templates/500d59d1.../versions?minCreatedAt=1704067200000&sort=createdAt,asc
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |模板的 UUID|
|messageKeyword|query|string| 否 |提交信息模糊搜索：搜索包含该关键词的版本留言 (Case-insensitive)|
|submitterId|query|string| 否 |提交者 ID：仅显示指定用户提交的版本。|
|versionId|query|string| 否 |版本业务 ID：精确匹配特定的版本号 (通常是时间戳字符串)|
|minCreatedAt|query|string| 否 |最小创建时间 (时间戳，ms)：筛选在此时间**之后** (>=) 提交的版本|
|maxCreatedAt|query|string| 否 |最大创建时间 (时间戳，ms)：筛选在此时间**之前** (<=) 提交的版本|
|page|query|string| 否 |页码：(从 0 开始)|
|size|query|string| 否 |每页条数|
|sort|query|string| 否 |排序规则，参见文档|

> 返回示例

> 200 Response

```json
{
  "content": [
    {
      "id": 0,
      "versionId": "string",
      "submitterId": "string",
      "message": "string",
      "createdAt": 0
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 0,
    "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": true
  },
  "totalPages": 0,
  "totalElements": 0,
  "last": true,
  "size": 0,
  "number": 0,
  "sort": {
    "empty": true,
    "unsorted": true,
    "sorted": true
  },
  "numberOfElements": 0,
  "first": true,
  "empty": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[TemplateVersionPagenable](#schematemplateversionpagenable)|

## GET 模板最新版本的缩略图

GET /api/v1/templates/{id}/thumbnail

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |模板的 UUID|
|refresh|query|boolean| 否 |是否强制刷新|
|angle|query|integer| 是 |0, 1, 2, 3|

> 返回示例

> 200 Response

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 数据模型

<h2 id="tocS_Template">Template</h2>

<a id="schematemplate"></a>
<a id="schema_Template"></a>
<a id="tocStemplate"></a>
<a id="tocstemplate"></a>

```json
{
  "id": "string",
  "path": "string",
  "name": "string",
  "metadata": {
    "templateId": "string",
    "creatorId": "string",
    "creationTime": 0,
    "worldId": "string",
    "anchorX": 0,
    "anchorY": 0,
    "anchorZ": 0,
    "width": 0,
    "height": 0,
    "length": 0,
    "locked": true,
    "lockedTimestamp": 0,
    "versions": [
      "string"
    ],
    "deleted": true,
    "stagedChanges": {
      "addedInstances": [
        "string"
      ],
      "removedInstanceIds": [
        "string"
      ],
      "empty": true
    },
    "parentTemplateInstances": {},
    "childTemplateInstances": {}
  },
  "locked": true,
  "latestVersion": "string",
  "usePermissionNode": "string",
  "colorSchemes": [
    "string"
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|true|none|模板uuid|none|
|path|string|true|none|模板路径|none|
|name|string|true|none|模板名称|none|
|metadata|object|true|none|元数据|none|
|» templateId|string|true|none||none|
|» creatorId|string|true|none|创建玩家id|none|
|» creationTime|integer|true|none|创建时间|none|
|» worldId|string|true|none|世界id|none|
|» anchorX|integer|true|none|锚点坐标|none|
|» anchorY|integer|true|none|锚点坐标|none|
|» anchorZ|integer|true|none|锚点坐标|none|
|» width|integer|true|none|宽|none|
|» height|integer|true|none|高|none|
|» length|integer|true|none|长|none|
|» locked|boolean|true|none|是否被锁定|none|
|» lockedTimestamp|integer¦null|true|none|锁定时间戳ms|none|
|» versions|[string]|true|none|版本列表|none|
|» deleted|boolean|true|none|软删除|none|
|» stagedChanges|object|true|none||none|
|»» addedInstances|[string]|true|none||none|
|»» removedInstanceIds|[string]|true|none||none|
|»» empty|boolean|true|none||none|
|» parentTemplateInstances|object|true|none||none|
|» childTemplateInstances|object|true|none||none|
|locked|boolean|true|none|是否被锁定|none|
|latestVersion|any|true|none|最新版本|none|

oneOf

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» *anonymous*|string|false|none||none|

xor

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» *anonymous*|integer|false|none||none|

xor

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» *anonymous*|boolean|false|none||none|

xor

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» *anonymous*|array|false|none||none|

xor

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» *anonymous*|object|false|none||none|

xor

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» *anonymous*|number|false|none||none|

continued

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|usePermissionNode|string|true|none|权限节点|none|
|colorSchemes|[string]|true|none|配色方案|none|

<h2 id="tocS_TemplatePagenable">TemplatePagenable</h2>

<a id="schematemplatepagenable"></a>
<a id="schema_TemplatePagenable"></a>
<a id="tocStemplatepagenable"></a>
<a id="tocstemplatepagenable"></a>

```json
{
  "content": [
    {
      "id": "string",
      "path": "string",
      "name": "string",
      "metadata": {
        "templateId": "string",
        "creatorId": "string",
        "creationTime": 0,
        "worldId": "string",
        "anchorX": 0,
        "anchorY": 0,
        "anchorZ": 0,
        "width": 0,
        "height": 0,
        "length": 0,
        "locked": true,
        "lockedTimestamp": 0,
        "versions": [
          "string"
        ],
        "deleted": true,
        "stagedChanges": {
          "addedInstances": [
            null
          ],
          "removedInstanceIds": [
            null
          ],
          "empty": true
        },
        "parentTemplateInstances": {},
        "childTemplateInstances": {}
      },
      "locked": true,
      "latestVersion": "string",
      "usePermissionNode": "string",
      "colorSchemes": [
        "string"
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 0,
    "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": true
  },
  "totalPages": 0,
  "totalElements": 0,
  "last": true,
  "size": 0,
  "number": 0,
  "sort": {
    "empty": true,
    "unsorted": true,
    "sorted": true
  },
  "numberOfElements": 0,
  "first": true,
  "empty": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|content|[[Template](#schematemplate)]|true|none|模板列表|none|
|pageable|object|true|none||none|
|» pageNumber|integer|true|none||none|
|» pageSize|integer|true|none||none|
|» sort|object|true|none||none|
|»» empty|boolean|true|none||none|
|»» unsorted|boolean|true|none||none|
|»» sorted|boolean|true|none||none|
|» offset|integer|true|none||none|
|» paged|boolean|true|none||none|
|» unpaged|boolean|true|none||none|
|totalPages|integer|true|none||none|
|totalElements|integer|true|none||none|
|last|boolean|true|none||none|
|size|integer|true|none||none|
|number|integer|true|none||none|
|sort|object|true|none||none|
|» empty|boolean|true|none||none|
|» unsorted|boolean|true|none||none|
|» sorted|boolean|true|none||none|
|numberOfElements|integer|true|none||none|
|first|boolean|true|none||none|
|empty|boolean|true|none||none|

<h2 id="tocS_TemplateVersion">TemplateVersion</h2>

<a id="schematemplateversion"></a>
<a id="schema_TemplateVersion"></a>
<a id="tocStemplateversion"></a>
<a id="tocstemplateversion"></a>

```json
{
  "id": 0,
  "versionId": "string",
  "submitterId": "string",
  "message": "string",
  "createdAt": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer|false|none||none|
|versionId|string|false|none||none|
|submitterId|string|false|none||none|
|message|string|false|none||none|
|createdAt|integer|false|none||none|

<h2 id="tocS_TemplateVersionPagenable">TemplateVersionPagenable</h2>

<a id="schematemplateversionpagenable"></a>
<a id="schema_TemplateVersionPagenable"></a>
<a id="tocStemplateversionpagenable"></a>
<a id="tocstemplateversionpagenable"></a>

```json
{
  "content": [
    {
      "id": 0,
      "versionId": "string",
      "submitterId": "string",
      "message": "string",
      "createdAt": 0
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 0,
    "sort": {
      "empty": true,
      "unsorted": true,
      "sorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": true
  },
  "totalPages": 0,
  "totalElements": 0,
  "last": true,
  "size": 0,
  "number": 0,
  "sort": {
    "empty": true,
    "unsorted": true,
    "sorted": true
  },
  "numberOfElements": 0,
  "first": true,
  "empty": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|content|[[TemplateVersion](#schematemplateversion)]|true|none|模板列表|none|
|pageable|object|true|none||none|
|» pageNumber|integer|true|none||none|
|» pageSize|integer|true|none||none|
|» sort|object|true|none||none|
|»» empty|boolean|true|none||none|
|»» unsorted|boolean|true|none||none|
|»» sorted|boolean|true|none||none|
|» offset|integer|true|none||none|
|» paged|boolean|true|none||none|
|» unpaged|boolean|true|none||none|
|totalPages|integer|true|none||none|
|totalElements|integer|true|none||none|
|last|boolean|true|none||none|
|size|integer|true|none||none|
|number|integer|true|none||none|
|sort|object|true|none||none|
|» empty|boolean|true|none||none|
|» unsorted|boolean|true|none||none|
|» sorted|boolean|true|none||none|
|numberOfElements|integer|true|none||none|
|first|boolean|true|none||none|
|empty|boolean|true|none||none|

