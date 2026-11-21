import api from './api.js';
import { useState, useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router';
import NavBar from './NavBar.jsx';
import Home from './Home.jsx';
import MagazineList from './MagazineList.jsx';
import AddBookForm from './AddBookForm.jsx';
import BookList from './BookList.jsx';
import './App.css';
import AuthProvider from './AuthProvider.jsx';
import useAuth from './useAuth.js';
import Login from './Login.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import DisplayCart from './DisplayCart.jsx';

// 2. App shell and public login
export default function App() {
  return <AuthProvider><main className="app-container">
    <h1>My Bookstore Frontend</h1>
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<ProtectedRoute />}><Route path="*" element={<SignedInBookApp />} /></Route>
    </Routes>
  </main></AuthProvider>;
}

// 3. Token-keyed session boundary
function SignedInBookApp() {
  const { token } = useAuth();
  // A session change unmounts every old draft, collection and pending callback.
  return <BookApp key={token} />;
}


// 4. Private state and Book loading
function BookApp() {
  const { token, isAdmin } = useAuth();
  const isCurrent = () => localStorage.getItem('token') === token;
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [cartLoading, setCartLoading] = useState(true);
  const [cartError, setCartError] = useState('');
  const [cartPending, setCartPending] = useState(false);
  // === KEEP YOUR EXISTING NICHE STATE ===
  // Keep private state in BookApp or its original child owner.
  // Do not move it above the token-keyed session boundary.
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  // === KEEP YOUR EXISTING NICHE EFFECTS AND HANDLERS ===
  // Retain your effects, callbacks, handlers and signatures.
  // Keep their existing ownership and props when merging.
  useEffect(() => {
    let active = true;
    api.get('/books')
        .then(response => { if (active) setBooks(response.data); })
        .catch(err => { if (active) setError(err.message); })
        .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [token]);

  useEffect(() => {
    let active = true;
    api.get('/carts/mine')
        .then(response => { if (active && localStorage.getItem('token') === token) setCart(response.status === 204 ? null : response.data); })
        .catch(err => { if (active && localStorage.getItem('token') === token) setCartError(err.message); })
        .finally(() => { if (active && localStorage.getItem('token') === token) setCartLoading(false); });
    return () => { active = false; };
  }, [token]);

  async function changeCart(productId, remove = false) {
    if (cartLoading || cartPending || !isCurrent()) return;
    setCartError(''); setCartPending(true);
    try {
      const path = `/carts/mine/products/${productId}`;
      const response = remove ? await api.delete(path) : await api.post(path);
      if (isCurrent()) setCart(response.status === 204 ? null : response.data);
    } catch (err) { if (isCurrent()) setCartError(err.message); }
    finally { if (isCurrent()) setCartPending(false); }
  }

  // 5. Book mutations and callbacks
  function handleBookAdded(savedBook) {
    if (!isCurrent()) return;
    setBooks(previous => [...previous, savedBook]);
    navigate('/inventory');
  }

  async function handleDeleteBook(id) {
    if (!window.confirm('Delete this book?')) return;
    setError('');
    try {
      await api.delete(`/books/${id}`);
      if (!isCurrent()) return;
      setBooks(previous => previous.filter(book => book.id !== id));
    } catch (err) { if (isCurrent()) setError(err.message); }
  }

  async function handleUpdateBook(id, data) {
    const response = await api.put(`/books/${id}`, data);
    if (!isCurrent()) return;
    const savedBook = response.data;
    setBooks(previous => previous.map(book => book.id === id ? savedBook : book));
  }


  // 6. Navigation and signed-in routes
  return <>
    <NavBar cartCount={cart?.products?.length ?? 0} />
    {cartError && <p role="status">Cart: {cartError}</p>}
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/inventory" element={<BookList books={books} loading={loading} error={error}
                                                  onDelete={handleDeleteBook} onUpdate={handleUpdateBook} canManage={isAdmin} onAddToCart={changeCart} cartPending={cartLoading || cartPending} />} />
      <Route path="/cart" element={<DisplayCart cart={cart} loading={cartLoading} error={cartError} pending={cartPending} onRemove={id => changeCart(id, true)} />} />
      <Route path="/add" element={isAdmin ? <AddBookForm onBookAdded={handleBookAdded} /> : <p role="alert">Administrator access is required.</p>} />
      <Route path="/magazines" element={<MagazineList onAddToCart={changeCart} cartPending={cartLoading || cartPending} />} />
      {/* === KEEP YOUR EXISTING NICHE ROUTES HERE ===
          Keep actual routes before this wildcard route.
          Retain their existing props and callbacks. */}
      <Route path="*" element={<h2>Page not found</h2>} />
    </Routes>
  </>;
}