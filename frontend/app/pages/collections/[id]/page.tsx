import { useParams } from "react-router";
import { TemplateCollectionDetail } from "~/common/template-collection";

export default function CollectionDetailPage() {
    const { id } = useParams<{ id: string }>();
    if (!id) return null;
    return <TemplateCollectionDetail collectionId={id} />;
}
