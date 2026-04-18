import React, { useRef, useState } from "react";
import { GenericCrudTable, type ColumnSchema, type GenericCrudTableHandle, type TableActions } from "~/common/generic-crud-table/generic-crud-table";
import { IDUNN_API } from "~/api";
import type { TemplateCollection } from "~/api/generated/model/template-collection";
import { Button } from "~/components/ui/button";
import { Check, Clock3, Lock, Pencil, Shuffle, Sparkles, Trash2, UserRound } from "lucide-react";
import { RandomTemplateDialog } from "./random-template-dialog";
import { toast } from "sonner";
import { useNavigate } from "react-router";
import { Badge } from "~/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "~/components/ui/dialog";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Switch } from "~/components/ui/switch";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "~/components/ui/tooltip";
import { useIntlayer } from "react-intlayer";
import { cn } from "~/lib/utils";

export interface TemplateCollectionListProps {
    onSelect?: (collection: TemplateCollection) => void;
}

export const TemplateCollectionList: React.FC<TemplateCollectionListProps> = ({ onSelect }) => {
    const navigate = useNavigate();
    const tableRef = useRef<GenericCrudTableHandle | null>(null);
    const { title, subtitle, columns, actions, dialogs, messages } = useIntlayer("template-collection");

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isRandomOpen, setIsRandomOpen] = useState(false);
    const [randomCollectionId, setRandomCollectionId] = useState<string | null>(null);
    const [currentCollection, setCurrentCollection] = useState<TemplateCollection | null>(null);
    
    // Form state
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [isPrivate, setIsPrivate] = useState(false);

    const loadData = async (page: number, size: number, search: string, sort: string) => {
        const response = await IDUNN_API.apiV1CollectionsGet(search || undefined, page, size, sort || undefined);
        return response.data as any; // Cast to PageResponse
    };

    const schema: Record<string, ColumnSchema<TemplateCollection>> = {
        name: {
            title: columns.name.value,
            sortable: true,
            filterable: true,
            render: (val, row) => (
                <div onClick={() => onSelect ? onSelect(row) : navigate(`/collections/${row.id}`)} className="cursor-pointer font-medium hover:underline text-primary">
                    {val}
                </div>
            )
        },
        description: {
            title: columns.description.value,
            sortable: true,
            render: (val) => {
                if (!val) return <span className="text-muted-foreground">-</span>;
                if (val.length <= 10) return <span className="text-muted-foreground">{val}</span>;
                return (
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <span className="text-muted-foreground cursor-help">{val.slice(0, 10)}...</span>
                            </TooltipTrigger>
                            <TooltipContent className="max-w-[300px] break-words">
                                <p>{val}</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                );
            }
        },
        privateCollection: {
            title: columns.visibility.value,
            sortable: true,
            render: (val) => (
                <Badge variant={val ? "secondary" : "default"}>
                    {val ? columns.private.value : columns.public.value}
                </Badge>
            )
        },
        creatorName: {
            title: columns.creator.value,
            sortable: true,
        },
        createTimeMs: {
            title: columns.createdAt.value,
            sortable: true,
            render: (val) => new Date(val).toLocaleDateString()
        }
    };

    const searchFields = [
        { key: "name", label: columns.name.value, fuzzy: true },
        { key: "creatorName", label: columns.creator.value, fuzzy: true }
    ];

    const renderCollectionPickerCard = (row: TemplateCollection) => {
        const createdAt = row.createTimeMs ? new Date(row.createTimeMs).toLocaleDateString() : "-";
        return (
            <button
                key={row.id}
                type="button"
                onClick={() => onSelect?.(row)}
                className={cn(
                    "group w-full rounded-2xl border border-border/60 bg-card/80 p-4 text-left transition-all duration-200",
                    "hover:-translate-y-0.5 hover:border-primary/35 hover:bg-primary/5 hover:shadow-md",
                )}
            >
                <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                        <div className="flex items-center gap-2">
                            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary/10 text-primary">
                                <Sparkles className="h-4 w-4" />
                            </div>
                            <div className="min-w-0">
                                <div className="truncate text-sm font-semibold text-foreground">{row.name}</div>
                                <div className="mt-0.5 flex items-center gap-2 text-xs text-muted-foreground">
                                    <span className="inline-flex items-center gap-1">
                                        <UserRound className="h-3.5 w-3.5" />
                                        {row.creatorName || "-"}
                                    </span>
                                    <span>·</span>
                                    <span>{createdAt}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="flex shrink-0 items-center gap-2">
                        <Badge variant={row.privateCollection ? "secondary" : "default"}>
                            {row.privateCollection ? (
                                <span className="inline-flex items-center gap-1">
                                    <Lock className="h-3 w-3" />
                                    {columns.private.value}
                                </span>
                            ) : (
                                columns.public.value
                            )}
                        </Badge>
                    </div>
                </div>
                <div className="mt-3 line-clamp-2 min-h-10 text-sm text-muted-foreground">
                    {row.description || subtitle}
                </div>
                <div className="mt-4 flex items-center justify-between">
                    <div className="text-xs text-muted-foreground">#{row.id}</div>
                    <div className="inline-flex items-center gap-2 rounded-full bg-primary px-3 py-1 text-xs font-medium text-primary-foreground">
                        <Check className="h-3.5 w-3.5" />
                        {actions.add}
                    </div>
                </div>
            </button>
        );
    };

    const renderCollectionBrowseCard = (row: TemplateCollection, tableActions: TableActions) => {
        const createdAt = row.createTimeMs ? new Date(row.createTimeMs).toLocaleDateString() : "-";
        return (
            <div
                key={row.id}
                className={cn(
                    "group overflow-hidden rounded-3xl border border-border/60 bg-card/90 shadow-sm transition-all duration-200",
                    "hover:-translate-y-1 hover:border-primary/35 hover:shadow-lg"
                )}
            >
                <button
                    type="button"
                    onClick={() => navigate(`/collections/${row.id}`)}
                    className="block w-full text-left"
                >
                    <div className="relative overflow-hidden border-b border-border/50 bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.12),_transparent_40%),radial-gradient(circle_at_bottom_right,_rgba(236,72,153,0.12),_transparent_40%)] px-5 py-5">
                        <div className="flex items-start justify-between gap-3">
                            <div className="min-w-0">
                                <div className="inline-flex items-center gap-2 rounded-full bg-background/80 px-3 py-1 text-xs font-medium text-primary shadow-sm backdrop-blur">
                                    <Sparkles className="h-3.5 w-3.5" />
                                    {columns.name.value}
                                </div>
                                <h3 className="mt-3 line-clamp-1 text-xl font-semibold tracking-tight text-foreground">
                                    {row.name}
                                </h3>
                                <p className="mt-2 line-clamp-2 min-h-10 text-sm text-muted-foreground">
                                    {row.description || subtitle}
                                </p>
                            </div>
                            <Badge variant={row.privateCollection ? "secondary" : "default"} className="shrink-0">
                                {row.privateCollection ? (
                                    <span className="inline-flex items-center gap-1">
                                        <Lock className="h-3 w-3" />
                                        {columns.private.value}
                                    </span>
                                ) : (
                                    columns.public.value
                                )}
                            </Badge>
                        </div>
                    </div>
                </button>

                <div className="space-y-4 px-5 py-4">
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
                        <TooltipProvider>
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <button
                                        type="button"
                                        onClick={() => handleCreatorFilter(row.creatorName)}
                                        className="inline-flex items-center gap-1.5 rounded-full px-2 py-1 -mx-2 -my-1 transition-colors hover:bg-primary/8 hover:text-primary"
                                    >
                                        <UserRound className="h-4 w-4" />
                                        {row.creatorName || "-"}
                                    </button>
                                </TooltipTrigger>
                                <TooltipContent>
                                    <p>点击筛选该创建者</p>
                                </TooltipContent>
                            </Tooltip>
                        </TooltipProvider>
                        <span className="inline-flex items-center gap-1.5">
                            <Clock3 className="h-4 w-4" />
                            {createdAt}
                        </span>
                        <span className="text-xs text-muted-foreground/80">#{row.id}</span>
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                        <Button onClick={() => navigate(`/collections/${row.id}`)}>
                            {actions.addTemplate}
                        </Button>
                        <Button
                            variant="outline"
                            className="border-indigo-200 text-indigo-600 hover:bg-indigo-50 hover:text-indigo-700 dark:border-indigo-900 dark:hover:bg-indigo-950"
                            onClick={() => handleOpenRandom(row.id.toString())}
                        >
                            <Shuffle className="mr-2 h-4 w-4" />
                            {actions.random}
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleEdit(row)}>
                            <Pencil className="mr-2 h-4 w-4" />
                            {actions.save}
                        </Button>
                        <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive hover:text-destructive"
                            onClick={() => handleDelete(row, tableActions)}
                        >
                            <Trash2 className="mr-2 h-4 w-4" />
                            {actions.remove}
                        </Button>
                    </div>
                </div>
            </div>
        );
    };

    const handleCreate = () => {
        setName("");
        setDescription("");
        setIsPrivate(false);
        setIsCreateOpen(true);
    };

    const handleEdit = (row: TemplateCollection) => {
        setCurrentCollection(row);
        setName(row.name);
        setDescription(row.description || "");
        setIsPrivate(row.privateCollection);
        setIsEditOpen(true);
    };

    const handleDelete = async (row: TemplateCollection, tableActions: TableActions) => {
        if (confirm(`${dialogs.confirmDeleteCollection.value} (${row.name})`)) {
            try {
                await IDUNN_API.apiV1CollectionsIdDelete(row.id.toString());
                toast.success(messages.deleteSuccess.value);
                tableActions.refreshPage();
            } catch (err) {
                toast.error(messages.deleteFailed.value);
            }
        }
    };

    const submitCreate = async () => {
        try {
            await IDUNN_API.apiV1CollectionsPost({
                name,
                description,
                privateCollection: isPrivate
            });
            toast.success(messages.createSuccess.value);
            setIsCreateOpen(false);
            tableRef.current?.refreshPage();
        } catch (err) {
            toast.error(messages.createFailed.value);
        }
    };

    const submitEdit = async () => {
        if (!currentCollection) return;
        try {
            await IDUNN_API.apiV1CollectionsIdPut(currentCollection.id.toString(), {
                name,
                description,
                privateCollection: isPrivate
            });
            toast.success(messages.updateSuccess.value);
            setIsEditOpen(false);
            tableRef.current?.refreshPage();
        } catch (err) {
            toast.error(messages.updateFailed.value);
        }
    };

    const handleOpenRandom = (id: string) => {
        setRandomCollectionId(id);
        setIsRandomOpen(true);
    };

    const handleCreatorFilter = (creatorName?: string | null) => {
        if (!creatorName || onSelect) return;
        tableRef.current?.setSearchFilter("creatorName", creatorName);
    };

    return (
        <div className={onSelect ? "" : "p-6"}>
            {!onSelect && (
                <div className="mb-6 flex flex-col gap-2">
                    <h1 className="text-2xl font-bold tracking-tight">{title}</h1>
                    <p className="text-muted-foreground">{subtitle}</p>
                </div>
            )}

            <GenericCrudTable<TemplateCollection>
                ref={tableRef}
                uid="tcol"
                list={loadData}
                schema={schema}
                searchFields={searchFields}
                getRowId={(row) => row.id}
                create={onSelect ? undefined : handleCreate}
                modify={onSelect ? undefined : handleEdit}
                deleteAction={onSelect ? undefined : handleDelete}
                rowActions={(row) => {
                    if (onSelect) {
                        return (
                            <Button variant="default" size="sm" onClick={(e) => { e.stopPropagation(); onSelect(row); }}>
                                {actions.add}
                            </Button>
                        );
                    }
                    return (
                        <Button 
                            variant="outline" 
                            size="sm" 
                            className="mr-2 text-indigo-500 hover:text-indigo-600 border-indigo-200 hover:bg-indigo-50 dark:border-indigo-800 dark:hover:bg-indigo-950" 
                            onClick={(e) => { e.stopPropagation(); handleOpenRandom(row.id.toString()); }}>
                            <Shuffle className="w-3.5 h-3.5 mr-1" />
                            {actions.random}
                        </Button>
                    );
                }}
                pageSize={20}
                customRenderer={{
                    renderContainer: (nodes, meta) => (
                        <div className="space-y-5">
                            {onSelect ? (
                                <div className="rounded-2xl border border-dashed border-primary/20 bg-primary/5 px-4 py-3">
                                    <div className="text-sm font-medium text-foreground">{dialogs.addTemplateTitle}</div>
                                    <div className="mt-1 text-sm text-muted-foreground">
                                        选择一个合集，立即将当前模板加入其中。
                                    </div>
                                </div>
                            ) : (
                                <div className="rounded-3xl border border-border/60 bg-[linear-gradient(135deg,rgba(59,130,246,0.08),rgba(236,72,153,0.05))] px-5 py-5">
                                    <div className="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
                                        <div>
                                            <div className="text-lg font-semibold tracking-tight text-foreground">
                                                发现适合当前场景的模板合集
                                            </div>
                                            <div className="mt-1 text-sm text-muted-foreground">
                                                卡片用于浏览与进入合集，上方工具栏仍可按名称、创建者与创建时间筛选、排序。
                                            </div>
                                        </div>
                                        <div className="text-xs text-muted-foreground">
                                            当前保留：搜索、创建者筛选、创建时间排序、可见性展示
                                        </div>
                                    </div>
                                </div>
                            )}

                            {meta.loading ? (
                                <div className={cn("grid gap-4", onSelect ? "grid-cols-1 md:grid-cols-2" : "grid-cols-1 lg:grid-cols-2 xl:grid-cols-3")}>{nodes}</div>
                            ) : meta.isEmpty ? (
                                <div className="rounded-2xl border border-dashed px-6 py-10 text-center text-sm text-muted-foreground">
                                    {onSelect ? "暂无可选合集" : "暂时还没有找到符合条件的合集"}
                                </div>
                            ) : (
                                <div className={cn("grid gap-4", onSelect ? "grid-cols-1 md:grid-cols-2" : "grid-cols-1 lg:grid-cols-2 xl:grid-cols-3")}>{nodes}</div>
                            )}
                        </div>
                    ),
                    renderElement: (row, tableActions) =>
                        onSelect ? renderCollectionPickerCard(row) : renderCollectionBrowseCard(row, tableActions),
                    renderLoading: () => (
                        <div className={cn("rounded-2xl border bg-muted/40 animate-pulse", onSelect ? "h-40" : "h-72")} />
                    ),
                    renderEmpty: () => (
                        <div className="rounded-2xl border border-dashed px-6 py-10 text-center text-sm text-muted-foreground">
                            {onSelect ? "暂无可选合集" : "暂时还没有找到符合条件的合集"}
                        </div>
                    )
                }}
            />

            <RandomTemplateDialog 
                collectionId={randomCollectionId} 
                open={isRandomOpen} 
                onOpenChange={setIsRandomOpen} 
            />

            {/* Create Dialog */}
            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{dialogs.createCollectionTitle}</DialogTitle>
                        <DialogDescription>{dialogs.createCollectionDesc}</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="name" className="text-right">{columns.name}</Label>
                            <Input id="name" value={name} onChange={(e) => setName(e.target.value)} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="description" className="text-right">{columns.description}</Label>
                            <Input id="description" value={description} onChange={(e) => setDescription(e.target.value)} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="private" className="text-right">{columns.private}</Label>
                            <div className="col-span-3 flex items-center space-x-2">
                                <Switch id="private" checked={isPrivate} onCheckedChange={setIsPrivate} />
                                <Label htmlFor="private">{dialogs.onlyVisibleToYou}</Label>
                            </div>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsCreateOpen(false)}>{actions.cancel}</Button>
                        <Button onClick={submitCreate}>{actions.create}</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Edit Dialog */}
            <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{dialogs.editCollectionTitle}</DialogTitle>
                        <DialogDescription>{dialogs.editCollectionDesc}</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="edit-name" className="text-right">{columns.name}</Label>
                            <Input id="edit-name" value={name} onChange={(e) => setName(e.target.value)} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="edit-desc" className="text-right">{columns.description}</Label>
                            <Input id="edit-desc" value={description} onChange={(e) => setDescription(e.target.value)} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="edit-private" className="text-right">{columns.private}</Label>
                            <div className="col-span-3 flex items-center space-x-2">
                                <Switch id="edit-private" checked={isPrivate} onCheckedChange={setIsPrivate} />
                                <Label htmlFor="edit-private">{dialogs.onlyVisibleToYou}</Label>
                            </div>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsEditOpen(false)}>{actions.cancel}</Button>
                        <Button onClick={submitEdit}>{actions.save}</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};
