import React, { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, Calendar, MapPin, Sparkles, RefreshCw, AlertCircle, Hotel, X } from 'lucide-react';
import HotelCard from '../components/HotelCard';
import { searchHotels } from '../services/api';

const POPULAR_DESTINATIONS = ['All', 'Goa', 'Mumbai', 'Jaipur', 'Manali', 'Bengaluru', 'Udaipur'];

export default function HomePage() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Get initial values from URL query params or defaults
  const initialLocation = searchParams.get('location') || '';
  const initialCheckIn = searchParams.get('checkIn') || '';
  const initialCheckOut = searchParams.get('checkOut') || '';

  const [location, setLocation] = useState(initialLocation);
  const [checkIn, setCheckIn] = useState(initialCheckIn);
  const [checkOut, setCheckOut] = useState(initialCheckOut);

  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Prevent selecting dates in the past
  const today = useMemo(() => new Date().toISOString().split('T')[0], []);

  // Compute minimum check-out date (at least 1 day after check-in, or tomorrow)
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

  // Compute stay duration if both dates selected
  const stayNights = useMemo(() => {
    if (checkIn && checkOut) {
      const inDate = new Date(checkIn);
      const outDate = new Date(checkOut);
      const diffTime = outDate - inDate;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays > 0 ? diffDays : 0;
    }
    return 0;
  }, [checkIn, checkOut]);

  // Fetch hotels matching criteria
  const fetchHotelData = async (loc, cIn, cOut) => {
    setLoading(true);
    setError(null);
    try {
      const data = await searchHotels(loc, cIn, cOut);
      setHotels(data);
    } catch (err) {
      setError(err.message || 'Failed to load hotels. Please make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHotelData(initialLocation, initialCheckIn, initialCheckOut);
  }, []);

  const handleSearchSubmit = (e) => {
    if (e) e.preventDefault();

    // If checkIn is set but checkOut is missing (or vice-versa), remind the user
    if ((checkIn && !checkOut) || (!checkIn && checkOut)) {
      setError('Please select both Check-in and Check-out dates to calculate stay availability.');
      return;
    }

    // Update URL query parameters
    const params = {};
    if (location.trim()) params.location = location.trim();
    if (checkIn) params.checkIn = checkIn;
    if (checkOut) params.checkOut = checkOut;
    setSearchParams(params);

    fetchHotelData(location, checkIn, checkOut);
  };

  const handleResetFilters = () => {
    setLocation('');
    setCheckIn('');
    setCheckOut('');
    setSearchParams({});
    fetchHotelData('', '', '');
  };

  const handleQuickCitySelect = (city) => {
    const selectedCity = city === 'All' ? '' : city;
    setLocation(selectedCity);
    const params = {};
    if (selectedCity) params.location = selectedCity;
    if (checkIn) params.checkIn = checkIn;
    if (checkOut) params.checkOut = checkOut;
    setSearchParams(params);
    fetchHotelData(selectedCity, checkIn, checkOut);
  };

  return (
    <div className="min-h-screen pb-16">
      {/* Hero Banner Section */}
      <section className="relative bg-gradient-to-br from-indigo-900 via-indigo-800 to-slate-900 text-white pt-12 pb-24 px-4 sm:px-6 lg:px-8 overflow-hidden">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#ffffff_1px,transparent_1px)] [background-size:16px_16px]" />

        <div className="max-w-5xl mx-auto text-center relative z-10">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-200 border border-indigo-400/30 text-xs font-semibold mb-4 backdrop-blur-xs">
            <Sparkles className="w-3.5 h-3.5 text-amber-300" />
            Smart Stay Pricing: 10% Off on Stays of 4+ Nights
          </span>

          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight">
            Discover Exceptional Stays Across India
          </h1>
          <p className="mt-3 text-base sm:text-lg text-indigo-100 max-w-2xl mx-auto font-normal">
            Browse verified luxury palaces, coastal resorts, and alpine retreats with live room availability.
          </p>
        </div>
      </section>

      {/* Interactive Search Bar (Floating Container) */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 -mt-14 relative z-20">
        <div className="bg-white rounded-2xl shadow-xl border border-slate-200/80 p-4 sm:p-6 backdrop-blur-md">
          <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {/* Location Input */}
            <div className="flex flex-col">
              <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5 flex items-center gap-1">
                <MapPin className="w-3.5 h-3.5 text-indigo-600" />
                Destination / City
              </label>
              <div className="relative">
                <input
                  type="text"
                  placeholder="e.g. Goa, Jaipur, Mumbai..."
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  className="w-full pl-3.5 pr-8 py-2.5 rounded-xl border border-slate-300 focus:outline-hidden focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm font-medium text-slate-800 placeholder-slate-400"
                />
                {location && (
                  <button
                    type="button"
                    onClick={() => setLocation('')}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  >
                    <X className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>

            {/* Check-In Date */}
            <div className="flex flex-col">
              <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-indigo-600" />
                Check-in Date
              </label>
              <input
                type="date"
                min={today}
                value={checkIn}
                onChange={(e) => {
                  setCheckIn(e.target.value);
                  // If checkOut is earlier than new checkIn, update checkOut
                  if (checkOut && e.target.value >= checkOut) {
                    const nextDay = new Date(e.target.value);
                    nextDay.setDate(nextDay.getDate() + 1);
                    setCheckOut(nextDay.toISOString().split('T')[0]);
                  }
                }}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-hidden focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm font-medium text-slate-800"
              />
            </div>

            {/* Check-Out Date */}
            <div className="flex flex-col">
              <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-indigo-600" />
                Check-out Date
              </label>
              <input
                type="date"
                min={minCheckOutDate}
                value={checkOut}
                onChange={(e) => setCheckOut(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-hidden focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm font-medium text-slate-800"
              />
            </div>

            {/* Search & Reset Buttons */}
            <div className="flex items-end gap-2">
              <button
                type="submit"
                className="flex-1 flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-95 text-white text-sm font-bold shadow-md shadow-indigo-100 transition-all"
              >
                <Search className="w-4 h-4" />
                <span>Search</span>
              </button>

              {(location || checkIn || checkOut) && (
                <button
                  type="button"
                  onClick={handleResetFilters}
                  title="Reset Filters"
                  className="px-3 py-2.5 rounded-xl border border-slate-300 hover:bg-slate-100 text-slate-600 transition-colors"
                >
                  <RefreshCw className="w-4 h-4" />
                </button>
              )}
            </div>
          </form>

          {/* Quick Destination Pill Filters */}
          <div className="mt-4 pt-4 border-t border-slate-100 flex items-center gap-2 overflow-x-auto pb-1 text-xs">
            <span className="text-slate-400 font-semibold whitespace-nowrap">Popular Destinations:</span>
            {POPULAR_DESTINATIONS.map((city) => {
              const isSelected = (city === 'All' && !location) || location.toLowerCase() === city.toLowerCase();
              return (
                <button
                  key={city}
                  type="button"
                  onClick={() => handleQuickCitySelect(city)}
                  className={`px-3 py-1 rounded-full whitespace-nowrap font-medium transition-colors ${
                    isSelected
                      ? 'bg-indigo-600 text-white'
                      : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                  }`}
                >
                  {city}
                </button>
              );
            })}
          </div>

          {/* Smart Stay Long-Stay Bonus Announcement Banner */}
          {stayNights >= 4 && (
            <div className="mt-3 p-2.5 rounded-xl bg-gradient-to-r from-emerald-500/10 via-indigo-500/10 to-blue-500/10 border border-emerald-500/30 flex items-center gap-2 text-xs text-emerald-800 font-semibold">
              <Sparkles className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>
                Smart Stay Offer Applied: You selected <strong>{stayNights} nights</strong>. Enjoy an automatic <strong>10% discount</strong> on your booking!
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Main Content: Hotel Listing Grid */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12">
        {/* Section Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 mb-8 gap-2">
          <div>
            <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
              <Hotel className="w-6 h-6 text-indigo-600" />
              {location ? `Hotels in ${location}` : 'All Featured Hotels'}
            </h2>
            <p className="text-sm text-slate-500 mt-0.5">
              {checkIn && checkOut
                ? `Showing room availability and pricing for ${checkIn} to ${checkOut}`
                : 'Displaying standard rates. Select dates above for live room inventory.'}
            </p>
          </div>

          {!loading && (
            <div className="text-sm font-semibold text-slate-600 bg-slate-100 px-3 py-1.5 rounded-lg self-start sm:self-auto">
              {hotels.length} {hotels.length === 1 ? 'property' : 'properties'} found
            </div>
          )}
        </div>

        {/* Error State */}
        {error && (
          <div className="p-4 mb-8 rounded-2xl bg-red-50 border border-red-200 text-red-700 flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-600 mt-0.5 shrink-0" />
            <div className="flex-1">
              <h4 className="font-bold text-sm">Notice</h4>
              <p className="text-sm mt-0.5">{error}</p>
            </div>
            <button
              onClick={() => fetchHotelData(location, checkIn, checkOut)}
              className="px-3 py-1 bg-red-600 text-white rounded-lg text-xs font-semibold hover:bg-red-700 transition-colors"
            >
              Retry
            </button>
          </div>
        )}

        {/* Loading State Skeletons */}
        {loading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((idx) => (
              <div
                key={idx}
                className="bg-white rounded-2xl overflow-hidden border border-slate-200 animate-pulse h-96 flex flex-col justify-between p-4"
              >
                <div className="h-48 bg-slate-200 rounded-xl mb-4" />
                <div className="h-5 bg-slate-200 rounded-md w-3/4 mb-2" />
                <div className="h-4 bg-slate-200 rounded-md w-full mb-2" />
                <div className="h-4 bg-slate-200 rounded-md w-2/3 mb-4" />
                <div className="h-10 bg-slate-200 rounded-xl" />
              </div>
            ))}
          </div>
        )}

        {/* Empty Search Results State */}
        {!loading && !error && hotels.length === 0 && (
          <div className="text-center py-16 bg-white rounded-2xl border border-slate-200 p-8 shadow-xs">
            <div className="w-16 h-16 mx-auto rounded-full bg-slate-100 flex items-center justify-center text-slate-400 mb-4">
              <Search className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-slate-800">No hotels found</h3>
            <p className="text-sm text-slate-500 mt-1 max-w-md mx-auto">
              We couldn't find any hotels matching "{location}". Try checking the spelling or selecting another destination.
            </p>
            <button
              onClick={handleResetFilters}
              className="mt-5 px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold transition-colors"
            >
              View All Hotels
            </button>
          </div>
        )}

        {/* Hotel Cards Grid */}
        {!loading && !error && hotels.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {hotels.map((hotel) => (
              <HotelCard
                key={hotel.id}
                hotel={hotel}
                checkIn={checkIn}
                checkOut={checkOut}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
