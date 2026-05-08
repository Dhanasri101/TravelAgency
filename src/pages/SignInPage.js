import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import SplitLayout from '../components/SplitLayout';
import TextField from '../components/TextField';
import PasswordField from '../components/PasswordField';
import Toast from '../components/Toast';
import { signIn } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import '../styles/components.css';

export default function SignInPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { loginWith } = useAuth();
  const state = location.state || {};
  const redirectTo = typeof state.from === 'string' && state.from.trim() ? state.from : '/';

  const [showToast, setShowToast] = useState(Boolean(state.justRegistered));
  const [email, setEmail] = useState(state.email || '');
  const [password, setPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [lockedBanner, setLockedBanner] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function clearFieldError(field) {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next[field];
        return next;
      });
    }
  }

  function validateClient() {
    const errs = {};
    if (!email.trim()) errs.email = 'Email address is required. Please enter your email to continue';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      errs.email = 'Enter a valid email address';
    if (!password) errs.password = 'Password is required. Please enter your password to continue.';
    return errs;
  }

  async function onSubmit(e) {
    e.preventDefault();
    setLockedBanner(null);
    const errs = validateClient();
    if (Object.keys(errs).length) {
      setFieldErrors(errs);
      return;
    }
    setSubmitting(true);
    try {
      const data = await signIn({ email: email.trim(), password });
      await loginWith(data);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      if (err.status === 423) {
        setLockedBanner(
          err.message ||
            'Your account is temporarily locked due to multiple failed login attempts. Please try again later.'
        );
        setFieldErrors({});
      } else if (err.fieldErrors && Object.keys(err.fieldErrors).length) {
        setFieldErrors(err.fieldErrors);
      } else {
        setFieldErrors({
          email: 'Incorrect email or password. Try again or create an account.',
          password: 'Incorrect email or password. Try again or create an account.',
        });
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <SplitLayout>
      {showToast && (
        <Toast
          title="Congratulations"
          message="Your account has been created successfully. Please sign in with the details."
          onClose={() => setShowToast(false)}
        />
      )}

      <div className="eyebrow">WELCOME BACK</div>
      <h1 className="page-title">Sign in to your account</h1>

      {lockedBanner && (
        <div className="banner-error" role="alert">{lockedBanner}</div>
      )}

      <form onSubmit={onSubmit} noValidate>
        <TextField
          label="Email"
          type="email"
          placeholder="Enter your email"
          hint="e.g. username@domain.com"
          autoComplete="email"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            clearFieldError('email');
          }}
          error={fieldErrors.email}
        />
        <PasswordField
          label="Password"
          placeholder="Enter your password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => {
            setPassword(e.target.value);
            clearFieldError('password');
          }}
          error={fieldErrors.password}
        />
        <div className="forgot-row">
          <Link to="#">Forgot password?</Link>
        </div>
        <button className="btn-primary" type="submit" disabled={submitting || !email.trim() || !password}>
          {submitting ? 'Signing in\u2026' : 'Sign in'}
        </button>
      </form>

      <div className="form-footer">
        Don&rsquo;t have an account? <Link to="/register" state={{ from: redirectTo }}>Create an account</Link>
      </div>
    </SplitLayout>
  );
}
