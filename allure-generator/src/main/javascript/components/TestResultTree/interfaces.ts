export interface AllureNodeContext {
    key?: string,
    value?: string
}

export interface AllureTreeLeaf {
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

export interface AllureTreeGroup {
    uid: string,
    name: string,
    context: AllureNodeContext,
    groups?: Array<AllureTreeGroup>,
    leafs?: Array<AllureTreeLeaf>
}

