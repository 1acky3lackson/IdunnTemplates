import React, { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "~/components/ui/dialog";
import { Button } from "~/components/ui/button";
import { Loader2, Shuffle } from "lucide-react";
import { IDUNN_API } from "~/api";
import type { Template } from "~/api/generated/model/template";
import { TemplateCard } from "~/common/template/template-card";
import { toast } from "sonner";
import { useIntlayer } from "react-intlayer";

export interface RandomTemplateDialogProps {
  collectionId?: string | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const RandomTemplateDialog: React.FC<RandomTemplateDialogProps> = ({ collectionId, open, onOpenChange }) => {
  const [template, setTemplate] = useState<Template | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { dialogs, actions, messages } = useIntlayer("template-collection");

  const fetchRandomTemplate = async () => {
    if (!collectionId) return;
    setIsLoading(true);
    setTemplate(null);
    try {
      const response = await IDUNN_API.apiV1CollectionsIdRandomGet(collectionId);
      console.log("resp", response);
      setTemplate(response.data as any);
    } catch (e: any) {
      // HTTP 404 typically means the collection is empty.
      if (e?.response?.status !== 404) {
        toast.error(messages.randomTemplateFailed.value);
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (open && collectionId) {
      fetchRandomTemplate();
    } else if (!open) {
      // Clear template state when closed to ensure a fresh state on next open
      // We use a small timeout to avoid visual flicker while dialog closes
      setTimeout(() => setTemplate(null), 300);
    }
  }, [open, collectionId]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md md:max-w-xl transition-all duration-300 shadow-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 to-pink-500">
            <Shuffle className="w-5 h-5 text-indigo-500" />
            {dialogs.randomTemplateTitle}
          </DialogTitle>
          <DialogDescription>
            {dialogs.randomTemplateDesc}
          </DialogDescription>
        </DialogHeader>

        <div className="py-2 flex flex-col items-center justify-center min-h-[350px] overflow-hidden">
          {isLoading && (
            <div className="flex flex-col items-center animate-pulse duration-700">
              <div className="relative">
                <div className="absolute inset-0 rounded-full blur-xl bg-gradient-to-r from-indigo-500 to-pink-500 opacity-20 animate-spin-slow"></div>
                <Loader2 className="w-10 h-10 animate-spin text-indigo-500 relative z-10" />
              </div>
              <p className="text-sm text-muted-foreground mt-4 tracking-wider">Picking...</p>
            </div>
          )}
          {!isLoading && template && (
            <div className="w-full h-full animate-in fade-in zoom-in-95 duration-500">
              <TemplateCard template={template} className="w-full h-[320px] shadow-lg border-2 border-indigo-500/10 hover:border-indigo-500/20 transition-all rounded-xl overflow-hidden" />
            </div>
          )}
          {!isLoading && !template && (
            <div className="text-muted-foreground text-sm flex flex-col items-center gap-2">
              <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-2">
                <Shuffle className="w-8 h-8 text-slate-300 dark:text-slate-600" />
              </div>
              {messages.noTemplateFound}
            </div>
          )}
        </div>

        <DialogFooter className="flex flex-col sm:flex-row gap-3 sm:justify-between w-full mt-4">
          <Button variant="ghost" onClick={() => onOpenChange(false)}>
            {actions.cancel}
          </Button>
          <Button
            onClick={fetchRandomTemplate}
            disabled={isLoading}
            size="lg"
            className="group gap-2 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 hover:opacity-90 transition-all text-white border-0 shadow-[0_0_20px_rgba(99,102,241,0.3)] hover:shadow-[0_0_30px_rgba(99,102,241,0.5)] active:scale-95 rounded-full px-8"
          >
            <Shuffle className={`w-4 h-4 ${isLoading ? 'animate-spin' : 'group-hover:rotate-12 transition-transform'}`} />
            {actions.oneMore}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
