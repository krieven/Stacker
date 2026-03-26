
interface RouterConfig {
    title?: string
    description?: string
    nameSpaceMap: {
        [key: string]: NameSpaceConfig
    }
    mainFlow: string
}

interface NameSpaceConfig {
    title?: string
    description?: string
    flowConfigMap: { [key: string]: FlowConfig }
}

interface FlowConfig {
    name?: string
    title?: string
    description?: string
    access?: AccessType
    address: string
    mapping: { [key: string]: string }
    properties: { [key: string]: string }
}

declare enum AccessType {
    IMPORT, EXPORT
}
