export interface AllureNodeContext {
    key?: string,
    value?: string
}

//TODO split groups and leafs
export interface AllureTreeNode {
    uid: string,
    context?: AllureNodeContext,
    children?: Array<AllureTreeNode>

    id: number,
    name: string,
    parentUid: string,
    status: string,
    start: number,
    stop: number,
    duration: number
    flaky: boolean,
    parameters: Array<string>
}

