import {SafeString} from 'handlebars/runtime';

export default function attachmentUri(source) {
  let sourceUri;
  if (source.startsWith('http')) {
    sourceUri = source;
  } else {
    sourceUri = 'data/attachments/' + source;
  }
  return new SafeString(`<a class="link" href="${sourceUri}" target="_blank" data-tooltip="Open attachment in new tab">`);
}
