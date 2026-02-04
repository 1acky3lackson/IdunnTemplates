import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
    Loader2,
    Lock,
    User,
    Code,
    Layout,
    Hexagon,
    Database,
    Server,
    Cloud,
} from "lucide-react";
import { useIntlayer } from "react-intlayer";

import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { useAuth } from "~/common/auth/auth-provider";

import lightLogo from "@/common/topbar/logo-light.svg";
import darkLogo from "@/common/topbar/logo-dark.svg";
import { FallingIconsBackground } from "~/common/util/falling-icons-background";
import MeshGradientBackground from "~/common/util/mesh-gradient-background";
import { useTheme } from "next-themes";

const Logo = () => (
    <div className="flex items-center gap-1 font-bold text-xl">
        {/* Assuming the svgs are importable as strings or components. 
            If they are URLs, we use img tags. 
            Tailwind dark mode strategy usually requires 'dark:' class.
            Here we explicitly check theme if needed or use CSS hiding. */}
        <img
            src={lightLogo}
            alt="Logo"
            className="h-12 w-auto dark:hidden"
        />
        <img
            src={darkLogo}
            alt="Logo"
            className="h-12 w-auto hidden dark:block"
        />
    </div>
);

// 定义你想在背景中飘落的图标集合
const backgroundIcons = [Code, Layout, Hexagon, Database, Server, Cloud];

export default function LoginPage() {
    const { login } = useAuth();
    const [isLoading, setIsLoading] = useState(false);
    const { theme } = useTheme();

    // 获取国际化文本
    const {
        title,
        description,
        skinServerName,
        description2,
        usernameLabel,
        passwordLabel,
        usernamePlaceholder,
        passwordPlaceholder,
        usernameErrorMsg,
        passwordErrorMsg,
        loginBtn,
        loginBtnLoading,
        footerText,
    } = useIntlayer("login-page");

    // 1. 定义表单验证 Schema (移入组件内部以支持动态国际化)
    const formSchema = z.object({
        username: z.string().min(2, {
            message: usernameErrorMsg.value, // 使用 .value 获取字符串
        }),
        password: z.string().min(4, {
            message: passwordErrorMsg.value,
        }),
    });

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            username: "",
            password: "",
        },
    });

    async function onSubmit(values: z.infer<typeof formSchema>) {
        setIsLoading(true);
        try {
            await login(values);
        } catch (error) {
            // 错误已在 AuthProvider 中通过 Toast 处理
        } finally {
            setIsLoading(false);
        }
    }

    return (
        // <div className="">
            <MeshGradientBackground
                id="idunn-login-page-bg"
                count={10}
                speedRange={{ min: 0.000007, max: 0.00012 }}
                radiusRange={{ min: 50, max: 70 }}
                lightnessRange={{ min: 30, max: 50 }}
                saturationRange={{ min: 20, max: 60 }}
                opacityRange={{ min: 0.3, max: 0.7 }}
                theme={theme as 'light' | 'dark' | 'system'}
            >
                <FallingIconsBackground icons={backgroundIcons} iconCount={20}>
                    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden ">

                        {/* 装饰背景：淡紫色光晕 */}
                        <div className="absolute -top-[20%] -left-[10%] h-125 w-125 rounded-full bg-primary/10 blur-[100px]" />
                        <div className="absolute top-[40%] -right-[10%] h-100 w-100 rounded-full bg-secondary/20 blur-[100px]" />

                        <Card className="z-1000 w-full max-w-md border-muted/40 shadow-xl backdrop-blur-sm sm:w-100">
                            <CardHeader className="space-y-1 text-center">
                                <div className="flex justify-center mb-4">
                                    {/* Logo */}
                                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                                        <Logo />
                                    </div>
                                </div>
                                <CardTitle className="text-2xl font-bold tracking-tight">
                                    {title}
                                </CardTitle>
                                <CardDescription>
                                    <span>{description}</span>
                                    <a href="https://skin.taixue.cc/" target="_blank" rel="noreferrer" className="font-extrabold underline text-primary mx-2">
                                        {skinServerName}
                                    </a>
                                    <span>{description2}</span>
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <Form {...form}>
                                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                                        <FormField
                                            control={form.control}
                                            name="username"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>{usernameLabel}</FormLabel>
                                                    <FormControl>
                                                        <div className="relative">
                                                            <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                                            <Input
                                                                placeholder={usernamePlaceholder.value}
                                                                className="pl-9 bg-background/50"
                                                                {...field}
                                                            />
                                                        </div>
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                        <FormField
                                            control={form.control}
                                            name="password"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>{passwordLabel}</FormLabel>
                                                    <FormControl>
                                                        <div className="relative">
                                                            <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                                            <Input
                                                                type="password"
                                                                placeholder={passwordPlaceholder.value}
                                                                className="pl-9 bg-background/50"
                                                                {...field}
                                                            />
                                                        </div>
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                        <Button
                                            type="submit"
                                            className="w-full font-bold transition-all hover:scale-[1.02]"
                                            disabled={isLoading}
                                        >
                                            {isLoading ? (
                                                <>
                                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                    {loginBtnLoading}
                                                </>
                                            ) : (
                                                loginBtn
                                            )}
                                        </Button>
                                    </form>
                                </Form>
                            </CardContent>
                            <CardFooter className="flex justify-center">
                                <p className="text-xs text-muted-foreground">
                                    {footerText}
                                </p>
                            </CardFooter>
                        </Card>

                    </div>
                </FallingIconsBackground>
            </MeshGradientBackground>
        // </div>
    );
}