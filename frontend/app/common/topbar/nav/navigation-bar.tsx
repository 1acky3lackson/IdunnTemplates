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
} from "lucide-react";

export function NavigationBar() {
  const { menus, items, logoTitle, logoDescription } =
    useIntlayer("navigation_bar");

  // --- 统一的数据定义 ---
  const menuData = {
    templates: {
      trigger: menus.templates,
      main: { title: logoTitle, desc: logoDescription, href: "/" },
      children: [
        {
          title: items.searchTemplates,
          href: "/templates",
          desc: items.searchTemplatesDesc,
        },
        {
          title: items.viewFolders,
          href: "/folders",
          desc: items.viewFoldersDesc,
        },
        { title: items.browseTags, href: "/tags", desc: items.browseTagsDesc },
      ],
    },
    sets: {
      trigger: menus.sets,
      children: [
        {
          title: items.browseSets,
          href: "/docs/sets/browse",
          desc: items.browseSetsDesc,
        },
        { title: items.mySets, href: "/docs/sets/my", desc: items.mySetsDesc },
        {
          title: items.createSet,
          href: "/docs/sets/new",
          desc: items.createSetDesc,
        },
      ],
    },
    brushes: {
      trigger: menus.brushes,
      children: [
        {
          title: items.browseBrushes,
          href: "/docs/brushes/browse",
          desc: items.browseBrushesDesc,
        },
        {
          title: items.myBrushes,
          href: "/docs/brushes/my",
          desc: items.myBrushesDesc,
        },
        {
          title: items.createBrush,
          href: "/docs/brushes/new",
          desc: items.createBrushDesc,
        },
      ],
    },
    commercial: {
      trigger: menus.commercial,
      children: [
        {
          icon: <Landmark className="w-4 h-4" />,
          title: items.commercial.balances.title,
          href: "/commercial/balances",
          desc: items.commercial.balances.desc,
        },
        {
          icon: <Calculator className="w-4 h-4" />,
          title: items.commercial.checkout.title,
          href: "/commercial/checkout",
          desc: items.commercial.checkout.desc,
        },
        {
          icon: <ArrowLeftRight className="w-4 h-4" />,
          title: items.commercial.transactions.title,
          href: "/commercial/balances/transactions",
          desc: items.commercial.transactions.desc,
        },
        {
          icon: <Parentheses className="w-4 h-4" />,
          title: items.commercial.globalParams.title,
          href: "/commercial/global-params",
          desc: items.commercial.globalParams.desc,
        },
        {
          icon: <FolderKanban className="w-4 h-4" />,
          title: items.commercial.projects.title,
          href: "/commercial/projects",
          desc: items.commercial.projects.desc,
        },
        {
          icon: <PackageSearch className="w-4 h-4" />,
          title: items.commercial.neteaseProducts.title,
          href: "/commercial/netease-products",
          desc: items.commercial.neteaseProducts.desc,
        },
        {
          icon: <Coins className="w-4 h-4" />,
          title: items.commercial.neteaseOrders.title,
          href: "/commercial/orders",
          desc: items.commercial.neteaseOrders.desc,
        },
        {
          icon: <Coins className="w-4 h-4" />,
          title: items.commercial.neteaseWithdraw.title,
          href: "/commercial/netease-withdraws",
          desc: items.commercial.neteaseWithdraw.desc,
        },
        {
          icon: <Coins className="w-4 h-4" />,
          title: items.commercial.systemWithdraw.title,
          href: "/commercial/withdraws",
          desc: items.commercial.systemWithdraw.desc,
        },
      ],
    },
  };

  return (
    <>
      {/* --- 桌面端版本 (md 以上显示) --- */}
      <div className="hidden md:flex items-center justify-center w-full py-4">
        <NavigationMenu>
          <NavigationMenuList>
            {/* Templates 特殊布局 */}
            <NavigationMenuItem>
              <NavigationMenuTrigger className="bg-transparent">
                {menuData.templates.trigger}
              </NavigationMenuTrigger>
              <NavigationMenuContent className="bg-background/50 backdrop-blur-2xl">
                <ul className="grid gap-3 p-6 md:w-100 lg:w-125 lg:grid-cols-[.75fr_1fr]">
                  <li className="row-span-3">
                    <NavigationMenuLink asChild>
                      <a
                        className="flex h-full w-full select-none flex-col justify-end rounded-md bg-linear-to-b from-muted/50 to-muted p-6 no-underline"
                        href={menuData.templates.main.href}
                      >
                        <div className="mb-2 mt-4 text-lg font-medium">
                          {menuData.templates.main.title}
                        </div>
                        <p className="text-sm leading-tight text-muted-foreground">
                          {menuData.templates.main.desc}
                        </p>
                      </a>
                    </NavigationMenuLink>
                  </li>
                  {menuData.templates.children.map((child, i) => (
                    <ListItem key={i} href={child.href} title={child.title}>
                      {child.desc}
                    </ListItem>
                  ))}
                </ul>
              </NavigationMenuContent>
            </NavigationMenuItem>

            {/* 标签链接 */}
            <NavigationMenuItem>
              <NavigationMenuLink asChild className="bg-transparent">
                <a href="/tags" className={navigationMenuTriggerStyle()}>
                  {menus.tags}
                </a>
              </NavigationMenuLink>
            </NavigationMenuItem>

            {/* 通用下拉菜单: Sets, Brushes, Commercial */}
            {[menuData.sets, menuData.brushes, menuData.commercial].map(
              (group, idx) => (
                <NavigationMenuItem key={idx}>
                  <NavigationMenuTrigger className="bg-transparent">
                    {group.trigger}
                  </NavigationMenuTrigger>
                  <NavigationMenuContent>
                    <ul className="grid w-100 gap-3 p-4 md:w-125 md:grid-cols-2 lg:w-150">
                      {group.children.map((item, i) => (
                        <div
                          key={i}
                          className="flex flex-row gap-1 items-center"
                        >
                          {"icon" in item && (
                            <div className="ml-2">{item.icon}</div>
                          )}
                          <ListItem title={item.title} href={item.href}>
                            {item.desc}
                          </ListItem>
                        </div>
                      ))}
                    </ul>
                  </NavigationMenuContent>
                </NavigationMenuItem>
              ),
            )}
          </NavigationMenuList>
        </NavigationMenu>
      </div>

      {/* --- 移动端悬浮按钮 (md 以下显示) --- */}
      <div className="fixed bottom-6 right-6 z-50 md:hidden">
        <Sheet>
          <SheetTrigger asChild>
            <Button
              size="icon"
              className="h-14 w-14 rounded-full shadow-xl ring-1 ring-border"
            >
              <Menu className="h-6 w-6" />
            </Button>
          </SheetTrigger>
          <SheetContent side="right" className="w-[85%] p-0">
            <SheetHeader className="p-6 text-left border-b">
              <SheetTitle>{logoTitle}</SheetTitle>
            </SheetHeader>
            <ScrollArea className="h-[calc(100vh-80px)] px-6">
              <div className="py-4 border-b">
                <a
                  href="/tags"
                  className="flex items-center justify-between py-2 text-lg font-medium"
                >
                  {menus.tags} <ChevronRight className="w-4 h-4" />
                </a>
              </div>
              <Accordion type="single" collapsible className="w-full pb-10">
                {Object.entries(menuData).map(([key, section]) => (
                  <AccordionItem value={key} key={key}>
                    <AccordionTrigger className="text-lg font-medium">
                      {section.trigger}
                    </AccordionTrigger>
                    <AccordionContent>
                      <div className="flex flex-col gap-1 pl-2 border-l-2 ml-1">
                        {section.children.map((item, i) => (
                          <a
                            key={i}
                            href={item.href}
                            className="flex items-center gap-3 rounded-md p-3 hover:bg-accent"
                          >
                            {"icon" in item && item.icon}
                            <div className="flex flex-col">
                              <span className="font-semibold text-sm">
                                {item.title as React.ReactNode}
                              </span>
                              <span className="text-xs text-muted-foreground line-clamp-1">
                                {item.desc}
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
