import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import {
  Search,
  Calendar,
  MapPin,
  Mail,
  FileText,
  CheckCircle2,
  XCircle,
  AlertCircle,
  AlertTriangle,
  Copy,
  Check,
  Building2,
  CreditCard,
  Sparkles,
  ArrowRight,
  RefreshCw,
} from 'lucide-react';
import { lookupBooking, cancelBooking } from '../services/api';

export default function ManageBookingPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [reference, setReference] = useState(searchParams.get('reference') || '');
  const [email, setEmail] = useState(searchParams.get('email') || '');

  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [lookupError, setLookupError] = useState(null);

  // Cancellation state
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelMessage, setCancelMessage] = useState(null);
  const [cancelError, setCancelError] = useState(null);

  // Clipboard copy state
  const [copied, setCopied] = useState(false);

  // Auto-search if both reference and email are present in query params
  useEffect(() => {
    const refParam = searchParams.get('reference');
    const emailParam = searchParams.get('email');
    if (refParam && emailParam) {
      handleLookup(refParam, emailParam);
    }
  }, []);

  const handleLookup = async (refToSearch, emailToSearch) => {
    const cleanRef = (refToSearch || reference).trim();
    const cleanEmail = (emailToSearch || email).trim();

    if (!cleanRef || !cleanEmail) {
      setLookupError('Please enter both your Booking Reference and Email Address.');
      return;
    }

    setLoading(true);
    setLookupError(null);
    setCancelMessage(null);
    setCancelError(null);

    try {
      const data = await lookupBooking(cleanRef, cleanEmail);
      setBooking(data);

      // Keep query parameters synced
      setSearchParams({ reference: cleanRef, email: cleanEmail });
    } catch (err) {
      setBooking(null);
      setLookupError(
        err.message || 'No reservation found matching the provided reference and email.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleLookupSubmit = (e) => {
    e.preventDefault();
    handleLookup(reference, email);
  };

  const handleConfirmCancel = async () => {
    if (!booking) return;

    setCancelling(true);
    setCancelError(null);

    try {
      const updatedBooking = await cancelBooking(booking.bookingReference, booking.guestEmail);
      setBooking(updatedBooking);
      setShowCancelModal(false);
      setCancelMessage(
        `Reservation ${updatedBooking.bookingReference} has been successfully cancelled. The room inventory has been released.`
      );
    } catch (err) {
      setCancelError(err.message || 'Failed to cancel reservation. Please try again.');
    } finally {
      setCancelling(false);
    }
  };

  const handleCopyReference = () => {
    if (booking?.bookingReference) {
      navigator.clipboard.writeText(booking.bookingReference);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const isConfirmed = booking?.status === 'CONFIRMED';
  const isCancelled = booking?.status === 'CANCELLED';

  return (
    <div className="min-h-screen pb-20">
      {/* Header Banner */}
      <section className="bg-gradient-to-br from-indigo-950 via-slate-900 to-indigo-900 text-white pt-12 pb-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-200 border border-indigo-400/30 text-xs font-semibold mb-3">
            <FileText className="w-3.5 h-3.5 text-indigo-400" />
            Guest Self-Service Portal
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Manage Your Hotel Reservation
          </h1>
          <p className="mt-2 text-sm sm:text-base text-slate-300 max-w-xl mx-auto">
            Retrieve your booking details, view stay dates & price breakdown, or cancel your reservation anytime.
          </p>
        </div>
      </section>

      {/* Main Container */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 -mt-10 relative z-20 space-y-8">
        {/* Lookup Form Card */}
        <div className="bg-white rounded-3xl shadow-xl border border-slate-200/80 p-6 sm:p-8 backdrop-blur-md">
          <h2 className="text-lg font-bold text-slate-900 mb-1 flex items-center gap-2">
            <Search className="w-5 h-5 text-indigo-600" />
            Find Your Booking
          </h2>
          <p className="text-xs text-slate-500 mb-6">
            Enter the 6-character booking reference code (e.g., TCT-XXXXXX) and the email address used during reservation.
          </p>

          <form onSubmit={handleLookupSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Booking Reference Input */}
            <div>
              <label className="text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5 block">
                Booking Reference
              </label>
              <div className="relative">
                <input
                  type="text"
                  placeholder="e.g. TCT-8K3M9P"
                  value={reference}
                  onChange={(e) => setReference(e.target.value.toUpperCase())}
                  required
                  className="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-hidden focus:ring-2 focus:ring-indigo-500 text-sm font-semibold tracking-wider text-slate-900 uppercase placeholder-slate-400"
                />
                <FileText className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              </div>
            </div>

            {/* Guest Email Input */}
            <div>
              <label className="text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5 block">
                Guest Email Address
              </label>
              <div className="relative">
                <input
                  type="email"
                  placeholder="e.g. rahul@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-hidden focus:ring-2 focus:ring-indigo-500 text-sm font-medium text-slate-900 placeholder-slate-400"
                />
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              </div>
            </div>

            {/* Submit Button */}
            <div className="sm:col-span-2 pt-2">
              <button
                type="submit"
                disabled={loading}
                className="w-full sm:w-auto px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-98 text-white text-sm font-bold shadow-md shadow-indigo-100 flex items-center justify-center gap-2 transition-all"
              >
                {loading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    <span>Searching Reservation...</span>
                  </>
                ) : (
                  <>
                    <Search className="w-4 h-4" />
                    <span>Find Reservation</span>
                  </>
                )}
              </button>
            </div>
          </form>

          {/* Lookup Error Message */}
          {lookupError && (
            <div className="mt-5 p-4 rounded-2xl bg-red-50 border border-red-200 text-red-700 text-xs sm:text-sm flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-red-600 mt-0.5 shrink-0" />
              <div>
                <span className="font-bold">Reservation Not Found: </span>
                {lookupError}
              </div>
            </div>
          )}
        </div>

        {/* Cancellation Success Toast / Notification */}
        {cancelMessage && (
          <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-start gap-3 shadow-xs">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 mt-0.5 shrink-0" />
            <div className="flex-1 font-medium">{cancelMessage}</div>
          </div>
        )}

        {/* Retrieved Booking Details Card */}
        {booking && (
          <div className="bg-white rounded-3xl shadow-lg border border-slate-200 overflow-hidden">
            {/* Header with Reference & Status Badge */}
            <div className="p-6 sm:p-8 bg-slate-50 border-b border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <div className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">
                  Reservation Reference
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-2xl font-black text-slate-900 tracking-wider">
                    {booking.bookingReference}
                  </span>
                  <button
                    type="button"
                    onClick={handleCopyReference}
                    title="Copy Reference"
                    className="p-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-100 text-slate-600 transition-colors"
                  >
                    {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              {/* Status Badge */}
              <div>
                {isConfirmed && (
                  <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200 text-xs font-bold">
                    <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                    CONFIRMED
                  </span>
                )}
                {isCancelled && (
                  <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-red-100 text-red-800 border border-red-200 text-xs font-bold">
                    <XCircle className="w-4 h-4 text-red-600" />
                    CANCELLED
                  </span>
                )}
              </div>
            </div>

            {/* Booking Body Grid */}
            <div className="p-6 sm:p-8 grid grid-cols-1 md:grid-cols-2 gap-8">
              {/* Hotel & Guest Information */}
              <div className="space-y-5">
                <div>
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">
                    Property Details
                  </h3>
                  <h4 className="text-lg font-bold text-slate-900 flex items-center gap-1.5">
                    <Building2 className="w-5 h-5 text-indigo-600" />
                    {booking.hotelName}
                  </h4>
                  <p className="text-xs text-slate-500 mt-1 flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5 text-amber-500" />
                    {booking.hotelLocation}, India
                  </p>
                </div>

                <div className="pt-4 border-t border-slate-100">
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">
                    Guest Information
                  </h3>
                  <p className="text-sm font-semibold text-slate-800">{booking.guestName}</p>
                  <p className="text-xs text-slate-500">{booking.guestEmail}</p>
                </div>

                <div className="pt-4 border-t border-slate-100">
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">
                    Booked On
                  </h3>
                  <p className="text-xs text-slate-600">
                    {booking.createdAt ? new Date(booking.createdAt).toLocaleString('en-IN') : 'Confirmed'}
                  </p>
                </div>
              </div>

              {/* Stay Dates & Financial Breakdown */}
              <div className="space-y-5 bg-slate-50/70 p-5 rounded-2xl border border-slate-100">
                <div>
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                    Stay Schedule
                  </h3>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className="p-2.5 bg-white rounded-xl border border-slate-200">
                      <span className="text-slate-400 block mb-0.5">Check-in</span>
                      <strong className="text-slate-800 text-sm">{booking.checkInDate}</strong>
                    </div>
                    <div className="p-2.5 bg-white rounded-xl border border-slate-200">
                      <span className="text-slate-400 block mb-0.5">Check-out</span>
                      <strong className="text-slate-800 text-sm">{booking.checkOutDate}</strong>
                    </div>
                  </div>
                  <div className="text-xs text-slate-500 mt-2 font-medium">
                    Total Duration: <strong>{booking.nights} nights</strong>
                  </div>
                </div>

                {/* Financial Summary */}
                <div className="pt-4 border-t border-slate-200 space-y-2 text-xs">
                  <div className="flex justify-between text-slate-600">
                    <span>Rate per night</span>
                    <span>₹{Number(booking.pricePerNight).toLocaleString('en-IN')}</span>
                  </div>

                  <div className="flex justify-between text-slate-600">
                    <span>Base Amount ({booking.nights} nights)</span>
                    <span>₹{Number(booking.baseAmount).toLocaleString('en-IN')}</span>
                  </div>

                  {Number(booking.discountAmount) > 0 && (
                    <div className="flex justify-between text-emerald-700 font-semibold bg-emerald-50 p-2 rounded-lg border border-emerald-100">
                      <span className="flex items-center gap-1">
                        <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                        Smart Stay Long-Stay Discount (10% off)
                      </span>
                      <span>- ₹{Number(booking.discountAmount).toLocaleString('en-IN')}</span>
                    </div>
                  )}

                  <div className="pt-2 border-t border-slate-200 flex justify-between items-baseline text-sm">
                    <span className="font-bold text-slate-800">Total Booking Amount</span>
                    <span className="text-lg font-extrabold text-indigo-900">
                      ₹{Number(booking.totalAmount).toLocaleString('en-IN')}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Card Actions Footer */}
            <div className="px-6 sm:px-8 py-5 bg-slate-50 border-t border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                {isConfirmed && (
                  <p className="text-xs text-slate-500">
                    Need to cancel? You can cancel your stay free of charge. Room capacity will be immediately released.
                  </p>
                )}
                {isCancelled && (
                  <p className="text-xs text-red-600 font-medium">
                    This booking was cancelled. The room inventory has been returned to the hotel pool.
                  </p>
                )}
              </div>

              {isConfirmed && (
                <button
                  type="button"
                  onClick={() => setShowCancelModal(true)}
                  className="px-5 py-2.5 rounded-xl bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 text-xs font-bold transition-colors self-start sm:self-auto"
                >
                  Cancel Booking
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Cancellation Confirmation Dialog / Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 sm:p-7 shadow-2xl border border-slate-200">
            <div className="w-12 h-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto mb-4">
              <AlertTriangle className="w-6 h-6" />
            </div>

            <h3 className="text-lg font-bold text-slate-900 text-center">
              Cancel Reservation?
            </h3>
            <p className="text-xs text-slate-500 text-center mt-1.5 leading-relaxed">
              Are you sure you want to cancel your reservation <strong>{booking?.bookingReference}</strong> at{' '}
              <strong>{booking?.hotelName}</strong>?
            </p>

            <div className="my-4 p-3.5 bg-amber-50 rounded-xl border border-amber-200 text-xs text-amber-800">
              This action cannot be undone. Room inventory will be immediately released and made available for other guests.
            </div>

            {cancelError && (
              <div className="mb-4 p-3 bg-red-50 rounded-xl text-xs text-red-700 font-medium">
                {cancelError}
              </div>
            )}

            <div className="flex gap-2.5 mt-5">
              <button
                type="button"
                disabled={cancelling}
                onClick={() => setShowCancelModal(false)}
                className="flex-1 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-semibold text-xs hover:bg-slate-100 transition-colors"
              >
                Keep Booking
              </button>
              <button
                type="button"
                disabled={cancelling}
                onClick={handleConfirmCancel}
                className="flex-1 py-2.5 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold text-xs shadow-xs transition-colors flex items-center justify-center gap-1.5"
              >
                {cancelling ? 'Cancelling...' : 'Yes, Cancel Reservation'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
