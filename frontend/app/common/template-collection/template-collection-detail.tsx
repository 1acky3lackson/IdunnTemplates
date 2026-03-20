import React, { useRef, useState, useEffect } from "react";
import { GenericCrudTable, type ColumnSchema, type TableActions } from "~/common/generic-crud-table/generic-crud-table";
import { IDUNN_API } from "~/api";
import type { Template } from "~/api/generated/model/template";
import type { TemplateCollection } from "~/api/generated/model/template-collection";
import { TemplateCard } from "~/common/template/template-card";
import { Button } from "~/components/ui/button";
import { Shuffle, Copy } from "lucide-react";
import { RandomTemplateDialog } from "./random-template-dialog";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "~/components/ui/dialog";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "~/components/ui/select";
import { useIntlayer } from "react-intlayer";

export interface TemplateCollectionDetailProps {
    collectionId: string;
}

export const TemplateCollectionDetail: React.FC<TemplateCollectionDetailProps> = ({ collectionId }) => {
    const tableRef = useRef<any>(null);
    const [collection, setCollection] = useState<TemplateCollection | null>(null);
    const [isAddOpen, setIsAddOpen] = useState(false);
    const [isRandomOpen, setIsRandomOpen] = useState(false);
    const [templateIdToAdd, setTemplateIdToAdd] = useState("");
    const [channelType, setChannelType] = useState<"right" | "left" | "other">("right");
    const [channelName, setChannelName] = useState("");
    const { columns, actions, dialogs, messages } = useIntlayer("template-collection");

    const actualChannel = channelType === "other" ? (channelName.trim() || 'custom') : channelType;

    const loadData = async (page: number, size: number, search: string, sort: string) => {
        const response = await IDUNN_API.apiV1CollectionsIdTemplatesGet(
            collectionId, 
            search as any || undefined, 
            String(page), 
            String(size), 
            sort as any || undefined
        );
        return response.data as any; // Cast to PageResponse
    };

    useEffect(() => {
        const fetchCollection = async () => {
            try {
                const res = await IDUNN_API.apiV1CollectionsGet(undefined, 0, 100);
                const colData = res.data as any;
                const found = colData.content?.find((c: TemplateCollection) => c.id.toString() === collectionId);
                if (found) {
                    setCollection(found);
                }
            } catch (err) {
                console.error("Failed to load collection stats", err);
            }
        };
        fetchCollection();
    }, [collectionId]);

    const schema: Record<string, ColumnSchema<Template>> = {
        name: { title: columns.name.value, filterable: true, sortable: true },
    };

    const searchFields = [
        { key: "name", label: columns.name.value, fuzzy: true },
        { key: "path", label: columns.path.value, fuzzy: true }
    ];

    const handleRemoveTemplate = async (template: Template, tableActions: TableActions) => {
        if (confirm(`${dialogs.confirmRemoveTemplate.value} (${template.name})`)) {
            try {
                await IDUNN_API.apiV1CollectionsIdTemplatesTemplateIdDelete(collectionId, template.id);
                toast.success(messages.removeTemplateSuccess.value);
                tableActions.refreshPage();
            } catch (err) {
                toast.error(messages.removeTemplateFailed.value);
            }
        }
    };

    const submitAddTemplate = async () => {
        if (!templateIdToAdd.trim()) return;
        try {
            await IDUNN_API.apiV1CollectionsIdTemplatesPost(collectionId, { templateId: templateIdToAdd });
            toast.success(messages.addTemplateSuccess.value);
            setIsAddOpen(false);
            setTemplateIdToAdd("");
            tableRef.current?.refreshPage();
        } catch (err) {
            toast.error(messages.addTemplateFailed.value);
        }
    };

    return (
        <div className="p-6">
            <div className="mb-6 flex flex-col gap-2">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight">
                            {collection ? collection.name : messages.loading}
                        </h1>
                        {collection?.description && (
                            <p className="text-muted-foreground">{collection.description}</p>
                        )}
                    </div>
                </div>
                {collection && (
                    <div className="mt-2 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-4 bg-card border rounded-xl shadow-sm">
                        <div className="flex items-center gap-3 w-full sm:w-auto">
                            <Label htmlFor="channelType" className="whitespace-nowrap font-medium text-muted-foreground">
                                {columns.bindChannel.value}
                            </Label>
                            <Select value={channelType} onValueChange={(v: "right" | "left" | "other") => setChannelType(v)}>
                                <SelectTrigger className="w-[140px] bg-background">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="right">{columns.bindRight.value}</SelectItem>
                                    <SelectItem value="left">{columns.bindLeft.value}</SelectItem>
                                    <SelectItem value="other">{columns.bindOther.value}</SelectItem>
                                </SelectContent>
                            </Select>
                            {channelType === "other" && (
                                <Input 
                                    id="channelName"
                                    value={channelName}
                                    onChange={(e) => setChannelName(e.target.value)}
                                    placeholder={columns.bindChannelPlaceholder.value}
                                    className="w-24 sm:w-32 bg-background focus-visible:ring-1"
                                />
                            )}
                        </div>
                        <div className="flex items-center gap-2 w-full sm:w-auto overflow-hidden">
                            <div className="flex-1 sm:flex-initial flex items-center overflow-x-auto rounded-md border bg-muted/50 px-3 py-2 scrollbar-none">
                                <code className="text-sm font-mono text-foreground whitespace-nowrap select-all">
                                    /idunn brush bind {actualChannel} collection {collectionId}
                                </code>
                            </div>
                            <Button
                                variant="outline"
                                size="icon"
                                className="shrink-0 hover:bg-primary hover:text-primary-foreground transition-colors"
                                onClick={() => {
                                    navigator.clipboard.writeText(`/idunn brush bind ${actualChannel} collection ${collectionId}`);
                                    toast.success(messages.commandCopied.value);
                                }}
                                title={actions.copyCommand.value}
                            >
                                <Copy className="w-4 h-4" />
                            </Button>
                        </div>
                    </div>
                )}
            </div>

            <GenericCrudTable<Template>
                ref={tableRef}
                uid={`collection-templates-${collectionId}`}
                list={loadData}
                schema={schema}
                searchFields={searchFields}
                getRowId={(row) => row.id}
                create={() => setIsAddOpen(true)}
                headerActions={
                    <Button 
                        onClick={() => setIsRandomOpen(true)}
                        className="bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 hover:opacity-90 transition-opacity text-white border-0 shadow-sm"
                    >
                        <Shuffle className="w-4 h-4 mr-2" />
                        {actions.random}
                    </Button>
                }
                pageSize={20}
                customRenderer={{
                    renderContainer: (nodes) => (
                        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 mt-6">
                            {nodes}
                        </div>
                    ),
                    renderElement: (row, childActions) => {
                        if (row === null || row === undefined) {
                            return null;
                        }
                        return (
                            <div className="relative group">
                                <TemplateCard template={row} className="h-full" />
                                <div className="absolute top-2 left-2 z-[60] opacity-0 group-hover:opacity-100 transition-opacity">
                                    <Button
                                        variant="destructive"
                                        size="sm"
                                        className="h-8 shadow-md"
                                        onClick={() => handleRemoveTemplate(row, childActions)}
                                    >
                                        {actions.remove}
                                    </Button>
                                </div>
                            </div>
                        )
                    }
                }}
            />

            {/* Add Template Dialog */}
            <Dialog open={isAddOpen} onOpenChange={setIsAddOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{dialogs.addTemplateTitle}</DialogTitle>
                        <DialogDescription>{dialogs.addTemplateDesc}</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="templateId" className="text-right">{columns.uuid}</Label>
                            <Input 
                                id="templateId" 
                                value={templateIdToAdd} 
                                onChange={(e) => setTemplateIdToAdd(e.target.value)} 
                                placeholder={dialogs.uuidPlaceholder.value}
                                className="col-span-3" 
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsAddOpen(false)}>{actions.cancel}</Button>
                        <Button onClick={submitAddTemplate}>{actions.addTemplate}</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <RandomTemplateDialog 
                collectionId={collectionId} 
                open={isRandomOpen} 
                onOpenChange={setIsRandomOpen} 
            />
        </div>
    );
};
