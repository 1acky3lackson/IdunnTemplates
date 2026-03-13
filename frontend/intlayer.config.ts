import { type IntlayerConfig, Locales } from 'intlayer';

const config: IntlayerConfig = {
  internationalization: {
    locales: [
      Locales.CHINESE, // 'zh-CN'
      Locales.CHINESE_TRADITIONAL, // 'zh-TW'
      Locales.ENGLISH,
    ]
  },
  compiler: {
    enabled: true,
    
  }
};

export default config;
