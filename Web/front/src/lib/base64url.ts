export function encodeUserId(username: string): string {
    // UTF-8 encode
    const utf8Bytes = new TextEncoder().encode(username);
    // Convert to binary string
    let binary = '';
    for (let i = 0; i < utf8Bytes.length; i++) {
        binary += String.fromCharCode(utf8Bytes[i]);
    }
    // Base64
    const base64 = btoa(binary);
    // Base64Url: + -> -, / -> _, remove =
    return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

export function decodeUserId(encoded: string): string {
    // Base64Url -> Base64
    let base64 = encoded.replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) {
        base64 += '=';
    }
    const binary = atob(base64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return new TextDecoder().decode(bytes);
}