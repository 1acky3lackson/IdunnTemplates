import React, { useState, useEffect } from 'react';
import { ChevronRight, ChevronDown, Folder, FolderOpen, Loader2, Landmark, PencilLine, LayersPlus } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { IDUNN_API } from '~/api';
import { useIntlayer } from 'react-intlayer';

// 辅助函数：从完整路径中提取文件夹名称
const getFolderName = (path: string) => {
    if (!path) return '';
    const parts = path.split('/').filter(Boolean);
    return parts[parts.length - 1];
};

interface TreeNodeProps {
    path: PathResult;
    level: number;
    selectedPath?: string;
    onSelect: (path: string) => void;
}

interface PathResult {
    'path': string;
    'canSave': boolean;
    'canCommit': boolean;
    'canUse': boolean;
}

const TreeNode = ({ path, level, selectedPath, onSelect }: TreeNodeProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(true); // 默认为 true，因为一挂载就开始加载
    const [children, setChildren] = useState<PathResult[]>([]);

    // 判断当前节点是否被选中
    const isSelected = path.path.endsWith('/') ? selectedPath === path.path : selectedPath === path.path || selectedPath === path.path + '/';
    // 判断当前节点是否是选中节点的父级
    const isParentOfSelected = selectedPath?.startsWith(path + '/');

    // 是否有子节点 (用于 UI 判断)
    const hasChildren = children.length > 0;

    // 1. 初始化挂载时：立即预加载子目录
    useEffect(() => {
        let isMounted = true;

        const fetchChildren = async () => {
            setIsLoading(true);
            try {
                const res = await IDUNN_API.apiV1PathsGet(path.path);
                const subPaths = (res.data) || [];

                if (isMounted) {
                    setChildren(subPaths);
                }
            } catch (error) {
                console.error(`Failed to load children for ${path.path}`, error);
                if (isMounted) setChildren([]);
            } finally {
                if (isMounted) setIsLoading(false);
            }
        };

        fetchChildren();

        return () => { isMounted = false; };
    }, [path]);

    // 2. 自动展开逻辑：如果选中的路径在当前节点内，且当前节点有子数据，则展开
    useEffect(() => {
        if (isParentOfSelected && !isOpen && hasChildren) {
            setIsOpen(true);
        }
    }, [isParentOfSelected, hasChildren]);

    // 处理展开/折叠点击
    const handleToggle = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (hasChildren && !isLoading) {
            setIsOpen(!isOpen);
        }
    };

    // 处理选中点击
    const handleSelect = () => {
        onSelect(path.path);
    };

    return (
        <div className="select-none">
            <Button
                variant="ghost"
                size="sm"
                className={cn(
                    "w-full justify-start hover:bg-muted/50 h-8 px-2 font-normal flex flex-row justify-between",
                    isSelected && "bg-accent text-accent-foreground font-medium",
                    level > 0 && "ml-0"
                )}
                style={{ paddingLeft: `${level * 12 + 8}px` }}
                onClick={handleSelect}
            >
                {/* 展开/折叠 图标区域 
                  注意：即使没有子节点，这里也需要保留占位空间，以保证文本对齐
                */}
                <div className="flex flex-row">
                    <div
                        className={cn(
                            "mr-1 p-0.5 rounded shrink-0 flex items-center justify-center h-4 w-4",
                            hasChildren ? "cursor-pointer hover:bg-muted" : "cursor-default"
                        )}
                        onClick={handleToggle}
                    >
                        {isLoading ? (
                            <Loader2 className="h-3 w-3 animate-spin text-muted-foreground/70" />
                        ) : hasChildren ? (
                            isOpen ? (
                                <ChevronDown className="h-3 w-3 text-muted-foreground" />
                            ) : (
                                <ChevronRight className="h-3 w-3 text-muted-foreground" />
                            )
                        ) : (
                            // 占位符：没有子节点时不显示图标，但占据空间
                            <span className="w-3 h-3 block" />
                        )}
                    </div>

                    {/* 文件夹图标 */}
                    {isOpen ? (
                        <FolderOpen className="mr-2 h-4 w-4 text-blue-500/80" />
                    ) : (
                        <Folder className={cn(
                            "mr-2 h-4 w-4",
                            // 如果没有子节点且没被选中，稍微调低透明度，显得"空"一点（可选）
                            !hasChildren && !isSelected ? "text-blue-500/40" : "text-blue-500/60"
                        )} />
                    )}

                    <span className="truncate">{getFolderName(path.path)}</span>
                </div>

                {
                    !isLoading && (
                        <div className="inline-flex flex-row gap-1 text-xs align-middle transition-transform duration-300">
                            {
                                path.canUse && (
                                    <span className="relative text-[10px] bg-primary/10 text-primary p-0.5 rounded-full font-bold top-0 hover:-top-1.5 transition-all duration-300">
                                        <Landmark className="w-2 h-2" />
                                    </span>
                                )
                            }
                            {
                                path.canCommit && (
                                    <span className="relative text-[10px] bg-primary/10 text-primary p-0.5 rounded-full font-bold top-0 hover:-top-1.5 transition-all duration-300">
                                        <PencilLine className="w-2 h-2" />
                                    </span>
                                )
                            }
                            {
                                path.canSave && (
                                    <span className="relative text-[10px] bg-primary/10 text-primary p-0.5 rounded-full font-bold top-0 hover:-top-1.5 transition-all duration-300">
                                        <LayersPlus className="w-2 h-2" />
                                    </span>
                                )
                            }
                        </div>
                    )
                }
            </Button>

            {/* 子节点渲染：只有在展开 且 有子节点时才渲染 */}
            {isOpen && hasChildren && (
                <div className="flex flex-col border-l border-border/40 ml-4">
                    {children.map((childPath) => (
                        <TreeNode
                            key={childPath.path}
                            path={childPath}
                            level={level + 1}
                            selectedPath={selectedPath}
                            onSelect={onSelect}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

interface DirectoryTreeProps {
    currentPath?: string;
    onSelect: (path: string) => void;
}

export const DirectoryTree = ({ currentPath, onSelect }: DirectoryTreeProps) => {
    const [rootPaths, setRootPaths] = useState<PathResult[]>([]);
    const [loading, setLoading] = useState(true);
    const { folder } = useIntlayer("directory");

    useEffect(() => {
        const fetchRoots = async () => {
            try {
                // 获取根目录
                const res = await IDUNN_API.apiV1PathsGet("");
                setRootPaths((res.data) || []);
            } catch (error) {
                console.error("Failed to load root paths", error);
            } finally {
                setLoading(false);
            }
        };
        fetchRoots();
    }, []);

    return (
        <div className="space-y-1">
            {/* "全部" 选项 */}
            <Button
                variant={!currentPath ? "secondary" : "ghost"}
                size="sm"
                className="w-full justify-start h-8 px-2"
                onClick={() => onSelect('')}
            >
                {/* 使用一个通用的 Folder 图标表示全部 */}
                <div className="w-4 flex justify-center"></div> {/* 模拟图标区的缩进 */}
                <Folder className="mr-2 h-4 w-4 text-foreground/50" />
                {folder.all}
            </Button>

            {loading ? (
                <div className="flex items-center justify-center p-4">
                    <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                </div>
            ) : (
                rootPaths.map(path => (
                    <TreeNode
                        key={path.path}
                        path={path}
                        level={0}
                        selectedPath={currentPath}
                        onSelect={onSelect}
                    />
                ))
            )}
        </div>
    );
};