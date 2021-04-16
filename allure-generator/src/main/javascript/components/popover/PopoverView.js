import "./styles.scss";
import { className } from "../../decorators";
import TooltipView from "../tooltip/TooltipView";

@className("popover")
class PopoverView extends TooltipView {}

export default PopoverView;
