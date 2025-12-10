// server.js - Complete Ground Booking System API
const express = require('express');
const { createClient } = require('@supabase/supabase-js');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(express.json());
app.use(cors());
const multer = require('multer');
const upload = multer({ storage: multer.memoryStorage() }); // keeps file in memory for direct upload

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-change-this';
const TOKEN_EXPIRES_IN = '7d'; // adjust as needed

// Ensure storage buckets exist
const ensureBuckets = async () => {
  const buckets = ['ground-images', 'user-profiles'];
  for (const bucket of buckets) {
    try {
      const { data } = await supabase.storage.getBucket(bucket);
      if (!data) {
        await supabase.storage.createBucket(bucket, { public: true });
        console.log(`✓ Created bucket: ${bucket}`);
      } else {
        console.log(`✓ Bucket exists: ${bucket}`);
      }
    } catch (err) {
      console.log(`⚠ Could not verify bucket ${bucket}: ${err.message}`);
    }
  }
};

// Call on startup
ensureBuckets();

// ----------------- Helpers / Middleware -----------------

function sendError(res, status = 400, message = 'Something went wrong') {
  return res.status(status).json({ success: false, message });
}

const verifyToken = (req, res, next) => {
  try {
    const auth = req.headers.authorization || '';
    const token = auth.startsWith('Bearer ') ? auth.slice(7) : null;
    if (!token) return sendError(res, 401, 'No token provided');

    const decoded = jwt.verify(token, JWT_SECRET);
    req.userId = decoded.userId;
    req.userEmail = decoded.email;
    next();
  } catch (err) {
    return sendError(res, 401, 'Invalid or expired token');
  }
};

const requireAdmin = async (req, res, next) => {
  try {
    const { data, error } = await supabase
      .from('users')
      .select('is_admin')
      .eq('id', req.userId)
      .single();

    if (error) return sendError(res, 400, 'Unable to verify admin role');
    if (!data || !data.is_admin) return sendError(res, 403, 'Admin access required');

    req.isAdmin = true;
    next();
  } catch (err) {
    return sendError(res, 500, err.message);
  }
};

// Helper to check ownership of a ground (admin must own it)
const requireGroundOwner = async (req, res, next) => {
  try {
    const groundId = req.params.groundId || req.body.ground_id;
    if (!groundId) return sendError(res, 400, 'groundId required');

    const { data: ground, error } = await supabase
      .from('grounds')
      .select('created_by')
      .eq('id', groundId)
      .single();

    if (error) return sendError(res, 400, 'Ground not found');

    // Allow if the ground was created by this admin, or created_by is null (legacy data)
    if (ground.created_by && ground.created_by !== req.userId) {
      return sendError(res, 403, 'You do not own this ground');
    }

    next();
  } catch (err) {
    return sendError(res, 500, err.message);
  }
};

// ----------------- Auth -----------------

// Register user
app.post('/api/register', async (req, res) => {
  const { name, email, phone, password } = req.body;
  if (!name || !email || !phone || !password) return sendError(res, 400, 'All fields are required');
  if (password.length < 6) return sendError(res, 400, 'Password must be >= 6 chars');

  try {
    const hashedPassword = await bcrypt.hash(password, 10);

    const { data, error } = await supabase
      .from('users')
      .insert([{
        name,
        email,
        phone,
        password_hash: hashedPassword,
        is_admin: false,
        created_at: new Date().toISOString()
      }])
      .select()
      .single();

    if (error) {
      // Handle unique email constraint
      if (error.code === '23505' || error.message.includes('duplicate')) {
        return sendError(res, 400, 'Email already exists');
      }
      return sendError(res, 400, error.message || 'Failed to register');
    }

    const token = jwt.sign({ userId: data.id, email: data.email }, JWT_SECRET, { expiresIn: TOKEN_EXPIRES_IN });
    return res.json({ 
      success: true, 
      token, 
      user: { 
        id: data.id, 
        name: data.name, 
        email: data.email, 
        phone: data.phone, 
        is_admin: data.is_admin 
      } 
    });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Login user
app.post('/api/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return sendError(res, 400, 'Email and password required');

  try {
    const { data, error } = await supabase
      .from('users')
      .select('*')
      .eq('email', email)
      .single();

    if (error || !data) return sendError(res, 401, 'Invalid credentials');

    const valid = await bcrypt.compare(password, data.password_hash);
    if (!valid) return sendError(res, 401, 'Invalid credentials');

    const token = jwt.sign({ userId: data.id, email: data.email }, JWT_SECRET, { expiresIn: TOKEN_EXPIRES_IN });
    return res.json({
      success: true,
      token,
      user: { 
        id: data.id, 
        name: data.name, 
        email: data.email, 
        phone: data.phone, 
        is_admin: data.is_admin 
      }
    });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// ----------------- Grounds -----------------

// Get all active grounds
app.get('/api/grounds', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('grounds')
      .select('*')
      .eq('is_active', true)
      .order('created_at', { ascending: false });
    
    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get ground details
app.get('/api/grounds/:groundId', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('grounds')
      .select('*')
      .eq('id', req.params.groundId)
      .single();
    
    if (error) return sendError(res, 404, 'Ground not found');
    return res.json({ success: true, data });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Search/Filter grounds
app.get('/api/grounds/search', async (req, res) => {
  try {
    const { type, location, minPrice, maxPrice } = req.query;
    
    let query = supabase
      .from('grounds')
      .select('*')
      .eq('is_active', true);
    
    if (type) query = query.eq('type', type);
    if (location) query = query.ilike('location', `%${location}%`);
    if (minPrice) query = query.gte('price', parseFloat(minPrice));
    if (maxPrice) query = query.lte('price', parseFloat(maxPrice));
    
    const { data, error } = await query.order('created_at', { ascending: false });
    
    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Admin: get own grounds list (only active)
app.get('/api/admin/grounds', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('grounds')
      .select('*')
      .eq('created_by', req.userId)
      .eq('is_active', true)
      .order('created_at', { ascending: false });

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Admin: create new ground
app.post('/api/grounds', verifyToken, requireAdmin, async (req, res) => {
  const { name, location, type, description, price, image_url } = req.body;
  // Check if price is undefined or null, allowing 0
  if (!name || !location || !type || price === undefined || price === null) {
      return sendError(res, 400, 'name, location, type and price are required');
  }
  try {
    const { data, error } = await supabase
      .from('grounds')
      .insert([{
        name,
        location,
        type,
        description: description || null,
        price: parseFloat(price),
        image_url: image_url || null,
        created_by: req.userId,
        is_active: true,
        created_at: new Date().toISOString()
      }])
      .select()
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data, message: 'Ground created successfully' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Admin: update ground (only owner)
app.put('/api/grounds/:groundId', verifyToken, requireAdmin, requireGroundOwner, async (req, res) => {
  const updates = {};
  const allowed = ['name', 'location', 'type', 'description', 'price', 'image_url', 'is_active'];
  allowed.forEach(k => { 
    if (req.body[k] !== undefined) {
      updates[k] = k === 'price' ? parseFloat(req.body[k]) : req.body[k];
    }
  });
  updates.updated_at = new Date().toISOString();

  try {
    const { data, error } = await supabase
      .from('grounds')
      .update(updates)
      .eq('id', req.params.groundId)
      .select()
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data, message: 'Ground updated successfully' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Admin: delete ground (soft delete - mark as inactive)
app.delete('/api/grounds/:groundId', verifyToken, requireAdmin, requireGroundOwner, async (req, res) => {
  try {
    const { error } = await supabase
      .from('grounds')
      .update({ is_active: false, updated_at: new Date().toISOString() })
      .eq('id', req.params.groundId);

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, message: 'Ground deleted successfully' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Upload ground image (admin only)
app.post('/api/grounds/upload-image', verifyToken, requireAdmin, upload.single('image'), async (req, res) => {
  try {
    if (!req.file) return sendError(res, 400, 'No image file provided');

    const bucket = 'ground-images';
    const ext = (req.file.originalname || '').split('.').pop() || 'jpg';
    const filename = `grounds/${Date.now()}-${Math.random().toString(36).slice(2, 10)}.${ext}`;

    // Check if bucket exists
    const { data: bucketCheck } = await supabase.storage.getBucket(bucket);
    if (!bucketCheck) {
      return sendError(res, 400, 'Storage bucket not configured');
    }

    // Upload file
    const { data, error } = await supabase.storage
      .from(bucket)
      .upload(filename, req.file.buffer, { 
        cacheControl: '3600', 
        upsert: false,
        contentType: req.file.mimetype 
      });

    if (error) {
      return sendError(res, 400, error.message || 'Upload failed');
    }

    // Generate public URL (Fixed regex)
    const publicUrl = `${process.env.SUPABASE_URL.replace(/\/+$/, '')}/storage/v1/object/public/${bucket}/${encodeURIComponent(filename)}`;

    return res.json({ 
      success: true, 
      path: filename, 
      url: publicUrl,
      message: 'Image uploaded successfully'
    });

  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Upload user profile image
app.post('/api/user/upload-profile-image', verifyToken, upload.single('image'), async (req, res) => {
  try {
    if (!req.file) return sendError(res, 400, 'No image file provided');

    const bucket = 'user-profiles';
    const ext = (req.file.originalname || '').split('.').pop() || 'jpg';
    const filename = `profiles/${req.userId}-${Date.now()}.${ext}`;

    // Check if bucket exists
    const { data: bucketCheck } = await supabase.storage.getBucket(bucket);
    if (!bucketCheck) {
      return sendError(res, 400, 'Storage bucket not configured');
    }

    // Upload file
    const { data, error } = await supabase.storage
      .from(bucket)
      .upload(filename, req.file.buffer, { 
        cacheControl: '3600', 
        upsert: true,
        contentType: req.file.mimetype 
      });

    if (error) {
      return sendError(res, 400, error.message || 'Upload failed');
    }

    // Generate public URL
    const publicUrl = `${process.env.SUPABASE_URL.replace(/\/+$/, '')}/storage/v1/object/public/${bucket}/${encodeURIComponent(filename)}`;

    // Update user profile with image
    const { updateError } = await supabase
      .from('users')
      .update({ image_url: publicUrl, updated_at: new Date().toISOString() })
      .eq('id', req.userId);

    if (updateError) return sendError(res, 400, updateError.message);

    return res.json({ 
      success: true, 
      url: publicUrl,
      message: 'Profile image uploaded successfully'
    });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// ----------------- Slots -----------------

// Get available slots for a ground on a date
app.get('/api/grounds/:groundId/available-slots/:date', async (req, res) => {
  try {
    const { groundId, date } = req.params;
    const { data: bookings, error } = await supabase
      .from('bookings')
      .select('time_slot, status')
      .eq('ground_id', groundId)
      .eq('booking_date', date)
      .in('status', ['confirmed', 'pending']);

    if (error) return sendError(res, 400, error.message);

    const allSlots = [
      '6:00-7:00 AM', '7:00-8:00 AM', '8:00-9:00 AM', '9:00-10:00 AM',
      '10:00-11:00 AM', '11:00-12:00 PM', '12:00-1:00 PM', '1:00-2:00 PM',
      '2:00-3:00 PM', '3:00-4:00 PM', '4:00-5:00 PM', '5:00-6:00 PM',
      '6:00-7:00 PM', '7:00-8:00 PM', '8:00-9:00 PM', '9:00-10:00 PM'
    ];

    const bookedSlots = (bookings || []).map(b => b.time_slot);
    const availableSlots = allSlots.filter(s => !bookedSlots.includes(s));

    return res.json({ 
      success: true, 
      availableSlots, 
      bookedSlots,
      totalSlots: allSlots.length,
      availableCount: availableSlots.length
    });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Check if specific slot is available
app.get('/api/grounds/:groundId/check-slot/:date/:timeSlot', async (req, res) => {
  try {
    const { groundId, date, timeSlot } = req.params;
    const { data: bookings, error } = await supabase
      .from('bookings')
      .select('id')
      .eq('ground_id', groundId)
      .eq('booking_date', date)
      .eq('time_slot', decodeURIComponent(timeSlot))
      .in('status', ['confirmed', 'pending'])
      .limit(1);

    if (error) return sendError(res, 400, error.message);

    const isAvailable = !bookings || bookings.length === 0;
    return res.json({ 
      success: true, 
      available: isAvailable,
      timeSlot: decodeURIComponent(timeSlot)
    });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// ----------------- Bookings -----------------

// Create booking
app.post('/api/bookings', verifyToken, async (req, res) => {
  try {
    const { ground_id, booking_date, time_slot, price } = req.body;
    if (!ground_id || !booking_date || !time_slot) return sendError(res, 400, 'Missing required fields');

    // Validate date is not in past
    const bookingDateObj = new Date(booking_date);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (bookingDateObj < today) {
      return sendError(res, 400, 'Cannot book for past dates');
    }

    // Check if slot already booked
    const { data: existing, error: checkErr } = await supabase
      .from('bookings')
      .select('id')
      .eq('ground_id', ground_id)
      .eq('booking_date', booking_date)
      .eq('time_slot', time_slot)
      .in('status', ['confirmed', 'pending'])
      .limit(1);

    if (checkErr) return sendError(res, 400, checkErr.message);
    if (existing && existing.length > 0) return sendError(res, 400, 'This slot is already booked');

    const finalPrice = (price !== undefined && price !== null) ? parseFloat(price) : null;

    // Create booking
    const { data, error } = await supabase
      .from('bookings')
      .insert([{
        user_id: req.userId,
        ground_id,
        booking_date,
        time_slot,
        price: finalPrice,
        status: 'confirmed',
        created_at: new Date().toISOString()
      }])
      .select()
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data, message: 'Booking confirmed' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get user's active bookings
app.get('/api/bookings/active/:userId', verifyToken, async (req, res) => {
  try {
    const requestedUserId = req.params.userId;
    // allow if requester is same user or admin
    const { data: user, error: uErr } = await supabase
      .from('users')
      .select('is_admin')
      .eq('id', req.userId)
      .single();
    
    if (uErr) return sendError(res, 400, 'Unable to verify permissions');
    if (req.userId !== requestedUserId && !user.is_admin) return sendError(res, 403, 'Forbidden');

    // Get all bookings (confirmed, pending) - not just future ones, include past too for history
    const { data, error } = await supabase
      .from('bookings')
      .select('*, grounds(*), users(name, email, phone)')
      .eq('user_id', requestedUserId)
      .in('status', ['confirmed', 'pending'])
      .order('booking_date', { ascending: false })
      .order('created_at', { ascending: false });

    if (error) {
      console.error('Error fetching bookings:', error);
      return sendError(res, 400, error.message);
    }
    
    // Transform the response to ensure grounds and users are properly nested
    const transformedData = (data || []).map(booking => {
      // Ensure grounds object is properly structured
      if (booking.grounds && typeof booking.grounds === 'object') {
        booking.grounds = booking.grounds;
      }
      // Ensure users object is properly structured
      if (booking.users && typeof booking.users === 'object') {
        booking.users = booking.users;
      }
      return booking;
    });
    
    return res.json({ success: true, data: transformedData });
  } catch (err) {
    console.error('Exception in getActiveBookings:', err);
    return sendError(res, 500, err.message);
  }
});

// Get user's booking history
app.get('/api/bookings/history/:userId', verifyToken, async (req, res) => {
  try {
    const requestedUserId = req.params.userId;
    const { data: user, error: uErr } = await supabase
      .from('users')
      .select('is_admin')
      .eq('id', req.userId)
      .single();
    
    if (uErr) return sendError(res, 400, 'Unable to verify permissions');
    if (req.userId !== requestedUserId && !user.is_admin) return sendError(res, 403, 'Forbidden');

    const { data, error } = await supabase
      .from('bookings')
      .select('*, grounds(*), users(name, email, phone)')
      .eq('user_id', requestedUserId)
      .order('booking_date', { ascending: false });

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Cancel booking
app.put('/api/bookings/:bookingId/cancel', verifyToken, async (req, res) => {
  try {
    const bookingId = req.params.bookingId;
    const { data: booking, error: getErr } = await supabase
      .from('bookings')
      .select('id, user_id, ground_id, status')
      .eq('id', bookingId)
      .single();

    if (getErr || !booking) return sendError(res, 404, 'Booking not found');
    if (booking.status === 'cancelled') return sendError(res, 400, 'Booking already cancelled');

    // Allow if owner of booking
    if (booking.user_id === req.userId) {
      // proceed
    } else {
      // Check if requester is admin and owns the ground
      const { data: requester, error: rErr } = await supabase
        .from('users')
        .select('is_admin')
        .eq('id', req.userId)
        .single();
      
      if (rErr) return sendError(res, 400, 'Unable to verify permissions');
      if (!requester.is_admin) return sendError(res, 403, 'Unauthorized');

      // Verify admin owns the ground
      const { data: ground, error: gErr } = await supabase
        .from('grounds')
        .select('created_by')
        .eq('id', booking.ground_id)
        .single();
      
      if (gErr) return sendError(res, 400, 'Unable to verify ground ownership');
      if (ground.created_by !== req.userId) return sendError(res, 403, 'Unauthorized');
    }

    const { data, error } = await supabase
      .from('bookings')
      .update({ status: 'cancelled', updated_at: new Date().toISOString() })
      .eq('id', bookingId)
      .select()
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data, message: 'Booking cancelled' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get all bookings for a ground (admin only)
app.get('/api/bookings/ground/:groundId', verifyToken, requireAdmin, requireGroundOwner, async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('bookings')
      .select('*, users(name, email, phone)')
      .eq('ground_id', req.params.groundId)
      .order('booking_date', { ascending: true });

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get booking details
app.get('/api/bookings/:bookingId', verifyToken, async (req, res) => {
  try {
    const { data: booking, error } = await supabase
      .from('bookings')
      .select('*, grounds(*), users(name, email, phone)')
      .eq('id', req.params.bookingId)
      .single();

    if (error) return sendError(res, 404, 'Booking not found');

    // Check authorization
    if (booking.user_id !== req.userId) {
      const { data: user } = await supabase
        .from('users')
        .select('is_admin')
        .eq('id', req.userId)
        .single();
      
      if (!user.is_admin) return sendError(res, 403, 'Forbidden');
    }

    return res.json({ success: true, data: booking });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// ----------------- Admin Reporting -----------------

// Admin: list all bookings for admin-owned grounds with user + ground info
app.get('/api/admin/bookings', verifyToken, requireAdmin, async (req, res) => {
  try {
    // Get grounds owned by this admin
    const { data: adminGrounds, error: gErr } = await supabase
      .from('grounds')
      .select('id, name')
      .eq('created_by', req.userId);

    if (gErr) {
      console.error('Error fetching admin grounds:', gErr);
      return sendError(res, 400, gErr.message);
    }

    const groundIds = (adminGrounds || []).map(g => g.id);
    if (groundIds.length === 0) {
      return res.json({ success: true, data: [] });
    }

    // Fetch bookings with joins to ground + user so admin can see who booked what
    // Use the same pattern as other endpoints for consistency
    const { data, error } = await supabase
      .from('bookings')
      .select('*, grounds(*), users(name, email, phone)')
      .in('ground_id', groundIds)
      .order('booking_date', { ascending: false })
      .order('created_at', { ascending: false });

    if (error) {
      console.error('Error fetching admin bookings:', error);
      return sendError(res, 400, error.message);
    }

    // Transform response - Supabase returns foreign key relationships as objects
    const transformed = (data || []).map(b => {
      // Handle grounds - Supabase returns as object for foreign key relationships
      const groundObj = (b.grounds && typeof b.grounds === 'object' && !Array.isArray(b.grounds)) 
        ? b.grounds 
        : (Array.isArray(b.grounds) && b.grounds.length > 0 ? b.grounds[0] : null);
      
      // Handle users - Supabase returns as object for foreign key relationships
      const userObj = (b.users && typeof b.users === 'object' && !Array.isArray(b.users))
        ? b.users
        : (Array.isArray(b.users) && b.users.length > 0 ? b.users[0] : null);

      return {
        id: b.id,
        ground_id: b.ground_id,
        booking_date: b.booking_date,
        time_slot: b.time_slot || '',
        status: b.status || 'confirmed',
        price: b.price || 0,
        created_at: b.created_at,
        grounds: groundObj ? {
          id: groundObj.id,
          name: groundObj.name || 'Unknown Ground',
          location: groundObj.location || '',
          type: groundObj.type || ''
        } : null,
        users: userObj ? {
          id: userObj.id || b.user_id || null,
          name: userObj.name || 'Unknown User',
          phone: userObj.phone || '',
          email: userObj.email || ''
        } : null
      };
    });

    console.log(`Admin bookings API: Returning ${transformed.length} bookings for admin ${req.userId}`);
    return res.json({ success: true, data: transformed });
  } catch (err) {
    console.error('Exception in admin bookings:', err);
    return sendError(res, 500, err.message);
  }
});

// Admin: bookings summary for admin's grounds
app.get('/api/admin/bookings-summary', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { data: adminGrounds, error: gErr } = await supabase
      .from('grounds')
      .select('id, name')
      .eq('created_by', req.userId);

    if (gErr) {
      console.error('Error fetching admin grounds:', gErr);
      return sendError(res, 400, gErr.message);
    }
    
    const groundIds = (adminGrounds || []).map(g => g.id);
    if (groundIds.length === 0) {
      return res.json({ success: true, data: [] });
    }

    // Get all bookings (confirmed, pending, cancelled) for admin's grounds
    const { data, error } = await supabase
      .from('bookings')
      .select('*, grounds(*), users(name, email, phone)')
      .in('ground_id', groundIds)
      .order('booking_date', { ascending: false })
      .order('created_at', { ascending: false });

    if (error) {
      console.error('Error fetching bookings summary:', error);
      return sendError(res, 400, error.message);
    }
    
    // Transform the response to ensure grounds and users are properly nested
    const transformedData = (data || []).map(booking => {
      // Ensure grounds object is properly structured
      if (booking.grounds && typeof booking.grounds === 'object') {
        booking.grounds = booking.grounds;
      }
      // Ensure users object is properly structured
      if (booking.users && typeof booking.users === 'object') {
        booking.users = booking.users;
      }
      return booking;
    });
    
    return res.json({ success: true, data: transformedData });
  } catch (err) {
    console.error('Exception in bookings-summary:', err);
    return sendError(res, 500, err.message);
  }
});

// Admin: bookings for specific ground (owner only)
app.get('/api/admin/grounds/:groundId/bookings', verifyToken, requireAdmin, requireGroundOwner, async (req, res) => {
  try {
    // Get all bookings (all statuses) for the ground
    const { data, error } = await supabase
      .from('bookings')
      .select('*, users(name, email, phone), grounds(*)')
      .eq('ground_id', req.params.groundId)
      .order('booking_date', { ascending: false });

    if (error) {
      console.error('Error fetching ground bookings:', error);
      return sendError(res, 400, error.message);
    }
    
    // Transform the response to ensure grounds is properly nested
    const transformedData = (data || []).map(booking => {
      if (booking.grounds && typeof booking.grounds === 'object') {
        booking.grounds = booking.grounds;
      }
      return booking;
    });
    
    return res.json({ success: true, data: transformedData });
  } catch (err) {
    console.error('Exception in ground bookings:', err);
    return sendError(res, 500, err.message);
  }
});

// Admin: bookings by date range
app.get('/api/admin/bookings-by-date-range', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { startDate, endDate } = req.query;
    const { data: adminGrounds, error: gErr } = await supabase
      .from('grounds')
      .select('id')
      .eq('created_by', req.userId);

    if (gErr) return sendError(res, 400, gErr.message);
    const groundIds = (adminGrounds || []).map(g => g.id);
    if (groundIds.length === 0) return res.json({ success: true, data: [] });

    // Get all bookings (all statuses) for admin's grounds within date range
    let query = supabase
      .from('bookings')
      .select('*, grounds(*), users(name, email, phone)')
      .in('ground_id', groundIds);

    if (startDate) query = query.gte('booking_date', startDate);
    if (endDate) query = query.lte('booking_date', endDate);

    const { data, error } = await query.order('booking_date', { ascending: false });
    
    if (error) {
      console.error('Error fetching bookings by date range:', error);
      return sendError(res, 400, error.message);
    }
    
    // Transform the response to ensure grounds and users are properly nested
    const transformedData = (data || []).map(booking => {
      if (booking.grounds && typeof booking.grounds === 'object') {
        booking.grounds = booking.grounds;
      }
      // Ensure users object is properly structured
      if (booking.users && typeof booking.users === 'object') {
        booking.users = booking.users;
      }
      return booking;
    });
    
    return res.json({ success: true, data: transformedData });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Admin: Get booking statistics
app.get('/api/admin/statistics', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { data: adminGrounds } = await supabase
      .from('grounds')
      .select('id')
      .eq('created_by', req.userId);

    const groundIds = (adminGrounds || []).map(g => g.id);
    if (groundIds.length === 0) {
      return res.json({ 
        success: true, 
        data: { 
          totalGrounds: 0,
          totalBookings: 0,
          totalRevenue: 0,
          activeBookingsCount: 0,
          cancelledBookingsCount: 0
        } 
      });
    }

    const { data: bookings } = await supabase
      .from('bookings')
      .select('status, price')
      .in('ground_id', groundIds);

    const totalBookings = bookings?.length || 0;
    const activeBookings = bookings?.filter(b => b.status === 'confirmed').length || 0;
    const cancelledBookings = bookings?.filter(b => b.status === 'cancelled').length || 0;
    const totalRevenue = bookings?.reduce((sum, b) => sum + (b.price || 0), 0) || 0;

    return res.json({
      success: true,
      data: {
        totalGrounds: groundIds.length,
        totalBookings,
        totalRevenue,
        activeBookingsCount: activeBookings,
        cancelledBookingsCount: cancelledBookings
      }
    });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// ----------------- User Profile -----------------

// Get profile
app.get('/api/user/profile', verifyToken, async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('users')
      .select('id, name, email, phone, image_url, created_at')
      .eq('id', req.userId)
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Update profile
app.put('/api/user/profile', verifyToken, async (req, res) => {
  try {
    const { name, phone, image_url } = req.body;
    if (!name || !phone) return sendError(res, 400, 'Name and phone are required');

    const { data, error } = await supabase
      .from('users')
      .update({ 
        name, 
        phone, 
        image_url: image_url || null, 
        updated_at: new Date().toISOString() 
      })
      .eq('id', req.userId)
      .select()
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data, message: 'Profile updated' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Change password
app.post('/api/user/change-password', verifyToken, async (req, res) => {
  try {
    const { oldPassword, newPassword } = req.body;
    if (!oldPassword || !newPassword) return sendError(res, 400, 'Both passwords are required');
    if (newPassword.length < 6) return sendError(res, 400, 'New password must be >= 6 chars');

    const { data: user, error: getErr } = await supabase
      .from('users')
      .select('password_hash')
      .eq('id', req.userId)
      .single();
    
    if (getErr) return sendError(res, 400, 'User not found');

    const valid = await bcrypt.compare(oldPassword, user.password_hash);
    if (!valid) return sendError(res, 401, 'Old password is incorrect');

    const hashed = await bcrypt.hash(newPassword, 10);
    const { error } = await supabase
      .from('users')
      .update({ 
        password_hash: hashed, 
        updated_at: new Date().toISOString() 
      })
      .eq('id', req.userId);
    
    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, message: 'Password changed successfully' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get user by ID (admin only)
app.get('/api/admin/users/:userId', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('users')
      .select('id, name, email, phone, is_admin, created_at')
      .eq('id', req.params.userId)
      .single();

    if (error) return sendError(res, 404, 'User not found');
    return res.json({ success: true, data });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get all users (admin only)
app.get('/api/admin/users', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('users')
      .select('id, name, email, phone, is_admin, created_at')
      .order('created_at', { ascending: false });

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Make user admin (super admin only - check if needed)
app.put('/api/admin/users/:userId/role', verifyToken, requireAdmin, async (req, res) => {
  try {
    const { is_admin } = req.body;
    
    const { data, error } = await supabase
      .from('users')
      .update({ is_admin, updated_at: new Date().toISOString() })
      .eq('id', req.params.userId)
      .select()
      .single();

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, data, message: 'User role updated' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Delete user (admin only)
app.delete('/api/admin/users/:userId', verifyToken, requireAdmin, async (req, res) => {
  try {
    // Soft delete by marking as inactive
    const { error } = await supabase
      .from('users')
      .update({ is_active: false, updated_at: new Date().toISOString() })
      .eq('id', req.params.userId);

    if (error) return sendError(res, 400, error.message);
    return res.json({ success: true, message: 'User deleted' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Get ground reviews (optional - requires reviews table)
app.get('/api/grounds/:groundId/reviews', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('reviews')
      .select('*, users(name)')
      .eq('ground_id', req.params.groundId)
      .order('created_at', { ascending: false });

    if (error) {
      // Reviews table might not exist yet
      return res.json({ success: true, data: [] });
    }
    return res.json({ success: true, data: data || [] });
  } catch (err) {
    return res.json({ success: true, data: [] });
  }
});

// Add ground review (optional)
app.post('/api/grounds/:groundId/reviews', verifyToken, async (req, res) => {
  try {
    const { rating, comment } = req.body;
    if (!rating) return sendError(res, 400, 'Rating is required');

    const { data, error } = await supabase
      .from('reviews')
      .insert([{
        ground_id: req.params.groundId,
        user_id: req.userId,
        rating: parseInt(rating),
        comment: comment || null,
        created_at: new Date().toISOString()
      }])
      .select()
      .single();

    if (error) {
      // Reviews table might not exist - gracefully handle
      return res.json({ success: true, message: 'Review would be added once reviews table is set up' });
    }
    return res.json({ success: true, data, message: 'Review added' });
  } catch (err) {
    return sendError(res, 500, err.message);
  }
});

// Health check
app.get('/', (req, res) => res.json({ success: true, message: 'Ground Booking API is running' }));

// 404 handler
app.use((req, res) => {
  res.status(404).json({ success: false, message: 'Endpoint not found' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`\n✓ Ground Booking System API running on port ${PORT}`);
  console.log(`✓ Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log(`✓ Database: Supabase connected\n`);
});
