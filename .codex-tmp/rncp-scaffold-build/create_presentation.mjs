import fs from "node:fs/promises";
import { Presentation, PresentationFile, layers, text } from "@oai/artifact-tool";

const root = "C:/new_music_app/music-wall";
const output = `${root}/RNCP/03_Soutenance/presentation.pptx`;
const preview = `${root}/.codex-tmp/rncp-scaffold-build/presentation-preview.png`;
const layout = `${root}/.codex-tmp/rncp-scaffold-build/presentation-layout.json`;

const presentation = Presentation.create({
  slideSize: { width: 1280, height: 720 },
});

const slide = presentation.slides.add();
slide.compose(
  layers({ name: "rncp-title-slide", width: "fill", height: "fill" }, [
    text(["SOUTENANCE RNCP"], {
      name: "Eyebrow",
      position: { left: 52, top: 44 },
      width: 600,
      height: 56,
      style: {
        fontSize: "26px",
        typeface: "Helvetica Neue",
        color: "#2F8F9D",
        alignment: "left",
        autoFit: "none",
        insets: { top: 0, right: 0, bottom: 0, left: 0 },
      },
    }),
    text(["Music Wall"], {
      name: "Title",
      position: { left: 52, top: 184 },
      width: 1000,
      height: 250,
      style: {
        fontSize: "80px",
        typeface: "Helvetica Neue",
        color: "#123E48",
        alignment: "left",
        verticalAlignment: "bottom",
        autoFit: "none",
        insets: { top: 0, right: 0, bottom: 0, left: 0 },
      },
    }),
    text(["Support de présentation — à compléter"], {
      name: "Subtitle",
      position: { left: 52, top: 500 },
      width: 760,
      height: 90,
      style: {
        fontSize: "28px",
        typeface: "Helvetica Neue",
        color: "#F56B61",
        alignment: "left",
        autoFit: "none",
        insets: { top: 0, right: 0, bottom: 0, left: 0 },
      },
    }),
  ]),
  { frame: { left: 0, top: 0, width: 1280, height: 720 }, baseUnit: 1 },
);

const png = await presentation.export({ slide, format: "png", scale: 1 });
await fs.writeFile(preview, new Uint8Array(await png.arrayBuffer()));
const layoutBlob = await slide.export({ format: "layout" });
await fs.writeFile(layout, await layoutBlob.text());
const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(output);
