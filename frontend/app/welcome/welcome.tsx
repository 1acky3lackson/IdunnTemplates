import TemplateBrowser from "~/common/template/template-browser";

export function Welcome() {
  return (
    // <main className="flex items-center justify-center pt-16 pb-4">
    <>
      {/* Grid, 2 cols, max-w-7xl */}
      <div className="w-ful md:px-0 mx-auto my-4">
        <TemplateBrowser />
      </div>
    </>
    // </main>
  );
}
