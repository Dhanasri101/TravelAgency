import React, { useState, useEffect, useCallback } from 'react';
import { client } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import Header from '../components/Header';
import './MyToursPage.css';

const STATUS_TABS = ['All tours', 'Booked', 'Confirmed', 'Started', 'Finished', 'Cancelled'];
const STEPS = ['Booked', 'Confirmed', 'Started', 'Finished'];

function PinIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#6a9ab5" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/>
      <circle cx="12" cy="10" r="3"/>
    </svg>
  );
}
function CalIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
    </svg>
  );
}
function ForkIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"/>
    </svg>
  );
}
function PersonIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
    </svg>
  );
}
function WalletIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M16 12h.01"/>
    </svg>
  );
}
function DocIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
    </svg>
  );
}
function AgentIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="3" width="20" height="18" rx="2"/><path d="M8 7h.01"/><path d="M12 7h.01"/><path d="M16 7h.01"/>
    </svg>
  );
}
function EmailIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="4" width="20" height="16" rx="2"/><polyline points="22,6 12,13 2,6"/>
    </svg>
  );
}
function PhoneIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#4a7d96" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
    </svg>
  );
}
function CheckIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="20 6 9 17 4 12"/>
    </svg>
  );
}

function getStepIndex(state) {
  const map = { BOOKED: 0, CONFIRMED: 1, STARTED: 2, FINISHED: 3, CANCELLED: -1 };
  return map[state] ?? 0;
}

function ProgressStepper({ state }) {
  const current = getStepIndex(state);
  const isCancelled = state === 'CANCELLED';

  return (
    <div className="mt-stepper">
      {STEPS.map((step, i) => {
        const done = !isCancelled && i <= current;
        return (
          <div key={step} className={`mt-step ${done ? 'done' : ''} ${i === 0 ? 'first' : ''} ${i === STEPS.length - 1 ? 'last' : ''}`}>
            <span className="mt-step-content">
              {done && <CheckIcon />}
              <span className="mt-step-label">{step}</span>
            </span>
          </div>
        );
      })}
    </div>
  );
}

function BookingCard({ booking, onCancel }) {
  const { tourDetails, travelAgent } = booking;

  return (
    <div className="mt-card">
      <ProgressStepper state={booking.state} />

      <div className="mt-card-body">
        {/* Tour header */}
        <div className="mt-tour-header">
          {booking.tourImageUrl && (
            <img className="mt-tour-img" src={booking.tourImageUrl} alt={booking.name} />
          )}
          <div>
            <h3 className="mt-tour-name">{booking.name}</h3>
            <div className="mt-tour-dest"><PinIcon /> {booking.destination}</div>
          </div>
        </div>

        {/* Details grid */}
        <div className="mt-details-grid">
          {/* Left: Tour details */}
          <div className="mt-details-col">
            <h4 className="mt-col-title">Tour details</h4>
            {tourDetails?.date && (
              <div className="mt-detail-row"><CalIcon /><span>{tourDetails.date}</span></div>
            )}
            {tourDetails?.mealPlan && (
              <div className="mt-detail-row"><ForkIcon /><span>{tourDetails.mealPlan}</span></div>
            )}
            {tourDetails?.guests && (
              <div className="mt-detail-row"><PersonIcon /><span>{tourDetails.guests}</span></div>
            )}
            {tourDetails?.totalPrice && (
              <div className="mt-detail-row"><WalletIcon /><span>Total price <strong>{tourDetails.totalPrice}</strong></span></div>
            )}
            <div className="mt-detail-row"><DocIcon /><span>Documents uploaded: {tourDetails?.documents || '0 items'}</span></div>
          </div>

          {/* Right: Travel agent */}
          <div className="mt-details-col">
            <h4 className="mt-col-title">Travel agent</h4>
            {travelAgent ? (
              <>
                {travelAgent.name && <div className="mt-detail-row"><AgentIcon /><span>{travelAgent.name}</span></div>}
                {travelAgent.email && <div className="mt-detail-row"><EmailIcon /><span>{travelAgent.email}</span></div>}
                {travelAgent.phone && <div className="mt-detail-row"><PhoneIcon /><span>{travelAgent.phone}</span></div>}
                {travelAgent.messenger && <div className="mt-detail-row"><span className="mt-messenger-icon">💬</span><span>{travelAgent.messenger}</span></div>}
              </>
            ) : (
              <div className="mt-detail-row"><span className="mt-no-agent">Not assigned yet</span></div>
            )}
          </div>
        </div>
      </div>

      {/* Actions */}
      {booking.state !== 'CANCELLED' && booking.state !== 'FINISHED' && (
        <div className="mt-card-actions">
          <button className="mt-btn-outline" onClick={() => onCancel(booking.id)}>Cancel</button>
          <button className="mt-btn-outline">Edit</button>
          <button className="mt-btn-solid">Upload documents</button>
        </div>
      )}

      {booking.state === 'CANCELLED' && (
        <div className="mt-cancelled-info">
          <span className="mt-cancelled-badge">Cancelled</span>
          {booking.cancelReason && <span className="mt-cancel-reason">Reason: {booking.cancelReason}</span>}
        </div>
      )}
    </div>
  );
}

export default function MyToursPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('my');
  const [statusFilter, setStatusFilter] = useState('All tours');
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchBookings = useCallback(async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      const res = await client.get(`/bookings?userId=${user.id}`);
      setBookings(res.data.bookings || []);
    } catch (err) {
      console.error('Failed to fetch bookings:', err);
      setBookings([]);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await client.patch(`/bookings/${bookingId}/cancel`);
      fetchBookings();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel booking');
    }
  };

  const filtered = statusFilter === 'All tours'
    ? bookings
    : bookings.filter(b => b.state === statusFilter.toUpperCase());

  return (
    <div className="app">
      <Header activeTab={activeTab} onTabChange={setActiveTab} />

      <div className="main-content">
        {/* Status filter tabs */}
        <div className="mt-status-tabs">
          {STATUS_TABS.map(tab => (
            <button
              key={tab}
              className={`mt-status-tab ${statusFilter === tab ? 'active' : ''}`}
              onClick={() => setStatusFilter(tab)}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Bookings grid */}
        {loading ? (
          <div className="mt-loading">Loading your bookings…</div>
        ) : filtered.length === 0 ? (
          <div className="mt-empty">
            <p>No bookings found{statusFilter !== 'All tours' ? ` with status "${statusFilter}"` : ''}.</p>
          </div>
        ) : (
          <div className="mt-bookings-grid">
            {filtered.map(booking => (
              <BookingCard key={booking.id} booking={booking} onCancel={handleCancel} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

