import * as React from "react";
import { useIntlayer, type IntlayerNode } from "react-intlayer";
import { cn } from "@/lib/utils";
import {
  NavigationMenu,
  NavigationMenuContent,
  NavigationMenuItem,
  NavigationMenuLink,
  NavigationMenuList,
  NavigationMenuTrigger,
  navigationMenuTriggerStyle,
} from "@/components/ui/navigation-menu";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  Menu,
  ArrowLeftRight,
  Calculator,
  Coins,
  FolderKanban,
  Landmark,
  PackageSearch,
  Parentheses,
  ChevronRight,
  Server,
  Layers,
  Library,
} from "lucide-react";

export function NavigationBar({ layout = "both" }: { layout?: "desktop" | "mobile" | "both" } = {}) {
  const { menus, items, logoTitle, logoDescription } =
    useIntlayer("navigation_bar");

  // 直接链接
  const directLinks: any[] = [];

  // 一级菜单
  const dropdownMenus = [
    {
      trigger: menus.templateManagement,
      children: [
        {
          icon: <Layers className="w-4 h-4" />,
          title: menus.templates,
          href: "/",
          desc: items.searchTemplatesDesc,
        },
        {
          icon: <Library className="w-4 h-4" />,
          title: menus.sets,
          href: "/collections",
          desc: items.browseSetsDesc,
        },
      ],
    },
    {
      trigger: menus.projectCollab,
      children: [
        {
          icon: <FolderKanban className="w-4 h-4" />,
          title: items.commercial.projects.title,
          href: "/commercial/projects",
          desc: items.commercial.projects.desc,
        },
        {
          icon: <PackageSearch className="w-4 h-4" />,
          title: items.commercial.neteaseProducts.title,
          href: "/commercial/products",
          desc: items.commercial.neteaseProducts.desc,
        },
      ],
    },
    {
      trigger: menus.revenueSharing,
      children: [
        {
          icon: <Coins className="w-4 h-4" />,
          title: items.commercial.neteaseOrders.title,
          href: "/commercial/orders",
          desc: items.commercial.neteaseOrders.desc,
        },
        {
          icon: <Calculator className="w-4 h-4" />,
          title: items.commercial.checkout.title,
          href: "/commercial/checkout",
          desc: items.commercial.checkout.desc,
        },
        {
          icon: <Landmark className="w-4 h-4" />,
          title: items.commercial.balances.title,
          href: "/commercial/balances",
          desc: items.commercial.balances.desc,
        },
        {
          icon: <ArrowLeftRight className="w-4 h-4" />,
          title: items.commercial.transactions.title,
          href: "/commercial/balances/transactions",
          desc: items.commercial.transactions.desc,
        },
        // {
        //   icon: <Coins className="w-4 h-4" />,
        //   title: items.commercial.systemWithdraw.title,
        //   href: "/commercial/withdraws",
        //   desc: items.commercial.systemWithdraw.desc,
        // },

        // {
        //   icon: <Coins className="w-4 h-4" />,
        //   title: items.commercial.neteaseWithdraw.title,
        //   href: "/commercial/netease-withdraws",
        //   desc: items.commercial.neteaseWithdraw.desc,
        // },
      ],
    },
    {
      trigger: menus.system,
      children: [
        {
          icon: <Parentheses className="w-4 h-4" />,
          title: items.commercial.globalParams.title,
          href: "/commercial/global-params",
          desc: items.commercial.globalParams.desc,
        },
        {
          icon: <Server className="w-4 h-4" />,
          title: items.commercial.servers.title,
          href: "/servers",
          desc: items.commercial.servers.desc,
        },
      ],
    },
  ];

  return (
    <>
      {/* --- 桌面端版本 (md 以上显示) --- */}
      {layout !== "mobile" && (
        <div className="hidden md:flex items-center justify-center w-full py-4">
          <NavigationMenu>
            <NavigationMenuList>
              {/* 直接链接 */}
              {directLinks.map((link, idx) => (
                <NavigationMenuItem key={idx}>
                  <NavigationMenuLink asChild className="bg-transparent">
                    <a href={link.href} className={navigationMenuTriggerStyle()}>
                      {link.title as React.ReactNode}
                    </a>
                  </NavigationMenuLink>
                </NavigationMenuItem>
              ))}

              {/* 下拉菜单 */}
              {dropdownMenus.map((group, idx) => (
                <NavigationMenuItem key={idx}>
                  <NavigationMenuTrigger className="bg-transparent">
                    {group.trigger as React.ReactNode}
                  </NavigationMenuTrigger>
                  <NavigationMenuContent>
                    <ul className="grid w-100 gap-3 p-4 md:w-125 md:grid-cols-2 lg:w-150">
                      {group.children.map((item, i) => (
                        <div
                          key={i}
                          className="flex flex-row gap-1 items-center"
                        >
                          {item.icon && (
                            <div className="ml-2">{item.icon}</div>
                          )}
                          <ListItem title={item.title} href={item.href}>
                            {item.desc as React.ReactNode}
                          </ListItem>
                        </div>
                      ))}
                    </ul>
                  </NavigationMenuContent>
                </NavigationMenuItem>
              ))}
            </NavigationMenuList>
          </NavigationMenu>
        </div>
      )}

      {/* --- 移动端菜单布局 (md 以下显示) --- */}
      {layout !== "desktop" && (
        <div className="md:hidden flex items-center mr-2">
          <Sheet>
            <SheetTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="md:hidden"
              >
                <Menu className="h-5 w-5" />
                <span className="sr-only">Toggle menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-[85%] p-0">
              <SheetHeader className="p-6 text-left border-b">
                <SheetTitle>{logoTitle as React.ReactNode}</SheetTitle>
              </SheetHeader>
              <ScrollArea className="h-[calc(100vh-80px)] px-6">
                <div className="py-4 border-b space-y-2">
                  {directLinks.map((link, idx) => (
                    <a
                      key={idx}
                      href={link.href}
                      className="flex items-center justify-between py-2 text-base font-medium"
                    >
                      {link.title as React.ReactNode} <ChevronRight className="w-4 h-4" />
                    </a>
                  ))}
                </div>
                <Accordion type="single" collapsible className="w-full pb-10 mt-4">
                  {dropdownMenus.map((section, idx) => (
                    <AccordionItem value={`section-${idx}`} key={idx}>
                      <AccordionTrigger className="text-base font-medium hover:no-underline">
                        {section.trigger as React.ReactNode}
                      </AccordionTrigger>
                      <AccordionContent>
                        <div className="flex flex-col gap-1 pl-2 border-l-2 ml-1">
                          {section.children.map((item, i) => (
                            <a
                              key={i}
                              href={item.href}
                              className="flex items-center gap-3 rounded-md p-3 hover:bg-accent"
                            >
                              {item.icon}
                              <div className="flex flex-col">
                                <span className="font-semibold text-sm">
                                  {item.title as React.ReactNode}
                                </span>
                                <span className="text-xs text-muted-foreground line-clamp-1">
                                  {item.desc as React.ReactNode}
                                </span>
                              </div>
                            </a>
                          ))}
                        </div>
                      </AccordionContent>
                    </AccordionItem>
                  ))}
                </Accordion>
              </ScrollArea>
            </SheetContent>
          </Sheet>
        </div>
      )}
    </>
  );
}

// --- 辅助组件 ListItem ---
const ListItem = React.forwardRef<
  React.ElementRef<"a">,
  { title: any; href: string; children: React.ReactNode; className?: string }
>(({ className, title, children, href }, ref) => (
  <li className="list-none w-full">
    <NavigationMenuLink asChild>
      <a
        ref={ref}
        href={href}
        className={cn(
          "block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground",
          className,
        )}
      >
        <div className="text-sm font-semibold leading-none">{title}</div>
        <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
          {children}
        </p>
      </a>
    </NavigationMenuLink>
  </li>
));
ListItem.displayName = "ListItem";
