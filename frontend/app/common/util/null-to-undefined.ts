type NullToUndefined<T> = T extends null
  ? undefined
  : T extends (infer U)[]
  ? NullToUndefined<U>[]
  : T extends object
  ? { [K in keyof T]: NullToUndefined<T[K]> }
  : T;

/**
 * 将对象（包括数组）中所有的 null 值转换为 undefined
 * @param obj 任意对象或数组
 * @returns 转换后的新对象，类型安全
 */
export function deepNullToUndefined<T>(obj: T): NullToUndefined<T> {
  // 处理 null
  if (obj === null) {
    return undefined as NullToUndefined<T>;
  }

  // 处理数组
  if (Array.isArray(obj)) {
    return obj.map(item => deepNullToUndefined(item)) as NullToUndefined<T>;
  }

  // 处理对象（排除函数、Date、RegExp 等特殊对象）
  if (typeof obj === 'object' && obj !== null && obj.constructor === Object) {
    const result: Record<string, any> = {};
    
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        result[key] = deepNullToUndefined((obj as Record<string, any>)[key]);
      }
    }
    
    return result as NullToUndefined<T>;
  }

  // 处理原始值和其他对象（Date、RegExp 等保持不变）
  return obj as NullToUndefined<T>;
}

// 使用示例
const example = {
  name: 'Alice',
  age: null,
  address: {
    street: '123 Main St',
    city: null,
    zip: 12345
  },
  hobbies: ['reading', null, 'coding'],
  nullable: null,
  metadata: {
    created: new Date(),
    tags: [null, 'important', null]
  },
  special: /test/g
};

const result = deepNullToUndefined(example);
console.log(result);

// 类型测试
type TestType = {
  name: string;
  age: undefined; // null 变为了 undefined
  address: {
    street: string;
    city: undefined; // null 变为了 undefined
    zip: number;
  };
  hobbies: (string | undefined)[]; // 数组中的 null 变为了 undefined
  nullable: undefined;
  metadata: {
    created: Date; // Date 对象保持不变
    tags: (undefined | string)[]; // 数组中的 null 变为了 undefined
  };
  special: RegExp; // RegExp 保持不变
};