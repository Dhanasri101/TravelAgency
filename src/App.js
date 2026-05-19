import React from 'react';
import { Route, Routes } from 'react-router-dom';
import RegisterPage from './pages/RegisterPage';
import SignInPage from './pages/SignInPage';
import HomePage from './pages/HomePage';
import TourDetailPage from './pages/TourDetailPage';
import MyToursPage from './pages/MyToursPage';
import ReportsPage from './pages/ReportsPage';
import FeedbackModerationPage from './pages/FeedbackModerationPage';
import ProtectedRoute from './auth/ProtectedRoute';
import PublicOnlyRoute from './auth/PublicOnlyRoute';
import AdminRoute from './auth/AdminRoute';

export default function App() {
  return (
    <Routes>
      <Route
        path="/register"
        element={
          <PublicOnlyRoute>
            <RegisterPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/sign-in"
        element={
          <PublicOnlyRoute>
            <SignInPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/"
        element={<HomePage />}
      />
      <Route
        path="/tours/:id"
        element={<TourDetailPage />}
      />
      <Route
        path="/my-tours"
        element={
          <ProtectedRoute>
            <MyToursPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports"
        element={
          <AdminRoute>
            <ReportsPage />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/feedback"
        element={
          <AdminRoute>
            <FeedbackModerationPage />
          </AdminRoute>
        }
      />
      <Route
        path="*"
        element={<HomePage />}
      />
    </Routes>
  );
}
