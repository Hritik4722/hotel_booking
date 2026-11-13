import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';

// Lightweight placeholders for upcoming milestones
function HotelDetailsPlaceholder() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-16 text-center">
      <h2 className="text-2xl font-bold text-slate-800">Hotel Details & Booking</h2>
      <p className="text-slate-600 mt-2">
        Full details, pricing breakdown, and booking confirmation flow will be implemented in Milestone 7.
      </p>
      <Link to="/" className="inline-block mt-4 px-4 py-2 bg-indigo-600 text-white rounded-lg font-medium text-sm">
        Back to Hotels
      </Link>
    </div>
  );
}

function ManageBookingPlaceholder() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-16 text-center">
      <h2 className="text-2xl font-bold text-slate-800">Manage Your Booking</h2>
      <p className="text-slate-600 mt-2">
        Booking retrieval and cancellation management will be implemented in Milestone 8.
      </p>
      <Link to="/" className="inline-block mt-4 px-4 py-2 bg-indigo-600 text-white rounded-lg font-medium text-sm">
        Back to Hotels
      </Link>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
        <Navbar />
        <div className="flex-1">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/hotels/:id" element={<HotelDetailsPlaceholder />} />
            <Route path="/manage-booking" element={<ManageBookingPlaceholder />} />
          </Routes>
        </div>
        <footer className="bg-white border-t border-slate-200 py-6 text-center text-xs text-slate-500">
          <p>© {new Date().getFullYear()} StayHaven Hotel Booking Portal. All rights reserved.</p>
        </footer>
      </div>
    </BrowserRouter>
  );
}
