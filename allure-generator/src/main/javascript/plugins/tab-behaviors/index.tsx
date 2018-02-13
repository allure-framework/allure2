import * as React from "react";
import TestResultTreeContainer from "../../components/TestResultTreeContainer";

window.allure.api.addReportTab({
    id: "behaviors",
    name: "Behaviors",
    icon: "fa fa-newspaper-o",
    render: () => <TestResultTreeContainer name={"Behaviors"} route={"behaviors"} dataPath={"data/behaviors.json"}/>
});
