import type { Route } from "./+types/templates-detail";

export function meta({ params }: Route.MetaArgs) {
  return [
    { title: `Template ${params.uuid}` },
  ];
}

export function loader({ params }: Route.LoaderArgs) {
  return { uuid: params.uuid };
}

export default function TemplatesDetail({ loaderData }: Route.ComponentProps) {
  const { uuid } = loaderData;
  
  return (
    <div className="container mx-auto py-10">
      <h1 className="text-2xl font-bold">Template Detail</h1>
      <p className="mt-4">
        Template UUID: <code className="bg-muted px-1 py-0.5 rounded">{uuid}</code>
      </p>
    </div>
  );
}
