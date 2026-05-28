import { client, API_BASE_URL } from './client';

function normalize(err) {
  if (err.response?.data) {
    return { ...err.response.data, status: err.response.status };
  }
  return { status: 0, error: 'Network error', fieldErrors: {} };
}

export async function signUp(payload) {
  try {
    const { data } = await client.post('/auth/sign-up', payload);
    return data;
  } catch (err) {
    throw normalize(err);
  }
}

export async function signIn(payload) {
  try {
    const { data } = await client.post('/auth/sign-in', payload);
    return data;
  } catch (err) {
    throw normalize(err);
  }
}

export async function fetchMe() {
  try {
    const { data } = await client.get('/auth/me');
    return data;
  } catch (err) {
    throw normalize(err);
  }
}

// Social login — redirects the browser to the backend OAuth2 authorization endpoint.
// The backend then redirects to the OAuth provider (Google / GitHub).
export function loginWithGoogle() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
}

export function loginWithGitHub() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/github`;
}

export function loginWithFacebook() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/facebook`;
}
