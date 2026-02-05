import { getBackendBaseUrl, IDUNN_API } from "..";
import type { Template } from "../generated";

export function getSchemLinkForTemplate(template: Template): string {
    return `${getBackendBaseUrl}/api/v1/templates/${template.id}/download`;
}