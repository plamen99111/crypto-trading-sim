import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Header from './components/Header';
import Login from './pages/Login';
import CryptoCurrencies from './pages/CryptoCurrencies';
import AccountInformation from './pages/AccountInformation';
import TransactionHistory from './pages/TransactionHistory'
import ProtectedRoute from './components/ProtectedRoute';
import ResetAccount from './pages/ResetAccount';

function App() {

    return (

        // Using the Router without creating a history object manually
        <Router
            futureFlags={{
                v7_startTransition: true,  // Enables startTransition in v7
                v7_relativeSplatPath: true, // Enables relative splat path in v7
                v7_fetcherPersist: true,
                v7_normalizeFormMethod: true,
                v7_partialHydration: true,
                v7_skipActionErrorRevalidation: true,
            }}
        >
            {/* React toastify for notification */}
            <ToastContainer />
            {/* Navigation Bar */}
            <Header />
            <Routes>
                <Route path="/" element={<CryptoCurrencies />} />
                <Route path="/login" element={<Login />} />
                <Route path="/account-information" element={<ProtectedRoute><AccountInformation /></ProtectedRoute>} />
                <Route path="/transaction-history" element={<ProtectedRoute><TransactionHistory /></ProtectedRoute>} />
                <Route path="/reset-account" element={<ProtectedRoute><ResetAccount /></ProtectedRoute>} />
            </Routes>
        </Router>
    );
}

export default App;
