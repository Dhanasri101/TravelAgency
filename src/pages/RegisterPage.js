import React, { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import SplitLayout from '../components/SplitLayout';
import TextField from '../components/TextField';
import PasswordField from '../components/PasswordField';
import PasswordRuleList from '../components/PasswordRuleList';
import { allRulesPass, evaluatePasswordRules } from '../hooks/usePasswordRules';
import { signUp } from '../api/auth';
import '../styles/components.css';

const empty = {
  firstName: '', lastName: '', email: '', password: '', confirmPassword: '',
};

export default function RegisterPage() {
  const navigate = useNavigate();
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
        state: { justRegistered: true, email: form.email.trim() },
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
        Already have an account? <Link to="/sign-in">Login</Link> instead
      </div>
    </SplitLayout>
  );
}
