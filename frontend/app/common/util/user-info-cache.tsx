import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useMemo,
  type ReactNode,
} from "react";

// --- 类型定义 ---

// 本地存储中保存的单项数据结构
interface UserInfoStorageItem {
  username: string;
  uuid: string;
}

// Context 暴露出的 API 接口定义
interface UserInfoContextType {
  setUserInfo: (username: string, uuid: string) => void;
  getUsernameByUuid: (uuid: string) => string | undefined;
  getUuidByUsername: (username: string) => string | undefined;
}

// 内部 State 的结构
interface MapState {
  nameToUuid: Map<string, string>;
  uuidToName: Map<string, string>;
}

// --- 常量 ---
const STORAGE_KEY = "USER_INFO_CACHE";

// --- Context 创建 ---
const UserInfoContext = createContext<UserInfoContextType | null>(null);

// --- Provider 组件 ---
export const UserInfoProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  // 1. 初始化 State：在初始化时直接从 localStorage 读取并重建 Map
  // 使用惰性初始化函数，确保只在挂载时执行一次读取操作
  const [maps, setMaps] = useState<MapState>(() => {
    const initialNameToUuid = new Map<string, string>();
    const initialUuidToName = new Map<string, string>();

    try {
      const storedData = localStorage.getItem(STORAGE_KEY);
      if (storedData) {
        const parsedData: UserInfoStorageItem[] = JSON.parse(storedData);
        if (Array.isArray(parsedData)) {
          parsedData.forEach(({ username, uuid }) => {
            if (username && uuid) {
              initialNameToUuid.set(username, uuid);
              initialUuidToName.set(uuid, username);
            }
          });
        }
      }
    } catch (error) {
      console.error(
        "Failed to parse user info cache from localStorage:",
        error,
      );
    }

    return {
      nameToUuid: initialNameToUuid,
      uuidToName: initialUuidToName,
    };
  });

  // 2. 核心方法：设置用户信息
  const setUserInfo = useCallback((username: string, uuid: string) => {
    setMaps((prev) => {
      // 为了保持 React 的不可变性，创建新的 Map 实例
      const newNameToUuid = new Map(prev.nameToUuid);
      const newUuidToName = new Map(prev.uuidToName);

      // 更新内存中的 Map
      newNameToUuid.set(username, uuid);
      newUuidToName.set(uuid, username);

      // 3. 持久化：准备数据并保存到 localStorage
      // 我们遍历 nameToUuid Map 来生成数组
      const storageData: UserInfoStorageItem[] = Array.from(
        newNameToUuid.entries(),
      ).map(([uName, uId]) => ({
        username: uName,
        uuid: uId,
      }));

      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(storageData));
      } catch (error) {
        console.error("Failed to save user info to localStorage:", error);
      }

      return {
        nameToUuid: newNameToUuid,
        uuidToName: newUuidToName,
      };
    });
  }, []);

  // 4. 获取方法：通过 UUID 获取 Username
  const getUsernameByUuid = useCallback(
    (uuid: string) => {
      return maps.uuidToName.get(uuid);
    },
    [maps.uuidToName],
  );

  // 4. 获取方法：通过 Username 获取 UUID
  const getUuidByUsername = useCallback(
    (username: string) => {
      return maps.nameToUuid.get(username);
    },
    [maps.nameToUuid],
  );

  // 构造 Context Value
  const value = useMemo(
    () => ({
      setUserInfo,
      getUsernameByUuid,
      getUuidByUsername,
    }),
    [setUserInfo, getUsernameByUuid, getUuidByUsername],
  );

  return (
    <UserInfoContext.Provider value={value}>
      {children}
    </UserInfoContext.Provider>
  );
};

// --- Custom Hook ---
export const useUserInfoCache = (): UserInfoContextType => {
  const context = useContext(UserInfoContext);
  if (!context) {
    throw new Error("useUserInfoCache must be used within a UserInfoProvider");
  }
  return context;
};
