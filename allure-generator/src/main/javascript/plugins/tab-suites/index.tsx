import * as React from "react";
import TestResultTreeContainer from "../../components/TestResultTreeContainer";

window.allure.api.addReportTab({
    id: "suites",
    name: "Suites",
    icon: "fa fa-suitcase",
    render: () => <TestResultTreeContainer name={"Suites"} route={"suites"} dataPath={"data/suites.json"}/>
});
