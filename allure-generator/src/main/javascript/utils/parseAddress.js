const RFC2822_ADDRESS = /^(.*) <(.*)>$/;
const LOOKS_LIKE_EMAIL = /^[^@]+@[^@]+$/;

/**
 * Parse a potentially RFC 2822 address into a display name and an address.
 *
 * @param {string | null | undefined} maybeAddress
 * @returns {{ displayName: string, url?: string } | null
 */
export default function parseAddress(maybeAddress) {
  if (!maybeAddress) {
    return null;
  }

  const match = maybeAddress.match(RFC2822_ADDRESS);
  if (match) {
    return {
      displayName: match[1],
      url: toHref(match[2]),
    };
  }

  return {
    displayName: maybeAddress,
    url: toHref(maybeAddress),
  };
}

/**
 * If the address is a valid URL, returns the URL.
 * If the address resembles an email address, returns a mailto: URL.
 * Otherwise, returns undefined.
 *
 * @param {string} address
 * @returns {string | undefined}
 */
function toHref(address) {
  if (isValidURL(address)) {
    return address;
  }

  if (LOOKS_LIKE_EMAIL.test(address)) {
    return `mailto:${address}`;
  }
}

/**
 * If the address is a valid URL, returns the URL.
 *
 * @param {string} maybeURL
 * @returns {boolean}
 */
function isValidURL(maybeURL) {
  try {
    new URL(maybeURL);
    return true;
  } catch (_error) {
    return false;
  }
}
