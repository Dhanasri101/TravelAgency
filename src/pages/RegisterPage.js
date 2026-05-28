import React, { useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import SplitLayout from '../components/SplitLayout';
import TextField from '../components/TextField';
import PasswordField from '../components/PasswordField';
import PasswordRuleList from '../components/PasswordRuleList';
import { allRulesPass, evaluatePasswordRules } from '../hooks/usePasswordRules';
import { signUp, loginWithGoogle, loginWithFacebook } from '../api/auth';
import '../styles/components.css';

const empty = {
  firstName: '', lastName: '', email: '', password: '', confirmPassword: '',
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state || {};
  const redirectTo = typeof state.from === 'string' && state.from.trim() ? state.from : '/';
  const [form, setForm] = useState(empty);
  const [fieldErrors, setFieldErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [banner, setBanner] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const rules = useMemo(
    () => evaluatePasswordRules(form.password, form.firstName, form.email),
    [form.password, form.firstName, form.email]
  );

  function validateField(name, state) {
    const v = state[name];
    if (name === 'firstName') {
      if (!v.trim()) return 'First name is required';
      if (!/^[A-Za-z\u00C0-\u024F][A-Za-z\u00C0-\u024F'\- ]{0,49}$/.test(v.trim()))
        return 'First name must be up to 50 characters. Only Latin letters, hyphens, and apostrophes are allowed.';
      return null;
    }
    if (name === 'lastName') {
      if (!v.trim()) return 'Last name is required';
      if (!/^[A-Za-z\u00C0-\u024F][A-Za-z\u00C0-\u024F'\- ]{0,49}$/.test(v.trim()))
        return 'Last name must be up to 50 characters. Only Latin letters, hyphens, and apostrophes are allowed.';
      return null;
    }
    if (name === 'email') {
      if (!v.trim()) return 'Email is required';
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))
        return 'Invalid email address. Please ensure it follows the format: username@domain.com';
      return null;
    }
    if (name === 'password') {
      if (!v) return 'Password is required';
      if (!allRulesPass(evaluatePasswordRules(v, state.firstName, state.email)))
        return 'Password does not meet all requirements';
      return null;
    }
    if (name === 'confirmPassword') {
      if (!v) return 'Please confirm your password';
      if (v !== state.password) return "Passwords don't match.";
      return null;
    }
    return null;
  }

  function applyErrors(state, fields) {
    setFieldErrors((prev) => {
      const next = { ...prev };
      fields.forEach((k) => {
        const msg = validateField(k, state);
        if (msg) next[k] = msg;
        else delete next[k];
      });
      return next;
    });
  }

  const update = (field) => (e) => {
    const value = e.target.value;
    setForm((prev) => {
      const nextState = { ...prev, [field]: value };
      const fieldsToCheck = Object.keys(nextState).filter((k) => touched[k]);
      if (fieldsToCheck.length) applyErrors(nextState, fieldsToCheck);
      return nextState;
    });
  };

  const onFieldBlur = (field) => () => {
    setTouched((t) => (t[field] ? t : { ...t, [field]: true }));
    applyErrors(form, [field]);
  };

  function validateClient() {
    const errs = {};
    Object.keys(form).forEach((k) => {
      const msg = validateField(k, form);
      if (msg) errs[k] = msg;
    });
    return errs;
  }

  async function onSubmit(e) {
    e.preventDefault();
    setBanner(null);
    const errs = validateClient();
    if (Object.keys(errs).length) {
      setFieldErrors(errs);
      return;
    }
    setSubmitting(true);
    try {
      await signUp({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      navigate('/sign-in', {
        state: { justRegistered: true, email: form.email.trim(), from: redirectTo },
        replace: true,
      });
    } catch (err) {
      if (err.fieldErrors && Object.keys(err.fieldErrors).length) {
        setFieldErrors(err.fieldErrors);
      } else {
        setBanner(err.message || err.error || 'Something went wrong. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <SplitLayout>
      <div className="eyebrow">LET&rsquo;S GET YOU STARTED</div>
      <h1 className="page-title">Create an account</h1>

      {banner && <div className="banner-error" role="alert">{banner}</div>}

      <form onSubmit={onSubmit} noValidate>
        <div className="form-grid">
          <TextField
            label="First name"
            placeholder="Enter your first name"
            hint="e.g. Johnson"
            autoComplete="given-name"
            value={form.firstName}
            onChange={update('firstName')}
            onBlur={onFieldBlur('firstName')}
            error={fieldErrors.firstName}
          />
          <TextField
            label="Last name"
            placeholder="Enter your last name"
            hint="e.g. Doe"
            autoComplete="family-name"
            value={form.lastName}
            onChange={update('lastName')}
            onBlur={onFieldBlur('lastName')}
            error={fieldErrors.lastName}
          />
          <div className="form-row">
            <TextField
              label="Email"
              type="email"
              placeholder="Enter your email"
              hint="e.g. username@domain.com"
              autoComplete="email"
              value={form.email}
              onChange={update('email')}
              onBlur={onFieldBlur('email')}
              error={fieldErrors.email}
            />
          </div>
          <div className="form-row">
            <PasswordField
              label="Password"
              placeholder="Enter your password"
              autoComplete="new-password"
              value={form.password}
              onChange={update('password')}
              onBlur={onFieldBlur('password')}
              error={fieldErrors.password}
            />
            <PasswordRuleList rules={rules} active={form.password.length > 0 || Boolean(touched.password)} />
          </div>
          <div className="form-row">
            <PasswordField
              label="Confirm password"
              placeholder="Confirm your password"
              hint="Confirm password must match your password"
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={update('confirmPassword')}
              onBlur={onFieldBlur('confirmPassword')}
              error={fieldErrors.confirmPassword}
            />
          </div>
        </div>

        <button
          className="btn-primary"
          type="submit"
          disabled={submitting || !form.firstName || !form.lastName || !form.email || !form.password || !form.confirmPassword}
        >
          {submitting ? 'Creating account\u2026' : 'Create an account'}
        </button>
      </form>

      <div className="form-footer">
        Already have an account? <Link to="/sign-in" state={{ from: redirectTo }}>Login</Link> instead
      </div>

      {/* Social login divider */}
      <div className="social-divider">
        <span className="social-divider__line" />
        <span className="social-divider__text">or sign up with</span>
        <span className="social-divider__line" />
      </div>

      {/* Social login buttons */}
      <div className="social-buttons">
        <button
          type="button"
          className="btn-social btn-social--google"
          onClick={loginWithGoogle}
          aria-label="Sign up with Google"
        >
          <svg className="social-icon" viewBox="0 0 48 48" aria-hidden="true">
            <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
            <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
            <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
            <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
          </svg>
          Continue with Google
        </button>

        <button
          type="button"
          className="btn-social btn-social--facebook"
          onClick={loginWithFacebook}
          aria-label="Sign up with Facebook"
        >
          <svg className="social-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path fill="#1877F2" d="M24 12.073C24 5.446 18.627 0 12 0S0 5.446 0 12.073C0 18.1 4.388 23.094 10.125 24v-8.437H7.078v-3.49h3.047v-2.66c0-3.025 1.792-4.697 4.533-4.697 1.312 0 2.686.236 2.686.236v2.971h-1.514c-1.491 0-1.956.932-1.956 1.887v2.263h3.328l-.532 3.49h-2.796V24C19.612 23.094 24 18.1 24 12.073z"/>
          </svg>
          Continue with Facebook
        </button>
      </div>

      <p className="social-consent">
        By continuing with Google or Facebook, you allow us to access your name and email for account authentication.
      </p>
      <p className="social-switch-hint">
        Need to use a different Facebook account?{' '}
        <a href="https://www.facebook.com" target="_blank" rel="noreferrer">Open Facebook</a>{' '}and switch accounts first.
      </p>
    </SplitLayout>
  );
}
