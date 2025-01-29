// Use runtime-injected variables via window.RUNTIME_CONFIG
export const PLAIN_API_URL = window.RUNTIME_CONFIG?.PLAIN_API_URLL || 'localhost:8080';
export const BACKEND_URL = window.RUNTIME_CONFIG?.REACT_APP_BACKEND_URL || 'https://localhost:8080';
export const API_URL = window.RUNTIME_CONFIG?.REACT_APP_API_URL || 'https://localhost:8080/api';
export const APP_IS_PRODUCTION = window.RUNTIME_CONFIG?.APP_IS_PRODUCTION === 'true';
export const APP_IS_ACCESS_TOKEN_SAMESITE_STRICT = window.RUNTIME_CONFIG?.APP_IS_ACCESS_TOKEN_SAMESITE_STRICT === 'false';


