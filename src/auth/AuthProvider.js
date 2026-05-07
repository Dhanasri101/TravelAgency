import React, { useCallback, useEffect, useState } from 'react';
import { setUnauthorizedHandler, TOKEN_KEY } from '../api/client';
import { fetchMe } from '../api/auth';
import { AuthContext } from './AuthContext';

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(localStorage.getItem(TOKEN_KEY)));

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const loginWith = useCallback(async (data) => {
    localStorage.setItem(TOKEN_KEY, data.idToken);
    setToken(data.idToken);
    try {
      const me = await fetchMe();
      setUser(me);
    } catch (e) {
      const [firstName, ...rest] = data.userName.split(' ');
      setUser({
        id: 'unknown',
        firstName,
        lastName: rest.join(' '),
        email: data.email,
        role: data.role,
        createdAt: '',
      });
    }
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => logout());
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    fetchMe()
      .then((u) => { if (!cancelled) setUser(u); })
      .catch(() => { if (!cancelled) logout(); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, loading, loginWith, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
