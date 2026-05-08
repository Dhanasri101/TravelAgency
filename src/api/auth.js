import { client } from './client';

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
