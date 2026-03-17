import { Locales, t, type Dictionary } from "intlayer";

const directoryContent = {
  key: "directory",
  content: {
    folder: {
      all: t({
        [Locales.ENGLISH]: "All Folders",
        [Locales.CHINESE]: "所有文件夹",
        [Locales.CHINESE_TRADITIONAL]: "所有資料夾",
      }),
    },
  },
} satisfies Dictionary;

export default directoryContent;
