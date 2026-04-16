import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Loader2, Lock, Code, Layout, Hexagon, Database, Server, Cloud } from "lucide-react";
import { useIntlayer } from "react-intlayer";
import { useSearchParams, useNavigate, useParams } from "react-router";

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
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { toast } from "sonner";
import { getBackendBaseUrl } from "~/api";

import lightLogo from "@/common/topbar/logo-light.svg";
import darkLogo from "@/common/topbar/logo-dark.svg";
import { FallingIconsBackground } from "~/common/util/falling-icons-background";
import MeshGradientBackground from "~/common/util/mesh-gradient-background";
import { useTheme } from "next-themes";

const Logo = () => (
  <div className="flex items-center gap-1 font-bold text-xl">
    <img src={lightLogo} alt="Logo" className="h-12 w-auto dark:hidden" />
    <img src={darkLogo} alt="Logo" className="h-12 w-auto hidden dark:block" />
  </div>
);

const backgroundIcons = [Code, Layout, Hexagon, Database, Server, Cloud];

export default function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false);
  const { theme } = useTheme();
  const [searchParams] = useSearchParams();
  const { encodedToken } = useParams();
  const navigate = useNavigate();

  const {
    title,
    description,
    passwordLabel,
    confirmPasswordLabel,
    passwordPlaceholder,
    confirmPasswordPlaceholder,
    passwordErrorMsg,
    confirmPasswordErrorMsg,
    registerBtn,
    registerBtnLoading,
    registerSuccessTitle,
    registerSuccessDesc,
    registerErrorTitle,
    invalidTokenMsg,
  } = useIntlayer("register-page");

  document.title = title.value;

  const formSchema = z.object({
    password: z.string().min(4, {
      message: passwordErrorMsg.value,
    }),
    confirmPassword: z.string().min(4, {
      message: confirmPasswordErrorMsg.value,
    }),
  }).refine((data) => data.password === data.confirmPassword, {
    message: confirmPasswordErrorMsg.value,
    path: ["confirmPassword"],
  });

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      password: "",
      confirmPassword: "",
    },
  });

  function decodeLinkToken(value: string | null | undefined) {
    if (!value) {
      return null;
    }

    if (!/^[0-9a-f]+$/i.test(value) || value.length % 2 !== 0) {
      return value;
    }

    try {
      const bytes = new Uint8Array(
        value.match(/.{1,2}/g)?.map((part) => parseInt(part, 16)) ?? [],
      );
      return new TextDecoder().decode(bytes);
    } catch (error) {
      console.error("Failed to decode auth link token", error);
      return value;
    }
  }

  async function onSubmit(values: z.infer<typeof formSchema>) {
    const token = decodeLinkToken(encodedToken) ?? searchParams.get("token");
    if (!token) {
      toast(registerErrorTitle, { description: invalidTokenMsg.value });
      return;
    }

    setIsLoading(true);
    try {
      // Use direct fetch as it might not be in the openapi spec wrapper yet
      const baseUrl = getBackendBaseUrl() || "";
      // But wait, the backend controller is mapped to /api/auth, not /api/v1/auth
      const apiUrl = baseUrl ? `${baseUrl}/api/auth/register` : "/api/auth/register";

      const response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          token: token,
          password: values.password,
        }),
      });

      if (response.ok) {
        toast(registerSuccessTitle, { description: registerSuccessDesc.value });
        navigate("/login");
      } else {
        const errorText = await response.text();
        throw new Error(errorText || "Registration failed");
      }
    } catch (error: any) {
      toast(registerErrorTitle, { description: error.message });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <MeshGradientBackground
      id="idunn-register-page-bg"
      count={10}
      speedRange={
        theme === "dark"
          ? { min: 0.00005, max: 0.0002 }
          : { min: 0.0004, max: 0.0008 }
      }
      radiusRange={
        theme === "dark" ? { min: 20, max: 90 } : { min: 30, max: 60 }
      }
      lightnessRange={
        theme === "dark" ? { min: 40, max: 50 } : { min: 40, max: 60 }
      }
      saturationRange={
        theme === "dark" ? { min: 30, max: 70 } : { min: 20, max: 60 }
      }
      opacityRange={
        theme === "dark" ? { min: 0.6, max: 0.8 } : { min: 0.1, max: 0.2 }
      }
      theme={theme as "light" | "dark" | "system"}
    >
      <FallingIconsBackground icons={backgroundIcons} iconCount={20}>
        <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden ">
          <div className="absolute -top-[20%] -left-[10%] h-125 w-125 rounded-full bg-primary/10 blur-[100px]" />
          <div className="absolute top-[40%] -right-[10%] h-100 w-100 rounded-full bg-secondary/20 blur-[100px]" />

          <Card className="z-1000 w-full max-w-md border-muted/40 shadow-xl backdrop-blur-sm sm:w-100">
            <CardHeader className="space-y-1 text-center">
              <div className="flex justify-center mb-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <Logo />
                </div>
              </div>
              <CardTitle className="text-2xl font-bold tracking-tight">
                {title}
              </CardTitle>
              <CardDescription>{description}</CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...form}>
                <form
                  onSubmit={form.handleSubmit(onSubmit)}
                  className="space-y-4"
                >
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
                  <FormField
                    control={form.control}
                    name="confirmPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>{confirmPasswordLabel}</FormLabel>
                        <FormControl>
                          <div className="relative">
                            <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                            <Input
                              type="password"
                              placeholder={confirmPasswordPlaceholder.value}
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
                        {registerBtnLoading}
                      </>
                    ) : (
                      registerBtn
                    )}
                  </Button>
                </form>
              </Form>
            </CardContent>
          </Card>
        </div>
      </FallingIconsBackground>
    </MeshGradientBackground>
  );
}
