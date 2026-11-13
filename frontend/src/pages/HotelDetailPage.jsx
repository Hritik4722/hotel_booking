import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useSearchParams, Link, useNavigate } from 'react-router-dom';
import {
  MapPin,
  Calendar,
  Sparkles,
  AlertCircle,
  CheckCircle2,
  Copy,
  Check,
  ArrowLeft,
  ShieldCheck,
  User,
  Mail,
  Clock,
  Tag,
  CreditCard,
  Building2,
  ExternalLink,
} from 'lucide-react';
import { getHotelById, createBooking } from '../services/api';

// Curated high quality hotel images keyed by hotel ID
const HOTEL_IMAGES = {
  1: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80', // Udaipur Palace
  2: 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80', // Goa Coastal
  3: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80', // Jaipur Haveli
  4: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80', // Manali Alpine
  5: 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80', // Mumbai Boulevard
  6: 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80', // Bengaluru TechCity
};

export default function HotelDetailPage() {
  const { id } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const [checkIn, setCheckIn] = useState(searchParams.get('checkIn') || '');
  const [checkOut, setCheckOut] = useState(searchParams.get('checkOut') || '');

  const [hotel, setHotel] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState(null);

  // Booking Form State
  const [guestName, setGuestName] = useState('');
  const [guestEmail, setGuestEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [bookingErrors, setBookingErrors] = useState({ general: null, fieldErrors: {} });

  // Confirmation Modal / View State
  const [confirmedBooking, setConfirmedBooking] = useState(null);
  const [copied, setCopied] = useState(false);

  // Prevent past dates in UI
  const today = useMemo(() => new Date().toISOString().split('T')[0], []);

  const minCheckOutDate = useMemo(() => {
    if (checkIn) {
      const d = new Date(checkIn);
      d.setDate(d.getDate() + 1);
      return d.toISOString().split('T')[0];
    }
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  }, [checkIn]);

  // Load hotel details and live availability
  const loadHotelData = async (cIn, cOut) => {
    setLoading(true);
    setFetchError(null);
    try {
      const data = await getHotelById(id, cIn, cOut);
      setHotel(data);
    } catch (err) {
      setFetchError(err.message || 'Failed to load hotel details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHotelData(checkIn, checkOut);
  }, [id]);

  // Handle date changes and refresh live pricing from backend
  const handleDateChange = (newCheckIn, newCheckOut) => {
    setCheckIn(newCheckIn);
    setCheckOut(newCheckOut);

    const params = {};
    if (newCheckIn) params.checkIn = newCheckIn;
    if (newCheckOut) params.checkOut = newCheckOut;
    setSearchParams(params);

    if (newCheckIn && newCheckOut) {
      loadHotelData(newCheckIn, newCheckOut);
    }
  };

  // Submit Booking Form
  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    setBookingErrors({ general: null, fieldErrors: {} });

    if (!checkIn || !checkOut) {
      setBookingErrors({
        general: 'Please select both check-in and check-out dates before confirming your reservation.',
        fieldErrors: {},
      });
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        hotelId: Number(id),
        guestName: guestName.trim(),
        guestEmail: guestEmail.trim(),
        checkInDate: checkIn,
        checkOutDate: checkOut,
      };

      const response = await createBooking(payload);
      setConfirmedBooking(response);
    } catch (err) {
      setBookingErrors({
        general: err.message || 'Failed to complete booking. Please try again.',
        fieldErrors: err.fieldErrors || {},
      });
    } finally {
      setSubmitting(false);
    }
  };

  // Copy reference code with feedback
  const handleCopyReference = () => {
    if (confirmedBooking?.bookingReference) {
      navigator.clipboard.writeText(confirmedBooking.bookingReference);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-slate-200 rounded-md w-1/4" />
          <div className="h-80 bg-slate-200 rounded-2xl" />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="md:col-span-2 space-y-4">
              <div className="h-6 bg-slate-200 rounded-md w-1/2" />
              <div className="h-24 bg-slate-200 rounded-md" />
            </div>
            <div className="h-64 bg-slate-200 rounded-2xl" />
          </div>
        </div>
      </div>
    );
  }

  if (fetchError || !hotel) {
    return (
      <div className="max-w-xl mx-auto px-4 py-20 text-center">
        <div className="w-16 h-16 mx-auto rounded-full bg-red-100 flex items-center justify-center text-red-600 mb-4">
          <AlertCircle className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-slate-800">Hotel Not Found</h2>
        <p className="text-slate-500 mt-2">{fetchError || 'Unable to retrieve hotel details.'}</p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 mt-6 px-5 py-2.5 bg-indigo-600 text-white rounded-xl font-semibold text-sm shadow-sm hover:bg-indigo-700"
        >
          <ArrowLeft className="w-4 h-4" />
          Return to Hotels
        </Link>
      </div>
    );
  }

  const {
    name,
    location,
    description,
    amenities,
    pricePerNight,
    availableRooms,
    demandStatus,
    priceBreakdown,
  } = hotel;

  const isSoldOut = availableRooms <= 0 || demandStatus === 'SOLD_OUT';
  const isHighDemand = availableRooms === 1 || demandStatus === 'HIGH_DEMAND';
  const imageUrl = HOTEL_IMAGES[hotel.id] || HOTEL_IMAGES[1];

  const amenitiesList = amenities
    ? amenities.split(',').map((a) => a.trim())
    : [];

  return (
    <div className="min-h-screen pb-20">
      {/* Back Navigation Bar */}
      <div className="bg-white border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3.5 flex items-center justify-between">
          <Link
            to={checkIn && checkOut ? `/?checkIn=${checkIn}&checkOut=${checkOut}` : '/'}
            className="inline-flex items-center gap-1.5 text-sm font-semibold text-slate-600 hover:text-indigo-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to search results</span>
          </Link>
          <span className="text-xs text-slate-400 font-medium">Hotel Reference ID: #{hotel.id}</span>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8">
        {/* Main Grid: Details (Left 2 cols) & Booking Card (Right 1 col) */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
          {/* Left Column: Media & Hotel Information */}
          <div className="lg:col-span-2 space-y-6">
            {/* Hero Image */}
            <div className="relative h-72 sm:h-96 rounded-3xl overflow-hidden shadow-md bg-slate-100">
              <img
                src={imageUrl}
                alt={name}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />

              {/* Badges on Hero */}
              <div className="absolute top-4 left-4 flex flex-wrap gap-2">
                <span className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-black/60 backdrop-blur-md text-white text-xs font-semibold">
                  <MapPin className="w-3.5 h-3.5 text-amber-400" />
                  {location}, India
                </span>

                {priceBreakdown?.discountApplied && (
                  <span className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-emerald-600 text-white text-xs font-bold shadow-md animate-bounce">
                    <Sparkles className="w-3.5 h-3.5" />
                    10% Long-Stay Discount Applied
                  </span>
                )}
              </div>

              {/* Header Title inside Hero */}
              <div className="absolute bottom-4 left-4 right-4 text-white">
                <h1 className="text-2xl sm:text-4xl font-extrabold tracking-tight drop-shadow-md">
                  {name}
                </h1>
                <p className="text-sm sm:text-base text-slate-200 mt-1 flex items-center gap-2">
                  <span>₹{Number(pricePerNight).toLocaleString('en-IN')} per night</span>
                  <span>•</span>
                  <span>{hotel.totalRooms} total rooms in property</span>
                </p>
              </div>
            </div>

            {/* Live Availability Status Pill */}
            <div className="flex items-center justify-between p-4 rounded-2xl bg-white border border-slate-200 shadow-xs">
              <div className="flex items-center gap-3">
                <div
                  className={`w-3.5 h-3.5 rounded-full ${
                    isSoldOut
                      ? 'bg-red-500 ring-4 ring-red-100'
                      : isHighDemand
                      ? 'bg-amber-500 ring-4 ring-amber-100 animate-pulse'
                      : 'bg-emerald-500 ring-4 ring-emerald-100'
                  }`}
                />
                <div>
                  <h4 className="text-sm font-bold text-slate-900">
                    {isSoldOut
                      ? 'Sold Out for Selected Stay'
                      : isHighDemand
                      ? 'High Demand: Only 1 Room Left!'
                      : `${availableRooms} Rooms Currently Available`}
                  </h4>
                  <p className="text-xs text-slate-500">
                    {checkIn && checkOut
                      ? `Inventory verified for stay between ${checkIn} and ${checkOut}`
                      : 'Select dates to verify real-time room availability'}
                  </p>
                </div>
              </div>

              {isHighDemand && (
                <span className="hidden sm:inline-flex px-3 py-1 bg-amber-50 text-amber-800 border border-amber-200 rounded-full text-xs font-semibold">
                  Popular Stay
                </span>
              )}
            </div>

            {/* Description Card */}
            <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-xs">
              <h2 className="text-lg font-bold text-slate-900 mb-3">About the Property</h2>
              <p className="text-slate-600 leading-relaxed text-sm sm:text-base">
                {description}
              </p>

              {/* Amenities Section */}
              <div className="mt-8 pt-6 border-t border-slate-100">
                <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-4 flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-indigo-600" />
                  Included Amenities & Perks
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {amenitiesList.map((amenity, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2.5 p-3 rounded-xl bg-slate-50 border border-slate-100 text-sm font-medium text-slate-700"
                    >
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                      <span>{amenity}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Right Column: Pricing Breakdown & Booking Form */}
          <div className="space-y-6">
            {/* Stay Dates Card */}
            <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs">
              <h3 className="text-base font-bold text-slate-900 mb-4 flex items-center gap-2">
                <Calendar className="w-4 h-4 text-indigo-600" />
                Select Your Stay Dates
              </h3>

              <div className="space-y-3">
                <div>
                  <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1 block">
                    Check-in Date
                  </label>
                  <input
                    type="date"
                    min={today}
                    value={checkIn}
                    onChange={(e) => {
                      const newIn = e.target.value;
                      let newOut = checkOut;
                      if (checkOut && newIn >= checkOut) {
                        const d = new Date(newIn);
                        d.setDate(d.getDate() + 1);
                        newOut = d.toISOString().split('T')[0];
                      }
                      handleDateChange(newIn, newOut);
                    }}
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:outline-hidden text-sm font-medium"
                  />
                </div>

                <div>
                  <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1 block">
                    Check-out Date
                  </label>
                  <input
                    type="date"
                    min={minCheckOutDate}
                    value={checkOut}
                    onChange={(e) => handleDateChange(checkIn, e.target.value)}
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:outline-hidden text-sm font-medium"
                  />
                </div>
              </div>
            </div>

            {/* Backend-Calculated Price Breakdown Card */}
            <div className="bg-gradient-to-br from-indigo-900 to-slate-900 rounded-3xl p-6 text-white shadow-lg">
              <div className="flex items-center justify-between pb-3 border-b border-indigo-800/80 mb-4">
                <h3 className="text-sm font-bold uppercase tracking-wider text-indigo-200 flex items-center gap-1.5">
                  <CreditCard className="w-4 h-4 text-amber-400" />
                  Live Price Breakdown
                </h3>
                <span className="text-xs bg-indigo-800 text-indigo-200 px-2.5 py-0.5 rounded-full font-semibold">
                  INR (₹)
                </span>
              </div>

              {priceBreakdown ? (
                <div className="space-y-3">
                  <div className="flex justify-between items-center text-sm text-indigo-100">
                    <span>
                      ₹{Number(priceBreakdown.pricePerNight).toLocaleString('en-IN')} × {priceBreakdown.nights} {priceBreakdown.nights === 1 ? 'night' : 'nights'}
                    </span>
                    <span>₹{Number(priceBreakdown.baseAmount).toLocaleString('en-IN')}</span>
                  </div>

                  {priceBreakdown.discountApplied && (
                    <div className="flex justify-between items-center text-sm text-emerald-300 bg-emerald-950/60 p-2.5 rounded-xl border border-emerald-500/30">
                      <div className="flex items-center gap-1.5">
                        <Tag className="w-4 h-4 text-emerald-400" />
                        <span>Smart Stay Discount (10% off)</span>
                      </div>
                      <span className="font-bold">
                        - ₹{Number(priceBreakdown.discountAmount).toLocaleString('en-IN')}
                      </span>
                    </div>
                  )}

                  <div className="pt-3 border-t border-indigo-800 flex justify-between items-baseline">
                    <div>
                      <div className="text-xs text-indigo-300 font-medium">Total Payable Amount</div>
                      <div className="text-xs text-indigo-400">Taxes & fees included</div>
                    </div>
                    <div className="text-2xl font-extrabold text-amber-300">
                      ₹{Number(priceBreakdown.totalAmount).toLocaleString('en-IN')}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-6 text-indigo-200 text-sm">
                  <Clock className="w-8 h-8 mx-auto text-indigo-400 mb-2 opacity-80" />
                  <p>Select both check-in and check-out dates above to view live pricing calculation.</p>
                </div>
              )}
            </div>

            {/* Guest Booking Form Card */}
            <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs">
              <h3 className="text-base font-bold text-slate-900 mb-1">Confirm Your Reservation</h3>
              <p className="text-xs text-slate-500 mb-5">
                Enter your details to reserve 1 room at this property.
              </p>

              {/* General Booking Error Banner */}
              {bookingErrors.general && (
                <div className="p-3.5 mb-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2">
                  <AlertCircle className="w-4 h-4 text-red-600 mt-0.5 shrink-0" />
                  <div>
                    <span className="font-bold">Booking Error: </span>
                    {bookingErrors.general}
                  </div>
                </div>
              )}

              <form onSubmit={handleBookingSubmit} className="space-y-4">
                {/* Guest Name */}
                <div>
                  <label className="text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1 block">
                    Full Name
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      placeholder="e.g. Rahul Sharma"
                      value={guestName}
                      onChange={(e) => setGuestName(e.target.value)}
                      required
                      className={`w-full pl-9 pr-3.5 py-2.5 rounded-xl border text-sm font-medium focus:outline-hidden ${
                        bookingErrors.fieldErrors?.guestName
                          ? 'border-red-400 focus:ring-2 focus:ring-red-400 bg-red-50/30'
                          : 'border-slate-300 focus:ring-2 focus:ring-indigo-500'
                      }`}
                    />
                    <User className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  </div>
                  {bookingErrors.fieldErrors?.guestName && (
                    <p className="mt-1 text-xs text-red-600 font-medium">
                      {bookingErrors.fieldErrors.guestName}
                    </p>
                  )}
                </div>

                {/* Guest Email */}
                <div>
                  <label className="text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1 block">
                    Email Address
                  </label>
                  <div className="relative">
                    <input
                      type="email"
                      placeholder="e.g. rahul@example.com"
                      value={guestEmail}
                      onChange={(e) => setGuestEmail(e.target.value)}
                      required
                      className={`w-full pl-9 pr-3.5 py-2.5 rounded-xl border text-sm font-medium focus:outline-hidden ${
                        bookingErrors.fieldErrors?.guestEmail
                          ? 'border-red-400 focus:ring-2 focus:ring-red-400 bg-red-50/30'
                          : 'border-slate-300 focus:ring-2 focus:ring-indigo-500'
                      }`}
                    />
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  </div>
                  {bookingErrors.fieldErrors?.guestEmail && (
                    <p className="mt-1 text-xs text-red-600 font-medium">
                      {bookingErrors.fieldErrors.guestEmail}
                    </p>
                  )}
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={submitting || isSoldOut || !checkIn || !checkOut}
                  className={`w-full py-3.5 px-4 rounded-xl text-sm font-bold transition-all shadow-md flex items-center justify-center gap-2 ${
                    isSoldOut
                      ? 'bg-slate-200 text-slate-400 cursor-not-allowed shadow-none'
                      : submitting
                      ? 'bg-indigo-400 text-white cursor-wait'
                      : 'bg-indigo-600 hover:bg-indigo-700 active:scale-98 text-white shadow-indigo-200'
                  }`}
                >
                  {submitting ? (
                    <span>Processing Reservation...</span>
                  ) : isSoldOut ? (
                    <span>Sold Out for Selected Dates</span>
                  ) : !checkIn || !checkOut ? (
                    <span>Select Dates to Book</span>
                  ) : (
                    <span>Confirm & Book Room</span>
                  )}
                </button>

                <p className="text-center text-xs text-slate-400 mt-2">
                  Instant confirmation • Free cancellation anytime
                </p>
              </form>
            </div>
          </div>
        </div>
      </div>

      {/* Confirmation View / Modal */}
      {confirmedBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl border border-slate-100 overflow-hidden relative">
            {/* Header Badge */}
            <div className="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto mb-4">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <div className="text-center">
              <span className="text-xs font-bold uppercase tracking-widest text-emerald-600 bg-emerald-50 px-3 py-1 rounded-full border border-emerald-200">
                Booking Confirmed
              </span>
              <h3 className="text-2xl font-extrabold text-slate-900 mt-2">
                You're Going to {hotel.name}!
              </h3>
              <p className="text-xs text-slate-500 mt-1">
                A confirmation has been recorded for {confirmedBooking.guestEmail}.
              </p>
            </div>

            {/* Unique Reference Card */}
            <div className="my-6 p-4 rounded-2xl bg-indigo-50 border border-indigo-100 text-center">
              <div className="text-xs font-semibold uppercase tracking-wider text-indigo-700 mb-1">
                Your Booking Reference
              </div>
              <div className="text-2xl font-black text-indigo-950 tracking-wider">
                {confirmedBooking.bookingReference}
              </div>

              <button
                type="button"
                onClick={handleCopyReference}
                className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white border border-indigo-200 text-indigo-700 text-xs font-semibold hover:bg-indigo-100 transition-colors shadow-2xs"
              >
                {copied ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-600" />
                    <span>Copied to Clipboard!</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5 text-indigo-600" />
                    <span>Copy Reference Code</span>
                  </>
                )}
              </button>
            </div>

            {/* Stay Details Summary */}
            <div className="space-y-2 text-xs border-t border-b border-slate-100 py-3.5 mb-6 text-slate-600">
              <div className="flex justify-between">
                <span className="text-slate-400">Hotel:</span>
                <span className="font-semibold text-slate-800">{hotel.name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Guest Name:</span>
                <span className="font-semibold text-slate-800">{confirmedBooking.guestName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Stay Dates:</span>
                <span className="font-semibold text-slate-800">
                  {confirmedBooking.checkInDate} to {confirmedBooking.checkOutDate} ({confirmedBooking.nights} nights)
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Total Paid:</span>
                <span className="font-bold text-indigo-900">
                  ₹{Number(confirmedBooking.totalAmount).toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-2.5">
              <Link
                to={`/manage-booking?reference=${confirmedBooking.bookingReference}&email=${encodeURIComponent(confirmedBooking.guestEmail)}`}
                className="flex-1 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-center text-xs shadow-sm flex items-center justify-center gap-1.5 transition-colors"
              >
                <span>Manage This Booking</span>
                <ExternalLink className="w-3.5 h-3.5" />
              </Link>
              <button
                type="button"
                onClick={() => navigate('/')}
                className="py-3 px-4 rounded-xl border border-slate-300 hover:bg-slate-100 text-slate-700 font-semibold text-xs transition-colors"
              >
                Back to Hotels
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
