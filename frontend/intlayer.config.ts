import { type IntlayerConfig, Locales } from 'intlayer';

const config: IntlayerConfig = {
  internationalization: {
    locales: [
      Locales.ENGLISH,
      Locales.CHINESE, // 'zh-CN'
      Locales.CHINESE_TRADITIONAL, // 'zh-TW'
    ],
    defaultLocale: Locales.CHINESE,
  },
};

export default config;
