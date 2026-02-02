import type { Route } from "./+types/templates";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Templates" },
  ];
}

export default function Templates() {
  return (
    <div className="container mx-auto py-10">
      <h1 className="text-2xl font-bold">Templates</h1>
      <p>Select a template to view details.</p>
    </div>
  );
}
