import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
} from "react";
import { useNavigate, useLocation } from "react-router";
import { toast } from "sonner";
import { useIntlayer } from "react-intlayer";
import { IDUNN_API } from "~/api";

interface AuthUser {
  username: string;
  uuid: string;
}

interface AuthContextType {
  user: AuthUser | null;
  login: (data: any) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// 定义 Token 过期前的缓冲时间（1分钟 = 60秒）
const EXPIRATION_BUFFER_SECONDS = 60;

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<any | null>(null);
  const navigate = useNavigate();
  const location = useLocation();

  // 获取国际化文本
  const {
    loginSuccessTitle,
    loginSuccessDesc,
    loginErrorTitle,
    loginErrorDefault,
    tokenMissing,
    sessionExpiredTitle,
    sessionExpiredDesc,
    reLoginAction,
  } = useIntlayer("auth-provider");

  // 初始化时检查本地存储
  useEffect(() => {
    const token = localStorage.getItem("auth_token");
    const userStr = localStorage.getItem("auth_user");
    if (token && userStr) {
      try {
        setUser(JSON.parse(userStr));
      } catch (e) {
        console.error("Failed to parse user data", e);
        localStorage.clear();
      }
    }
  }, []);

  // 将 logout 包裹在 useCallback 中，以便在 useEffect 依赖中使用
  const logout = useCallback(() => {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_exp");
    localStorage.removeItem("auth_user");
    IDUNN_API.apiAuthLogoutPost();
    setUser(null);
    // navigate("/login");

    // full reload page to page /login
    window.location.reload();
  }, [navigate]);

  // 核心：自动过期检查器
  useEffect(() => {
    // 定义检查函数
    const checkTokenExpiration = () => {
      const expiresTimeStr = localStorage.getItem("auth_exp");
      if (!expiresTimeStr) return;

      const expiresTime = parseInt(expiresTimeStr, 10);
      const currentTime = Math.floor(Date.now()); // 当前时间（ms）

      // 剩余时间 (假设 expiresTime 是毫秒时间戳，如果是秒请在下面 * 1000)
      // 注意：请确保后端返回的 expiresTime 单位与 Date.now() 统一
      const timeLeft = expiresTime - currentTime;

      // 如果剩余时间小于缓冲时间（1分钟），强制登出
      if (timeLeft <= EXPIRATION_BUFFER_SECONDS * 1000) {
        // 只有当不在登录页时才触发
        if (location.pathname !== "/login") {
          logout();

          // 使用新的 Toast API
          toast(sessionExpiredTitle, {
            description: sessionExpiredDesc,
            action: {
              label: reLoginAction,
              onClick: () => navigate("/login"),
            },
            // 给这个警告加一个特殊的样式标识（可选）
            className: "bg-destructive/10 border-destructive/20",
          });
        }
      }
    };

    // 立即检查一次，防止刷新页面时已经是过期状态
    checkTokenExpiration();

    // 每 10 秒检查一次
    const intervalId = setInterval(checkTokenExpiration, 10000);

    // 组件卸载时清除定时器
    return () => clearInterval(intervalId);
  }, [
    navigate,
    location.pathname,
    logout,
    sessionExpiredTitle,
    sessionExpiredDesc,
    reLoginAction,
  ]);

  const login = async (values: any) => {
    try {
      // 调用 apifox 生成的接口
      const res = await IDUNN_API.apiAuthLoginPost({
        username: values.username,
        password: values.password,
      });

      console.log("Login response data:", res);
      const { JWT, expiresTime, ...userInfo } = res.data;

      if (!JWT) throw new Error(tokenMissing.value);

      // 存储数据
      localStorage.setItem("auth_token", JWT);
      // 存储过期时间 (确保是字符串形式的毫秒时间戳)
      localStorage.setItem("auth_exp", String(expiresTime + Date.now()));
      localStorage.setItem("auth_user", JSON.stringify(userInfo));

      setUser(userInfo);

      toast(loginSuccessTitle, {
        description: `${loginSuccessDesc.value}，${userInfo.username}`,
      });

      navigate("/"); // 跳转到首页
    } catch (error: any) {
      console.error(error);
      const errorMsg = error.response?.data?.message || loginErrorDefault.value;

      toast(loginErrorTitle, {
        description: errorMsg,
        action: {
          label: "Retry",
          onClick: () => login(values),
        },
      });
      throw error;
    }
  };

  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth: () => AuthContextType = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
