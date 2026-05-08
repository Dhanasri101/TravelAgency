import axios from 'axios';

export const TOKEN_KEY = 'ta.jwt';

export const client = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers = config.headers || {};
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

let onUnauthorized = null;

export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn;
}

client.interceptors.response.use(
  (res) => res,
  (err) => {
    const sentAuth = Boolean(err.config?.headers?.Authorization);
    if (err.response?.status === 401 && sentAuth && onUnauthorized) {
      onUnauthorized();
    }
    return Promise.reject(err);
  }
);
