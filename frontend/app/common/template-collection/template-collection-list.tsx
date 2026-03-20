import React, { useRef, useState } from "react";
import { GenericCrudTable, type ColumnSchema, type TableActions } from "~/common/generic-crud-table/generic-crud-table";
import { IDUNN_API } from "~/api";
import type { TemplateCollection } from "~/api/generated/model/template-collection";
import { Button } from "~/components/ui/button";
import { Shuffle } from "lucide-react";
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

export interface TemplateCollectionListProps {
    onSelect?: (collection: TemplateCollection) => void;
}

export const TemplateCollectionList: React.FC<TemplateCollectionListProps> = ({ onSelect }) => {
    const navigate = useNavigate();
    const tableRef = useRef<any>(null);
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
