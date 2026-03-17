/**
 * 验证 Partial<T> 对象，确保指定的必需字段都存在
 * @param obj 部分对象
 * @param requiredFields 必需字段列表（如果不提供，则检查所有字段）
 * @param options 配置选项
 * @returns 完整对象 T
 * @throws 当缺少必需字段时抛出错误
 */
export function requireFields<T extends object>(
  obj: Partial<T>,
  requiredFields?: (keyof T)[],
  options?: {
    errorMessage?: string;
    allowUndefined?: boolean; // 是否允许 undefined 值
    throwOnExtra?: boolean; // 是否在发现额外字段时抛出异常
  },
): T {
  const {
    errorMessage,
    allowUndefined = false,
    throwOnExtra = false,
  } = options || {};

  // 确定需要检查的字段
  const fieldsToCheck = requiredFields || (Object.keys(obj) as (keyof T)[]);

  // 检查缺失字段
  const missingFields: string[] = [];

  for (const field of fieldsToCheck) {
    const value = obj[field];

    // 检查字段是否存在
    if (!Object.prototype.hasOwnProperty.call(obj, field)) {
      missingFields.push(String(field));
    }
    // 如果不允许 undefined，检查值是否为 undefined
    else if (!allowUndefined && value === undefined) {
      missingFields.push(String(field));
    }
  }

  // 如果指定了 throwOnExtra，检查是否有额外字段
  if (throwOnExtra) {
    const extraFields = Object.keys(obj).filter(
      (key) => !fieldsToCheck.includes(key as keyof T),
    );

    if (extraFields.length > 0) {
      throw new Error(
        `Unexpected extra fields: ${extraFields.map((f) => `'${f}'`).join(", ")}`,
      );
    }
  }

  // 如果有缺失字段，抛出异常
  if (missingFields.length > 0) {
    const fieldList = missingFields.map((f) => `'${f}'`).join(", ");
    throw new Error(errorMessage || `Missing required fields: ${fieldList}`);
  }

  return obj as T;
}

/**
 * 验证 Partial<T> 对象，确保所有必需字段都存在
 * @param obj 部分对象
 * @param errorMessage 自定义错误消息（可选）
 * @returns Required<T> 确保所有字段都变为必需
 * @throws 当缺少必需字段时抛出错误
 */
export function requireAllFields<T extends object>(
  obj: Partial<T>,
  errorMessage?: string,
): Required<T> {
  const missingFields: string[] = [];

  // 检查每个字段是否存在且不为 undefined
  for (const key in obj) {
    if (
      !Object.prototype.hasOwnProperty.call(obj, key) ||
      obj[key] === undefined
    ) {
      missingFields.push(key);
    }
  }

  if (missingFields.length > 0) {
    const fieldList = missingFields.map((f) => `'${f}'`).join(", ");
    throw new Error(
      errorMessage ||
        `Missing required fields: ${fieldList}. Expected all fields to be present and not undefined.`,
    );
  }

  return obj as Required<T>;
}

/**
 * 更严格的版本：确保所有必需字段都存在且不为 undefined
 */
export function requireAllFieldsStrict<T extends object>(
  obj: Partial<T>,
  errorMessage?: string,
): T {
  return requireFields(obj, undefined, {
    errorMessage,
    allowUndefined: false,
    throwOnExtra: false,
  });
}
