import type { Route } from "./+types/home";
import { Welcome } from "../welcome/welcome";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "IdunnTemplates" },
    { name: "description", content: "A Dynamic Updating Template Library for Minecraft" },
  ];
}

export default function Home() {
  return <>
    <Welcome />
  </>;
}
