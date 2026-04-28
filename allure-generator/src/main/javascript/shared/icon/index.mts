import "./styles.scss";
import { createFragmentFromHtml } from "../dom.mts";
import { escapeAttr, escapeHtml } from "../html.mts";

type IconDefinition = {
  body: string;
  fill?: "currentColor" | "none";
  viewBox: string;
};

const defineIcons = <T extends Record<string, IconDefinition>>(icons: T) => icons;

const ICONS = defineIcons({
  lineAlertsFixed: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path vector-effect="non-scaling-stroke" d="M8 12L11 15L16 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineAlertsMalfunctioned: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path vector-effect="non-scaling-stroke" d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineAlertsRegressed: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path vector-effect="non-scaling-stroke" d="M9 11L15 17M15 11L9 17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineArrowsChevronRight: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineArrowsRefreshCcw1: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M8.54636 19.7675C10.9455 20.8319 13.803 20.7741 16.2499 19.3613C20.3154 17.0141 21.7084 11.8156 19.3612 7.75008L19.1112 7.31706M4.63826 16.2502C2.29105 12.1847 3.68399 6.98619 7.74948 4.63898C10.1965 3.22621 13.0539 3.16841 15.4531 4.23277M2.49316 16.3338L5.22521 17.0659L5.95727 14.3338M18.0424 9.6659L18.7744 6.93385L21.5065 7.6659" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineArrowsSortLineAsc: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M4.5 18H19.5M4.5 12H15.5M4.5 6H12.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>',
  },
  lineArrowsSortLineDesc: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M4.5 6H19.5M4.5 12C6.45262 12 13.5474 12 15.5 12M4.5 18L12.5 18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>',
  },
  lineArrowsSwitchVertical1: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M7 4V20M7 20L3 16M7 20L11 16M17 20V4M17 4L13 8M17 4L21 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineChartsBarChartSquare: {
    viewBox: "0 0 24 24",
    body: '<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity=".7" stroke-width="1.5" d="M8 13v4m8-6v6M12 7v10m-4 4h11l2-2V5l-2-2H5L3 5v14l2 2h3Z"/>',
  },
  lineChartsTimeline: {
    viewBox: "0 0 24 24",
    body: '<path d="M11 8H7" stroke="currentColor" stroke-opacity="0.7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M13 16H7" stroke="currentColor" stroke-opacity="0.7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M17 12H7" stroke="currentColor" stroke-opacity="0.7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M5 21L3 19V5L5 3H19L21 5V19L19 21H5Z" stroke="currentColor" stroke-opacity="0.7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineDevBug2: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M2.5 7L5 9.5M2.5 20.5L5.5 17.5M1.5 13.5H4.6213M19 13.5H22.5M19 9.5L21.5 7M18.5 17.5L21.5 20.5M8 7.5V6.5C8 4.29086 9.7908 2.5 12 2.5C14.2092 2.5 16 4.29086 16 6.5V7.5M12 21.5C8.13401 21.5 5 18.366 5 14.5V11.4999C5 10.3829 5 9.82428 5.15712 9.37516C5.43856 8.57093 6.07093 7.93856 6.87522 7.65712C7.32427 7.5 7.88284 7.5 9 7.5H15.0001C16.1171 7.5 16.6757 7.5 17.1248 7.65712C17.9291 7.93856 18.5614 8.57093 18.8429 9.37516C19 9.82428 19 10.3829 19 11.4999V14.5C19 18.366 15.866 21.5 12 21.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineDevCodeSquare: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M14.5 15L17.5 12L14.5 9M9.5 9L6.5 12L9.5 15M7.8 21H16.2C17.8802 21 18.7202 21 19.362 20.673C19.9265 20.3854 20.3854 19.9265 20.673 19.362C21 18.7202 21 17.8802 21 16.2V7.8C21 6.11984 21 5.27976 20.673 4.63803C20.3854 4.07354 19.9265 3.6146 19.362 3.32698C18.7202 3 17.8802 3 16.2 3H7.8C6.11984 3 5.27976 3 4.63803 3.32698C4.07354 3.6146 3.6146 4.07354 3.32698 4.63803C3 5.27976 3 6.11984 3 7.8V16.2C3 17.8802 3 18.7202 3.32698 19.362C3.6146 19.9265 4.07354 20.3854 4.63803 20.673C5.27976 21 6.11984 21 7.8 21Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineDevDataflow3: {
    viewBox: "0 0 24 24",
    body: '<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity=".7" stroke-width="1.5" d="M12 4v14l2 2h3m0 0a2 2 0 1 0 4 0 2 2 0 0 0-4 0ZM7 4h10M7 4a2 2 0 1 1-4 0 2 2 0 0 1 4 0Zm10 0a2 2 0 1 0 4 0 2 2 0 0 0-4 0Zm-5 8h5m0 0a2 2 0 1 0 4 0 2 2 0 0 0-4 0Z"/>',
  },
  lineFilesClipboardCheck: {
    viewBox: "0 0 24 24",
    body: '<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity=".7" stroke-width="1.5" d="M16 4h2l2 2v14l-2 2H6l-2-2V6l2-2h2m1 11 2 2 5-4m-6-7h5l1-1V4v0-1l-1-1H9L8 3v2l1 1h1Z"/>',
  },
  lineFilesFile2: {
    viewBox: "0 0 24 24",
    body: '<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity=".7" stroke-width="1.5" d="M14 11H8m2 4H8m8-8H8m12 0v13l-2 2H6l-2-2V4l2-2h12l2 2v3Z"/>',
  },
  lineFilesFileAttachment2: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M20 7V6.8C20 5.11984 20 4.27976 19.673 3.63803C19.3854 3.07354 18.9265 2.6146 18.362 2.32698C17.7202 2 16.8802 2 15.2 2H8.8C7.11984 2 6.27976 2 5.63803 2.32698C5.07354 2.6146 4.6146 3.07354 4.32698 3.63803C4 4.27976 4 5.11984 4 6.8V17.2C4 18.8802 4 19.7202 4.32698 20.362C4.6146 20.9265 5.07354 21.3854 5.63803 21.673C6.27976 22 7.11984 22 8.8 22H12.5M12.5 11H8M11.5 15H8M16 7H8M18 18V12.5C18 11.6716 18.6716 11 19.5 11C20.3284 11 21 11.6716 21 12.5V18C21 19.6569 19.6569 21 18 21C16.3431 21 15 19.6569 15 18V14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineFilesFolder: {
    viewBox: "0 0 24 24",
    body: '<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity=".7" stroke-width="1.5" d="m13 7-1-2-1-1-1-1H3L2 4v3m0 0h18l2 2v10l-2 2H4l-2-2V7Z"/>',
  },
  lineGeneralChecklist3: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M13 5H21M13 9H18M13 15H21M13 19H18M3 17.012L4.99133 19L9 15M4 4H8C8.55228 4 9 4.44772 9 5V9C9 9.55228 8.55228 10 8 10H4C3.44772 10 3 9.55228 3 9V5C3 4.44772 3.44772 4 4 4Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralCopy3: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M5 15C4.06812 15 3.60218 15 3.23463 14.8478C2.74458 14.6448 2.35523 14.2554 2.15224 13.7654C2 13.3978 2 12.9319 2 12V5.2C2 4.0799 2 3.51984 2.21799 3.09202C2.40973 2.71569 2.71569 2.40973 3.09202 2.21799C3.51984 2 4.0799 2 5.2 2H12C12.9319 2 13.3978 2 13.7654 2.15224C14.2554 2.35523 14.6448 2.74458 14.8478 3.23463C15 3.60218 15 4.06812 15 5M12.2 22H18.8C19.9201 22 20.4802 22 20.908 21.782C21.2843 21.5903 21.5903 21.2843 21.782 20.908C22 20.4802 22 19.9201 22 18.8V12.2C22 11.0799 22 10.5198 21.782 10.092C21.5903 9.71569 21.2843 9.40973 20.908 9.21799C20.4802 9 19.9201 9 18.8 9H12.2C11.0799 9 10.5198 9 10.092 9.21799C9.71569 9.40973 9.40973 9.71569 9.21799 10.092C9 10.5198 9 11.0799 9 12.2V18.8C9 19.9201 9 20.4802 9.21799 20.908C9.40973 21.2843 9.71569 21.5903 10.092 21.782C10.5198 22 11.0799 22 12.2 22Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralDownloadCloud: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M4 16.2422C2.79401 15.435 2 14.0602 2 12.5C2 10.1564 3.79151 8.23129 6.07974 8.01937C6.54781 5.17213 9.02024 3 12 3C14.9798 3 17.4522 5.17213 17.9203 8.01937C20.2085 8.23129 22 10.1564 22 12.5C22 14.0602 21.206 15.435 20 16.2422M8 17L12 21M12 21L16 17M12 21V12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralHomeLine: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M8 17H16M11.0177 2.764L4.23539 8.03912C3.78202 8.39175 3.55534 8.56806 3.39203 8.78886C3.24737 8.98444 3.1396 9.20478 3.07403 9.43905C3 9.70352 3 9.9907 3 10.5651V17.8C3 18.9201 3 19.4801 3.21799 19.908C3.40973 20.2843 3.71569 20.5903 4.09202 20.782C4.51984 21 5.07989 21 6.2 21H17.8C18.9201 21 19.4802 21 19.908 20.782C20.2843 20.5903 20.5903 20.2843 20.782 19.908C21 19.4801 21 18.9201 21 17.8V10.5651C21 9.9907 21 9.70352 20.926 9.43905C20.8604 9.20478 20.7526 8.98444 20.608 8.78886C20.4447 8.56806 20.218 8.39175 19.7646 8.03913L12.9823 2.764C12.631 2.49075 12.4553 2.35412 12.2613 2.3016C12.0902 2.25526 11.9098 2.25526 11.7387 2.3016C11.5447 2.35412 11.369 2.49075 11.0177 2.764Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralInfoCircle: {
    viewBox: "0 0 17 16",
    body: '<path d="M8.33268 10.6663V7.99967M8.33268 5.33301H8.33935M14.9993 7.99967C14.9993 11.6816 12.0146 14.6663 8.33268 14.6663C4.65078 14.6663 1.66602 11.6816 1.66602 7.99967C1.66602 4.31778 4.65078 1.33301 8.33268 1.33301C12.0146 1.33301 14.9993 4.31778 14.9993 7.99967Z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralLink1: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M10.0002 13C10.4297 13.5741 10.9776 14.0491 11.6067 14.3929C12.2359 14.7367 12.9317 14.9411 13.6468 14.9923C14.362 15.0435 15.0798 14.9403 15.7515 14.6897C16.4233 14.4392 17.0333 14.047 17.5402 13.54L20.5402 10.54C21.451 9.59695 21.955 8.33394 21.9436 7.02296C21.9322 5.71198 21.4063 4.45791 20.4793 3.53087C19.5523 2.60383 18.2982 2.07799 16.9872 2.0666C15.6762 2.0552 14.4132 2.55918 13.4702 3.46997L11.7502 5.17997M14.0002 11C13.5707 10.4258 13.0228 9.95078 12.3936 9.60703C11.7645 9.26327 11.0687 9.05885 10.3535 9.00763C9.63841 8.95641 8.92061 9.0596 8.24885 9.31018C7.5771 9.56077 6.96709 9.9529 6.4602 10.46L3.4602 13.46C2.54941 14.403 2.04544 15.666 2.05683 16.977C2.06822 18.288 2.59407 19.542 3.52111 20.4691C4.44815 21.3961 5.70221 21.9219 7.01319 21.9333C8.32418 21.9447 9.58719 21.4408 10.5302 20.53L12.2402 18.82" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralLinkExternal: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M21 9L21 3M21 3H15M21 3L13 11M10 5H7.8C6.11984 5 5.27976 5 4.63803 5.32698C4.07354 5.6146 3.6146 6.07354 3.32698 6.63803C3 7.27976 3 8.11984 3 9.8V16.2C3 17.8802 3 18.7202 3.32698 19.362C3.6146 19.9265 4.07354 20.3854 4.63803 20.673C5.27976 21 6.11984 21 7.8 21H14.2C15.8802 21 16.7202 21 17.362 20.673C17.9265 20.3854 18.3854 19.9265 18.673 19.362C19 18.7202 19 17.8802 19 16.2V14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineGeneralXClose: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineHelpersFlag: {
    viewBox: "0 0 16 16",
    body: '<path d="M2.667 10s.667-.667 2.667-.667c2 0 3.333 1.333 5.333 1.333S13.334 10 13.334 10V2s-.667.666-2.667.666c-2 0-3.333-1.333-5.333-1.333S2.667 2 2.667 2v12.666" stroke="currentColor" stroke-opacity=".72" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineHelpersPlayCircle: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path vector-effect="non-scaling-stroke" d="M9.5 8.96533C9.5 8.48805 9.5 8.24941 9.59974 8.11618C9.68666 8.00007 9.81971 7.92744 9.96438 7.9171C10.1304 7.90525 10.3311 8.03429 10.7326 8.29239L15.4532 11.3271C15.8016 11.551 15.9758 11.663 16.0359 11.8054C16.0885 11.9298 16.0885 12.0702 16.0359 12.1946C15.9758 12.337 15.8016 12.449 15.4532 12.6729L10.7326 15.7076C10.3311 15.9657 10.1304 16.0948 9.96438 16.0829C9.81971 16.0726 9.68666 15.9999 9.59974 15.8838C9.5 15.7506 9.5 15.512 9.5 15.0347V8.96533Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineIconBomb2: {
    viewBox: "0 0 16 16",
    body: '<path vector-effect="non-scaling-stroke" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="m9.566 3.1 1.3-1.3a1.607 1.607 0 0 1 2.267 0L14.2 2.867a1.6 1.6 0 0 1 0 2.266l-1.3 1.3m1.766-5.1-1 1m-.333 6.334a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z"/>',
  },
  lineImagesImage: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M16.2 21H6.93137C6.32555 21 6.02265 21 5.88238 20.8802C5.76068 20.7763 5.69609 20.6203 5.70865 20.4608C5.72312 20.2769 5.93731 20.0627 6.36569 19.6343L14.8686 11.1314C15.2646 10.7354 15.4627 10.5373 15.691 10.4632C15.8918 10.3979 16.1082 10.3979 16.309 10.4632C16.5373 10.5373 16.7354 10.7354 17.1314 11.1314L21 15V16.2M16.2 21C17.8802 21 18.7202 21 19.362 20.673C19.9265 20.3854 20.3854 19.9265 20.673 19.362C21 18.7202 21 17.8802 21 16.2M16.2 21H7.8C6.11984 21 5.27976 21 4.63803 20.673C4.07354 20.3854 3.6146 19.9265 3.32698 19.362C3 18.7202 3 17.8802 3 16.2V7.8C3 6.11984 3 5.27976 3.32698 4.63803C3.6146 4.07354 4.07354 3.6146 4.63803 3.32698C5.27976 3 6.11984 3 7.8 3H16.2C17.8802 3 18.7202 3 19.362 3.32698C19.9265 3.6146 20.3854 4.07354 20.673 4.63803C21 5.27976 21 6.11984 21 7.8V16.2M10.5 8.5C10.5 9.60457 9.60457 10.5 8.5 10.5C7.39543 10.5 6.5 9.60457 6.5 8.5C6.5 7.39543 7.39543 6.5 8.5 6.5C9.60457 6.5 10.5 7.39543 10.5 8.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  lineLayoutsColumns2: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity="1" stroke-width="1.5" d="M12 3v18M8 3h11l2 2v14l-2 2H5l-2-2V5l2-2h3Z"/>',
  },
  lineLayoutsMaximize2: {
    viewBox: "0 0 24 24",
    body: '<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-opacity=".7" stroke-width="1.5" d="m14 10 7-7m0 0h-6m6 0v6m-11 5-7 7m0 0h6m-6 0v-6"/>',
  },
  lineTimeClockStopwatch: {
    viewBox: "0 0 24 24",
    body: '<path vector-effect="non-scaling-stroke" d="M12 9.5V13.5L14.5 15M12 5C7.30558 5 3.5 8.80558 3.5 13.5C3.5 18.1944 7.30558 22 12 22C16.6944 22 20.5 18.1944 20.5 13.5C20.5 8.80558 16.6944 5 12 5ZM12 5V2M10 2H14M20.329 5.59204L18.829 4.09204L19.579 4.84204M3.67102 5.59204L5.17102 4.09204L4.42102 4.84204" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
  },
  solidAlertCircle: {
    viewBox: "0 0 24 24",
    fill: "currentColor",
    body: '<path vector-effect="non-scaling-stroke" fill-rule="evenodd" clip-rule="evenodd" d="M12 1C5.92487 1 1 5.92487 1 12C1 18.0751 5.92487 23 12 23C18.0751 23 23 18.0751 23 12C23 5.92487 18.0751 1 12 1ZM13 8C13 7.44772 12.5523 7 12 7C11.4477 7 11 7.44772 11 8V12C11 12.5523 11.4477 13 12 13C12.5523 13 13 12.5523 13 12V8ZM12 15C11.4477 15 11 15.4477 11 16C11 16.5523 11.4477 17 12 17H12.01C12.5623 17 13.01 16.5523 13.01 16C13.01 15.4477 12.5623 15 12.01 15H12Z" fill="currentColor" />',
  },
  solidCheckCircle: {
    viewBox: "0 0 24 24",
    fill: "currentColor",
    body: '<path vector-effect="non-scaling-stroke" fill-rule="evenodd" clip-rule="evenodd" d="M12 1C5.92487 1 1 5.92487 1 12C1 18.0751 5.92487 23 12 23C18.0751 23 23 18.0751 23 12C23 5.92487 18.0751 1 12 1ZM17.2071 9.70711C17.5976 9.31658 17.5976 8.68342 17.2071 8.29289C16.8166 7.90237 16.1834 7.90237 15.7929 8.29289L10.5 13.5858L8.20711 11.2929C7.81658 10.9024 7.18342 10.9024 6.79289 11.2929C6.40237 11.6834 6.40237 12.3166 6.79289 12.7071L9.79289 15.7071C10.1834 16.0976 10.8166 16.0976 11.2071 15.7071L17.2071 9.70711Z" fill="currentColor" />',
  },
  solidHelpCircle: {
    viewBox: "0 0 24 24",
    fill: "currentColor",
    body: '<path vector-effect="non-scaling-stroke" fill-rule="evenodd" clip-rule="evenodd" d="M12 1C5.92487 1 1 5.92487 1 12C1 18.0751 5.92487 23 12 23C18.0751 23 23 18.0751 23 12C23 5.92487 18.0751 1 12 1ZM10.9066 8.27123C11.3138 8.03191 11.7926 7.94443 12.2581 8.02428C12.7236 8.10413 13.1459 8.34615 13.45 8.70749C13.7542 9.06883 13.9207 9.52615 13.92 9.99847L13.92 9.99996C13.92 10.4691 13.5549 10.9582 12.8653 11.4179C12.5509 11.6275 12.2294 11.7889 11.9826 11.8986C11.8606 11.9529 11.7603 11.9929 11.6929 12.0186C11.663 12.03 11.6329 12.041 11.6027 12.0516C11.0794 12.2267 10.7968 12.7926 10.9713 13.3162C11.146 13.8401 11.7123 14.1233 12.2362 13.9486L12.4049 13.8876C12.5015 13.8508 12.6356 13.7971 12.7949 13.7263C13.1105 13.586 13.5391 13.3724 13.9747 13.082C14.7849 12.5419 15.9195 11.5312 15.92 10.0009C15.9213 9.05644 15.5883 8.14201 14.9801 7.41949C14.3717 6.69682 13.5273 6.21277 12.5962 6.05307C11.6652 5.89337 10.7077 6.06833 9.89327 6.54696C9.07886 7.02559 8.46013 7.77701 8.14666 8.66812C7.96339 9.18911 8.23716 9.76002 8.75815 9.9433C9.27914 10.1266 9.85006 9.85279 10.0333 9.33181C10.1901 8.88625 10.4994 8.51054 10.9066 8.27123ZM12 16C11.4477 16 11 16.4477 11 17C11 17.5522 11.4477 18 12 18H12.01C12.5623 18 13.01 17.5522 13.01 17C13.01 16.4477 12.5623 16 12.01 16H12Z" fill="currentColor" />',
  },
  solidMinusCircle: {
    viewBox: "0 0 24 24",
    fill: "currentColor",
    body: '<path vector-effect="non-scaling-stroke" fill-rule="evenodd" clip-rule="evenodd" d="M12 1C5.92487 1 1 5.92487 1 12C1 18.0751 5.92487 23 12 23C18.0751 23 23 18.0751 23 12C23 5.92487 18.0751 1 12 1ZM8 11C7.44772 11 7 11.4477 7 12C7 12.5523 7.44772 13 8 13H16C16.5523 13 17 12.5523 17 12C17 11.4477 16.5523 11 16 11H8Z" fill="currentColor" />',
  },
  solidXCircle: {
    viewBox: "0 0 24 24",
    fill: "currentColor",
    body: '<path vector-effect="non-scaling-stroke" fill-rule="evenodd" clip-rule="evenodd" d="M12 1C5.92487 1 1 5.92487 1 12C1 18.0751 5.92487 23 12 23C18.0751 23 23 18.0751 23 12C23 5.92487 18.0751 1 12 1ZM15.7071 8.29289C16.0976 8.68342 16.0976 9.31658 15.7071 9.70711L13.4142 12L15.7071 14.2929C16.0976 14.6834 16.0976 15.3166 15.7071 15.7071C15.3166 16.0976 14.6834 16.0976 14.2929 15.7071L12 13.4142L9.70711 15.7071C9.31658 16.0976 8.68342 16.0976 8.29289 15.7071C7.90237 15.3166 7.90237 14.6834 8.29289 14.2929L10.5858 12L8.29289 9.70711C7.90237 9.31658 7.90237 8.68342 8.29289 8.29289C8.68342 7.90237 9.31658 7.90237 9.70711 8.29289L12 10.5858L14.2929 8.29289C14.6834 7.90237 15.3166 7.90237 15.7071 8.29289Z" fill="currentColor" />',
  },
} as const);

export type IconName = keyof typeof ICONS;
export type IconSize = "xs" | "s" | "m" | "l" | "xl";

export type IconRenderOptions = {
  attributes?: Record<string, string | number | null | undefined>;
  className?: string;
  inline?: boolean;
  size?: IconSize;
  title?: string;
};

export const renderIcon = (
  name: IconName,
  {
    attributes = {},
    className = "",
    inline = false,
    size = "s",
    title = "",
  }: IconRenderOptions = {},
) => {
  const icon: IconDefinition = ICONS[name];
  const classes = ["a-icon", `a-icon_size_${size}`, inline ? "a-icon_inline" : "", className]
    .filter(Boolean)
    .join(" ");
  const attributeMarkup = Object.entries(attributes)
    .filter(([, value]) => value !== null && value !== undefined && value !== "")
    .map(([key, value]) => `${escapeAttr(key)}="${escapeAttr(String(value))}"`)
    .join(" ");

  return `<svg class="${escapeAttr(classes)}" viewBox="${escapeAttr(icon.viewBox)}" fill="${escapeAttr(
    icon.fill ?? "none",
  )}" xmlns="http://www.w3.org/2000/svg" ${title ? 'role="img"' : 'aria-hidden="true"'} ${attributeMarkup}>${title ? `<title>${escapeHtml(title)}</title>` : ""}${icon.body}</svg>`;
};

export const createIconElement = (name: IconName, options: IconRenderOptions = {}) => {
  const fragment = createFragmentFromHtml(
    renderIcon(name, options),
    document.body || document.documentElement || document.createElement("div"),
  );
  const icon = fragment.firstElementChild;

  if (!(icon instanceof SVGElement)) {
    throw new Error(`Icon "${name}" could not be rendered`);
  }

  return icon;
};
