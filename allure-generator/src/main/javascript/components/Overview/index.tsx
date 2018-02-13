import "./styles.scss";
import * as React from "react";
import * as ReactGridLayout from "react-grid-layout";
import Widget from "../Widget";

interface OverviewProps {

}

interface OverviewState {
}

export default class Overview extends React.Component<OverviewProps, OverviewState> {

    render() {
        const sad = "sad ".repeat(200);
        const ResizableGridLayout = ReactGridLayout.WidthProvider(ReactGridLayout);
        return (
            <div className={"Overview"}>
                <div className={"Overview__content"}>
                    <ResizableGridLayout cols={12}
                                         rowHeight={50}
                                         verticalCompact={true}
                                         autoSize={true} useCSSTransforms={true}
                                         draggableHandle={".Widget__draggable-handle"}
                    >
                        <div key="a" data-grid={{x: 0, y: 0, w: 8, h: 5, isResizable: true}}>
                            <Widget name={"Super image"}><img src="https://files.gitter.im/allure-framework/allure-ru/sHEi/image.png"/></Widget>
                        </div>
                        <div key="b" data-grid={{x: 0, y: 0, w: 4, h: 5, isResizable: true}}>
                            <Widget name={"Sad strings"}>{sad}</Widget>
                        </div>
                        {/*<Widget key={1}>A</Widget>*/}
                        {/*<Widget key={2}>B</Widget>*/}
                        {/*<Widget key={3}>C</Widget>*/}
                        {/*<Widget key={4}>D</Widget>*/}
                        {/*<Widget key={5}>E</Widget>*/}
                    </ResizableGridLayout>
                </div>
            </div>
        );
    }
}