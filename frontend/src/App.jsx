import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import HotelDetailPage from './pages/HotelDetailPage';
import ManageBookingPage from './pages/ManageBookingPage';

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
        <Navbar />
        <div className="flex-1">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/hotels/:id" element={<HotelDetailPage />} />
            <Route path="/manage-booking" element={<ManageBookingPage />} />
          </Routes>
        </div>
        <footer className="bg-white border-t border-slate-200 py-6 text-center text-xs text-slate-500">
          <p>© {new Date().getFullYear()} StayHaven Hotel Booking Portal. All rights reserved.</p>
        </footer>
      </div>
    </BrowserRouter>
  );
}
