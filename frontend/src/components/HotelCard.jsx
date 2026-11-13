import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Users, Sparkles, AlertCircle, ArrowRight, CheckCircle2 } from 'lucide-react';

// Curated high quality hotel images keyed by city/hotel
const HOTEL_IMAGES = {
  1: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80', // Udaipur Palace
  2: 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80', // Goa Coastal
  3: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80', // Jaipur Haveli
  4: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80', // Manali Alpine
  5: 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80', // Mumbai Boulevard
  6: 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=800&q=80', // Bengaluru TechCity
};

export default function HotelCard({ hotel, checkIn = '', checkOut = '' }) {
  const {
    id,
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

  // Parse amenities from comma-separated string to badges
  const amenitiesList = amenities
    ? amenities.split(',').map((a) => a.trim()).slice(0, 4)
    : [];

  // Construct query params for detail page navigation
  const queryParams = new URLSearchParams();
  if (checkIn) queryParams.append('checkIn', checkIn);
  if (checkOut) queryParams.append('checkOut', checkOut);
  const detailUrl = `/hotels/${id}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;

  const imageUrl = HOTEL_IMAGES[id] || HOTEL_IMAGES[1];

  return (
    <article className="group bg-white rounded-2xl overflow-hidden border border-slate-200/80 shadow-xs hover:shadow-xl hover:border-slate-300 transition-all duration-300 flex flex-col justify-between">
      <div>
        {/* Hotel Image & Status Badges */}
        <div className="relative h-52 sm:h-56 overflow-hidden bg-slate-100">
          <img
            src={imageUrl}
            alt={name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            loading="lazy"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-80" />

          {/* Location Badge */}
          <div className="absolute bottom-3 left-3 flex items-center gap-1.5 px-3 py-1 rounded-full bg-black/50 backdrop-blur-md text-white text-xs font-medium">
            <MapPin className="w-3.5 h-3.5 text-amber-400" />
            <span>{location}, India</span>
          </div>

          {/* Demand & Availability Badges */}
          <div className="absolute top-3 right-3 flex flex-col items-end gap-1.5">
            {isHighDemand && (
              <span className="flex items-center gap-1 px-3 py-1 rounded-full bg-amber-500/95 backdrop-blur-md text-white text-xs font-bold shadow-md animate-pulse">
                <AlertCircle className="w-3.5 h-3.5" />
                High Demand: Only 1 room left!
              </span>
            )}
            {isSoldOut && (
              <span className="flex items-center gap-1 px-3 py-1 rounded-full bg-red-600/95 backdrop-blur-md text-white text-xs font-bold shadow-md">
                Sold Out
              </span>
            )}
            {!isSoldOut && !isHighDemand && (
              <span className="flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-600/90 backdrop-blur-md text-white text-xs font-semibold shadow-xs">
                <CheckCircle2 className="w-3.5 h-3.5" />
                {availableRooms} rooms available
              </span>
            )}
          </div>

          {/* Smart Stay Long-Stay Discount Badge */}
          {priceBreakdown?.discountApplied && (
            <div className="absolute top-3 left-3 flex items-center gap-1 px-3 py-1 rounded-full bg-indigo-600/95 backdrop-blur-md text-white text-xs font-semibold shadow-md">
              <Sparkles className="w-3.5 h-3.5 text-amber-300" />
              10% Long-Stay Discount
            </div>
          )}
        </div>

        {/* Content Body */}
        <div className="p-5">
          <h3 className="text-lg font-bold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-1">
            {name}
          </h3>

          <p className="mt-2 text-sm text-slate-600 line-clamp-2 leading-relaxed">
            {description}
          </p>

          {/* Amenities Badges */}
          <div className="mt-4 flex flex-wrap gap-1.5">
            {amenitiesList.map((amenity, index) => (
              <span
                key={index}
                className="px-2.5 py-0.5 rounded-md bg-slate-100 text-slate-700 text-xs font-medium border border-slate-200/60"
              >
                {amenity}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Footer: Price & CTA */}
      <div className="px-5 pb-5 pt-3 border-t border-slate-100 flex items-center justify-between gap-3">
        <div>
          <div className="text-xs text-slate-500 font-medium">Starting from</div>
          <div className="flex items-baseline gap-1">
            <span className="text-xl font-extrabold text-slate-900">
              ₹{Number(pricePerNight).toLocaleString('en-IN')}
            </span>
            <span className="text-xs text-slate-500">/ night</span>
          </div>

          {priceBreakdown && (
            <div className="text-xs text-indigo-700 font-semibold mt-0.5">
              ₹{Number(priceBreakdown.totalAmount).toLocaleString('en-IN')} total for {priceBreakdown.nights} nights
            </div>
          )}
        </div>

        {isSoldOut ? (
          <button
            disabled
            className="px-4 py-2.5 rounded-xl bg-slate-100 text-slate-400 text-sm font-semibold cursor-not-allowed border border-slate-200"
          >
            Sold Out
          </button>
        ) : (
          <Link
            to={detailUrl}
            className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-95 text-white text-sm font-semibold shadow-sm transition-all"
          >
            <span>Book Now</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        )}
      </div>
    </article>
  );
}
