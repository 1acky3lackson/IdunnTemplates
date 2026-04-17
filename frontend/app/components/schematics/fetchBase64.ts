import apiClient from "@/lib/axios";

/**
 * 异步获取二进制文件并返回纯净Base64编码
 * @param url 文件的URL地址
 * @param filename 自定义文件名（可选）
 * @returns 包含Base64编码和文件信息的对象
 */
async function fetchFileToBase64(
  url: string,
  filename?: string,
): Promise<{ base64: string; filename: string; size: number; type: string }> {
  try {
    const response = await apiClient.get<ArrayBuffer>(url, {
      responseType: "arraybuffer",
    });
    const arrayBuffer = response.data;

    // 将ArrayBuffer转换为纯净Base64
    const base64 = arrayBufferToBase64(arrayBuffer);

    // 获取文件信息
    const blob = new Blob([arrayBuffer]);

    return {
      base64,
      filename: filename || getFilenameFromUrl(url),
      size: blob.size,
      type: (response.headers["content-type"] as string) || blob.type,
    };
  } catch (error) {
    console.error("获取文件失败:", error);
    throw error;
  }
}

/**
 * 将ArrayBuffer转换为Base64字符串
 * @param buffer ArrayBuffer对象
 * @returns 纯净的Base64字符串
 */
function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  const len = bytes.byteLength;

  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }

  return btoa(binary);
}

/**
 * 从URL中提取文件名
 * @param url 文件URL
 * @returns 文件名
 */
function getFilenameFromUrl(url: string): string {
  // 移除查询参数和哈希
  const cleanUrl = url.split("?")[0].split("#")[0];
  return cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1);
}

/**
 * 将Base64字符串转换为ArrayBuffer
 * @param base64 纯净的Base64字符串
 * @returns ArrayBuffer对象
 */
function base64ToArrayBuffer(base64: string): ArrayBuffer {
  const binaryString = atob(base64);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);

  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }

  return bytes.buffer;
}

/**
 * 验证Base64字符串是否有效
 * @param base64 要验证的Base64字符串
 * @returns 是否有效
 */
function isValidBase64(base64: string): boolean {
  if (typeof base64 !== "string") return false;

  // Base64正则表达式
  const base64Regex = /^[A-Za-z0-9+/]*={0,2}$/;

  // 检查长度是否为4的倍数
  if (base64.length % 4 !== 0) return false;

  return base64Regex.test(base64);
}

// 使用示例
async function exampleUsage(): Promise<void> {
  try {
    const result = await fetchFileToBase64(
      "https://example.com/file.pdf",
      "document.pdf",
    );

    // console.log('纯净Base64编码:', result.base64.substring(0, 100) + '...');
    // console.log('文件名:', result.filename);
    // console.log('文件大小:', result.size, 'bytes');
    // console.log('文件类型:', result.type);
    // console.log('Base64有效:', isValidBase64(result.base64));

    // 如果需要，可以将Base64转换回ArrayBuffer
    const arrayBuffer = base64ToArrayBuffer(result.base64);
    // console.log('转换回的ArrayBuffer大小:', arrayBuffer.byteLength, 'bytes');
  } catch (error) {
    // console.error('处理文件时出错:', error);
  }
}

// 类型定义
interface FileToBase64Result {
  base64: string;
  filename: string;
  size: number;
  type: string;
}

// 导出函数（如果使用模块系统）
export {
  fetchFileToBase64,
  arrayBufferToBase64,
  base64ToArrayBuffer,
  isValidBase64,
  getFilenameFromUrl,
};

export type { FileToBase64Result };
