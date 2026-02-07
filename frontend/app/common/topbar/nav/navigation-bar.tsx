import * as React from "react"
import { useIntlayer, type IntlayerNode } from "react-intlayer"
import { cn } from "@/lib/utils"
import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
    NavigationMenuTrigger,
    navigationMenuTriggerStyle,
} from "@/components/ui/navigation-menu"

export function NavigationBar() {
    // 使用 Intlayer 获取内容
    const { menus, items, logoTitle, logoDescription } = useIntlayer("navigation_bar");

    const setComponents = [
        { title: items.browseSets, href: "/docs/sets/browse", description: items.browseSetsDesc },
        { title: items.mySets, href: "/docs/sets/my", description: items.mySetsDesc },
        { title: items.createSet, href: "/docs/sets/new", description: items.createSetDesc },
    ]

    const brushComponents = [
        { title: items.browseBrushes, href: "/docs/brushes/browse", description: items.browseBrushesDesc },
        { title: items.myBrushes, href: "/docs/brushes/my", description: items.myBrushesDesc },
        { title: items.createBrush, href: "/docs/brushes/new", description: items.createBrushDesc },
    ]

    const utilComponents = [
        { title: items.resize, href: "/docs/utilities/resize", description: items.resizeDesc },
    ]

    return (
        <NavigationMenu>
            <NavigationMenuList>
                {/* TEMPLATES */}
                <NavigationMenuItem>
                    <NavigationMenuTrigger className="bg-transparent">{menus.templates}</NavigationMenuTrigger>
                    <NavigationMenuContent className="bg-background/50 backdrop-blur-2xl">
                        <ul className="grid gap-3 p-6 md:w-100 lg:w-125 lg:grid-cols-[.75fr_1fr]">
                            <li className="row-span-3">
                                <NavigationMenuLink asChild>
                                    <a
                                        className="flex h-full w-full select-none flex-col justify-end rounded-md bg-linear-to-b from-muted/50 to-muted p-6 no-underline outline-none focus:shadow-md"
                                        href="/"
                                    >
                                        <div className="mb-2 mt-4 text-lg font-medium">
                                            {logoTitle}
                                        </div>
                                        <p className="text-sm leading-tight text-muted-foreground">
                                            {logoDescription}
                                        </p>
                                    </a>
                                </NavigationMenuLink>
                            </li>
                            <ListItem href="/templates" title={items.searchTemplates}>
                                {items.searchTemplatesDesc}
                            </ListItem>
                            <ListItem href="/folders" title={items.viewFolders}>
                                {items.viewFoldersDesc}
                            </ListItem>
                            <ListItem href="/tags" title={items.browseTags}>
                                {items.browseTagsDesc}
                            </ListItem>
                        </ul>
                    </NavigationMenuContent>
                </NavigationMenuItem>

                {/* TAGS */}
                <NavigationMenuItem>
                    <NavigationMenuLink asChild className="bg-transparent">
                        <a href="/tags" className={navigationMenuTriggerStyle()}>
                            {menus.tags}
                        </a>
                    </NavigationMenuLink>
                </NavigationMenuItem>

                {/* SETS */}
                <NavigationMenuItem>
                    <NavigationMenuTrigger className="bg-transparent">{menus.sets}</NavigationMenuTrigger>
                    <NavigationMenuContent>
                        <ul className="grid w-100 gap-3 p-4 md:w-125 md:grid-cols-2 lg:w-150 ">
                            {setComponents.map((component, index) => (
                                <ListItem
                                    key={`navbar-set-${component.title}-${index}`}
                                    title={component.title}
                                    href={component.href}
                                >
                                    {component.description}
                                </ListItem>
                            ))}
                        </ul>
                    </NavigationMenuContent>
                </NavigationMenuItem>

                {/* BRUSHES */}
                <NavigationMenuItem>
                    <NavigationMenuTrigger className="bg-transparent">{menus.brushes}</NavigationMenuTrigger>
                    <NavigationMenuContent>
                        <ul className="grid w-100 gap-3 p-4 md:w-125 md:grid-cols-2 lg:w-150 ">
                            {brushComponents.map((component, index) => (
                                <ListItem
                                    key={`navbar-brush-${component.title}-${index}`}
                                    title={component.title}
                                    href={component.href}
                                >
                                    {component.description}
                                </ListItem>
                            ))}
                        </ul>
                    </NavigationMenuContent>
                </NavigationMenuItem>

                {/* BRUSHES */}
                <NavigationMenuItem>
                    <NavigationMenuTrigger className="bg-transparent">{menus.utilities}</NavigationMenuTrigger>
                    <NavigationMenuContent>
                        <ul className="grid w-100 gap-3 p-4 md:w-125 md:grid-cols-2 lg:w-150 ">
                            {utilComponents.map((component, index) => (
                                <ListItem
                                    key={`navbar-brush-${component.title}-${index}`}
                                    title={component.title}
                                    href={component.href}
                                >
                                    {component.description}
                                </ListItem>
                            ))}
                        </ul>
                    </NavigationMenuContent>
                </NavigationMenuItem>
            </NavigationMenuList>
        </NavigationMenu>
    )
}

interface ListItemProps {
    // 修改 title 类型，支持 string, ReactNode 或 Intlayer 的翻译节点
    title?:  React.ReactNode | IntlayerNode | React.ComponentPropsWithoutRef<"a">["title"];
    className?: string;
    children: React.ReactNode;
    href: string;
    [key: string]: any;
}

const ListItem = React.forwardRef<React.ElementRef<"a">, ListItemProps>(
    ({ className, title, children, href }, ref) => {
        return (
            <li>
                <NavigationMenuLink asChild>
                    <a
                        ref={ref}
                        className={cn(
                            "block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
                            className
                        )}
                        href={href}
                    >
                        <div className="text-sm font-semibold leading-none">
                            {/* 直接渲染 title。
                                Intlayer 的 TranslationNode 在被 useIntlayer 提取后
                                会自动转化为当前语言的字符串或 React 节点。
                            */}
                            {title}
                        </div>
                        <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
                            {children}
                        </p>
                    </a>
                </NavigationMenuLink>
            </li>
        )
    }
)
ListItem.displayName = "ListItem"