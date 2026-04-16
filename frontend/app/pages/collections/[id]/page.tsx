import { useParams } from "react-router";
import { TemplateCollectionDetail } from "~/common/template-collection";

export default function CollectionDetailPage() {
    const { id } = useParams<{ id: string }>();
    if (!id) return null;
    document.title = `模板合集详情 ${id}`;
    return <TemplateCollectionDetail collectionId={id} />;
}
