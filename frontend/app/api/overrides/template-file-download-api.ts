import apiClient from "@/lib/axios";
import { getBackendBaseUrl } from "..";
import type { Template } from "../generated";

export function getSchemLinkForTemplate(template: Template): string {
    return `${getBackendBaseUrl()}/api/v1/templates/${template.id}/download`;
}

export function getSchemLinkForTemplateVersion(
    templateId: string,
    version?: string | null,
): string {
    const baseUrl = `${getBackendBaseUrl()}/api/v1/templates/${templateId}/download`;
    if (!version) {
        return baseUrl;
    }
    return `${baseUrl}?version=${encodeURIComponent(version)}`;
}

function getFilenameFromDisposition(disposition?: string): string | null {
    if (!disposition) return null;

    const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
        return decodeURIComponent(utf8Match[1]);
    }

    const basicMatch = disposition.match(/filename="?([^"]+)"?/i);
    return basicMatch?.[1] ?? null;
}

export async function downloadTemplateFile(
    templateId: string,
    options?: {
        version?: string | null;
        filename?: string;
    },
) {
    const response = await apiClient.get<Blob>(
        getSchemLinkForTemplateVersion(templateId, options?.version),
        {
            responseType: "blob",
        },
    );

    const filename =
        options?.filename ||
        getFilenameFromDisposition(response.headers["content-disposition"] as string | undefined) ||
        `${templateId}${options?.version ? `-${options.version}` : ""}.schem`;

    const blobUrl = URL.createObjectURL(response.data);
    const anchor = document.createElement("a");
    anchor.href = blobUrl;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(blobUrl);
}
