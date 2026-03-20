import React, { useRef, useState } from "react";
import { GenericCrudTable, type ColumnSchema, type TableActions } from "~/common/generic-crud-table/generic-crud-table";
import { IDUNN_API } from "~/api";
import type { ApiV1ServersGet200ResponseInner } from "~/api/generated/model/api-v1-servers-get200-response-inner";
import { Button } from "~/components/ui/button";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "~/components/ui/dialog";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "~/components/ui/tooltip";
import { Copy } from "lucide-react";
import { useIntlayer } from "react-intlayer";

export const TrustedServerList: React.FC = () => {
    const tableRef = useRef<any>(null);
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const { title, subtitle, columns, actions, dialogs, messages } = useIntlayer("trusted-server");
    
    // Form state
    const [name, setName] = useState("");
    const [remarks, setRemarks] = useState("");

    const loadData = async (page: number, size: number, search: string, sort: string) => {
        const response = await IDUNN_API.apiV1ServersGet();
        let data = response.data || [];
        
        // Client-side search implementation
        if (search) {
            const searchTerms = search.split(',').filter(Boolean);
            searchTerms.forEach(term => {
                const [keyVal, val] = term.split(':');
                const key = keyVal.replace('~', '');
                data = data.filter(item => {
                   const itemVal = String(item[key as keyof typeof item] || '').toLowerCase();
                   return itemVal.includes(val.toLowerCase());
                });
            });
        }
        
        // Client-side sort implementation
        if (sort) {
            const [field, dir] = sort.split(',');
            data.sort((a, b) => {
                const valA = a[field as keyof typeof a] || '';
                const valB = b[field as keyof typeof b] || '';
                if (valA < valB) return dir === 'asc' ? -1 : 1;
                if (valA > valB) return dir === 'asc' ? 1 : -1;
                return 0;
            });
        } else {
            // default sort by createdAt desc if available
            data.sort((a, b) => {
                const valA = a.createdAt || 0;
                const valB = b.createdAt || 0;
                return valB - valA;
            });
        }
        
        // Client-side pagination
        const totalElements = data.length;
        const offset = page * size;
        const pagedData = data.slice(offset, offset + size);
        
        return {
            content: pagedData,
            totalElements,
            number: page,
            last: offset + size >= totalElements
        };
    };

    const schema: Record<string, ColumnSchema<ApiV1ServersGet200ResponseInner>> = {
        name: {
            title: columns.name.value,
            sortable: true,
            filterable: true,
        },
        remarks: {
            title: columns.description.value,
            sortable: true,
            render: (val) => {
                if (!val) return <span className="text-muted-foreground">-</span>;
                if (val.length <= 10) return <span className="text-muted-foreground">{val}</span>;
                return (
                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <span className="text-muted-foreground cursor-help border-b border-dotted border-muted-foreground/50">{val.slice(0, 10)}...</span>
                            </TooltipTrigger>
                            <TooltipContent className="max-w-[300px] break-words">
                                <p>{val}</p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                );
            }
        },
        token: {
            title: columns.token.value,
            render: (val) => {
                if (!val) return <span className="text-muted-foreground">-</span>;
                return (
                    <div className="flex items-center gap-2">
                        <code className="bg-muted px-2 py-1 rounded text-xs select-all text-muted-foreground">{val.slice(0, 8)}********</code>
                        <Button
                            variant="ghost"
                            size="icon"
                            className="h-6 w-6"
                            onClick={() => {
                                navigator.clipboard.writeText(val);
                                toast.success(messages.tokenCopied.value);
                            }}
                            title={actions.copyFullToken.value}
                        >
                            <Copy className="h-3.5 w-3.5 text-muted-foreground hover:text-primary transition-colors" />
                        </Button>
                    </div>
                );
            }
        },
        createdByUsername: {
            title: columns.creator.value,
            sortable: true,
            filterable: true,
        },
        createdAt: {
            title: columns.createdAt.value,
            sortable: true,
            render: (val) => val ? new Date(val).toLocaleDateString() : '-'
        }
    };

    const searchFields = [
        { key: "name", label: columns.name.value, fuzzy: true },
        { key: "remarks", label: columns.description.value, fuzzy: true }
    ];

    const handleCreate = () => {
        setName("");
        setRemarks("");
        setIsCreateOpen(true);
    };

    const handleDelete = async (row: ApiV1ServersGet200ResponseInner, TableActions: TableActions) => {
        if (!row.id) return;
        if (confirm(`${dialogs.confirmDelete.value} "${row.name}"?`)) {
            try {
                await IDUNN_API.apiV1ServersIdDelete(row.id.toString());
                toast.success(messages.deleteSuccess.value);
                TableActions.refreshPage();
            } catch (err) {
                toast.error(messages.deleteFailed.value);
            }
        }
    };

    const submitCreate = async () => {
        try {
            await IDUNN_API.apiV1ServersPost({
                name,
                remarks
            });
            toast.success(messages.addSuccess.value);
            setIsCreateOpen(false);
            tableRef.current?.refreshPage();
        } catch (err) {
            toast.error(messages.addFailed.value);
        }
    };

    return (
        <div className="p-6">
            <div className="mb-6 flex flex-col gap-2">
                <h1 className="text-2xl font-bold tracking-tight text-foreground">{title}</h1>
                <p className="text-muted-foreground">{subtitle}</p>
            </div>

            <GenericCrudTable<ApiV1ServersGet200ResponseInner>
                ref={tableRef}
                uid="servers"
                list={loadData}
                schema={schema}
                searchFields={searchFields}
                getRowId={(row) => row.id || Math.random().toString()}
                create={handleCreate}
                deleteAction={handleDelete}
                pageSize={20}
            />

            {/* Create Dialog */}
            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{dialogs.addServerTitle}</DialogTitle>
                        <DialogDescription>{dialogs.addServerDesc}</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="name" className="text-right">{columns.name}</Label>
                            <Input id="name" value={name} onChange={(e) => setName(e.target.value)} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="remarks" className="text-right">{columns.description}</Label>
                            <Input id="remarks" value={remarks} onChange={(e) => setRemarks(e.target.value)} className="col-span-3" />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsCreateOpen(false)}>{actions.cancel}</Button>
                        <Button onClick={submitCreate}>{actions.add}</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};
