const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Helper to handle fetch responses and standardize error messages.
 */
async function handleResponse(response) {
  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const error = new Error(
      data?.message || (typeof data === 'string' ? data : 'An unexpected error occurred')
    );
    error.status = response.status;
    error.fieldErrors = data?.fieldErrors || null;
    throw error;
  }

  return data;
}

/**
 * Searches hotels with optional location, check-in, and check-out filters.
 */
export async function searchHotels(location = '', checkIn = '', checkOut = '') {
  const params = new URLSearchParams();
  if (location && location.trim()) params.append('location', location.trim());
  if (checkIn) params.append('checkIn', checkIn);
  if (checkOut) params.append('checkOut', checkOut);

  const queryString = params.toString();
  const url = `${API_BASE_URL}/hotels${queryString ? `?${queryString}` : ''}`;
  const response = await fetch(url);
  return handleResponse(response);
}

/**
 * Retrieves full details and pricing breakdown for a specific hotel.
 */
export async function getHotelById(id, checkIn = '', checkOut = '') {
  const params = new URLSearchParams();
  if (checkIn) params.append('checkIn', checkIn);
  if (checkOut) params.append('checkOut', checkOut);

  const queryString = params.toString();
  const url = `${API_BASE_URL}/hotels/${id}${queryString ? `?${queryString}` : ''}`;
  const response = await fetch(url);
  return handleResponse(response);
}

/**
 * Creates a new booking.
 */
export async function createBooking(bookingData) {
  const response = await fetch(`${API_BASE_URL}/bookings`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(bookingData),
  });
  return handleResponse(response);
}

/**
 * Retrieves an existing booking by booking reference and guest email.
 */
export async function lookupBooking(reference, email) {
  const params = new URLSearchParams({
    reference: reference.trim(),
    email: email.trim(),
  });
  const response = await fetch(`${API_BASE_URL}/bookings/lookup?${params.toString()}`);
  return handleResponse(response);
}

/**
 * Cancels a booking after verifying guest email via JSON request body.
 */
export async function cancelBooking(reference, email) {
  const response = await fetch(`${API_BASE_URL}/bookings/${encodeURIComponent(reference.trim())}/cancel`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: email.trim() }),
  });
  return handleResponse(response);
}
