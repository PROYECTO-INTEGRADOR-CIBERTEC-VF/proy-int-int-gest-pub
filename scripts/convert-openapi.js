const fs = require('fs');
const openapi = JSON.parse(fs.readFileSync('openapi/saip-openapi.json', 'utf8'));

const collection = {
    info: {
        name: openapi.info.title || "SAIP API Collection",
        description: openapi.info.description || "Coleccion auto-generada para Postman.",
        schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    item: []
};

// Group by tags
const tagsMap = {};
openapi.tags.forEach(t => {
    tagsMap[t.name] = {
        name: t.name,
        description: t.description,
        item: []
    };
});

Object.keys(openapi.paths).forEach(path => {
    const pathObj = openapi.paths[path];
    const postmanUrl = {
        raw: "{{baseUrl}}" + path.replace(/\{(\w+)\}/g, ':$1'),
        host: ["{{baseUrl}}"],
        path: path.split('/').filter(p => p !== '').map(p => p.replace(/\{(\w+)\}/g, ':$1')),
        variable: []
    };

    const pathTokens = path.split('/').filter(p => p !== '');
    pathTokens.forEach(token => {
        if (token.startsWith('{') && token.endsWith('}')) {
            postmanUrl.variable.push({
                key: token.replace(/\{|\}/g, ''),
                value: ""
            });
        }
    });

    Object.keys(pathObj).forEach(method => {
        const op = pathObj[method];
        
        const request = {
            method: method.toUpperCase(),
            header: [
                { key: "Authorization", value: "Bearer {{token}}", type: "text" },
                { key: "Content-Type", value: "application/json", type: "text" }
            ],
            url: postmanUrl,
            description: op.description || op.summary || ""
        };

        if (op.requestBody && op.requestBody.content && op.requestBody.content['application/json']) {
            let schemaRef = op.requestBody.content['application/json'].schema.$ref;
            let bodyStr = "{}";
            if (schemaRef) {
                const schemaName = schemaRef.split('/').pop();
                const schema = openapi.components.schemas[schemaName];
                if (schema && schema.properties) {
                    const sample = {};
                    Object.keys(schema.properties).forEach(k => {
                        const t = schema.properties[k].type;
                        if (t === 'string') sample[k] = "string";
                        else if (t === 'integer') sample[k] = 0;
                        else if (t === 'boolean') sample[k] = false;
                        else sample[k] = null;
                    });
                    bodyStr = JSON.stringify(sample, null, 4);
                }
            }
            request.body = {
                mode: "raw",
                raw: bodyStr,
                options: { raw: { language: "json" } }
            };
        }

        const item = {
            name: op.summary || (method.toUpperCase() + " " + path),
            request: request,
            response: []
        };

        const tag = (op.tags && op.tags.length > 0) ? op.tags[0] : "General";
        if (!tagsMap[tag]) {
            tagsMap[tag] = { name: tag, item: [] };
        }
        tagsMap[tag].item.push(item);
    });
});

Object.values(tagsMap).forEach(folder => {
    if (folder.item.length > 0) {
        collection.item.push(folder);
    }
});

fs.writeFileSync('postman/SAIP-Collection.postman_collection.json', JSON.stringify(collection, null, 4));
console.log('Collection created successfully at postman/SAIP-Collection.postman_collection.json');
