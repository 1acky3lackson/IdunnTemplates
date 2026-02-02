module.exports = {
    idunnApi: {
        input: 'http://127.0.0.1:4523/export/openapi/5?version=2.0',
        output: {
            target: './src/api/generated/api.ts',
            client: 'axios', // 也可以选 'react-query'
            mode: 'split',
            override: {
                mutator: {
                    path: './app/lib/axios.ts', // 指向你刚才写的那个 axios 配置文件
                    name: 'apiClient',
                },
            },
        },
    },
};