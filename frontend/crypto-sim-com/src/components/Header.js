import React, { useState } from 'react';
import { Navbar, Nav, NavDropdown } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { logout, isAuthenticated } from '../services/authService';
import TextLogoBlack from '../assets/logos/logoTextBlack.png';
import Logo from '../assets/logos/logo.png';
import '../styles/Header.css';

const Header = () => {
    const [expanded, setExpanded] = useState(false);
    const navigate = useNavigate();


    const handleLogout = async () => {
        await logout();
        navigate('/');
        setExpanded(false);
    };

    const handleLinkClick = () => {
        setExpanded(false);
    };


    return (
        <Navbar expand="lg" fixed="top" className="custom-navbar" expanded={expanded}>
            <div className="container d-flex justify-content-between align-items-center">
                {/* Logo container aligned to the left */}
                <div className="logo-container d-flex">
                    <Navbar.Brand as={Link} to="/" className="custom-navbar-brand" onClick={handleLinkClick}>
                        <img
                            src={Logo}
                            height="30"
                            width="30"
                            alt="Logo"
                            className="d-inline-block align-top rounded-1 custom-logo"
                        />
                        <img
                            src={TextLogoBlack}
                            height="30"
                            width="300"
                            alt="Text Logo"
                            className="d-inline-block align-top custom-logo text-logo-space"
                        />
                    </Navbar.Brand>
                </div>
                {/* Toggler button (only for small screens) */}
                <Navbar.Toggle
                    aria-controls="basic-navbar-nav"
                    style={{ borderColor: 'white' }}
                    onClick={() => setExpanded(!expanded)}
                    className="d-lg-none"
                />

                {/* Navigation links container aligned to the center */}
                <div className="nav-container d-flex justify-content-center align-items-center">
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="d-flex justify-content-center align-items-center">
                            <Nav.Link as={Link} to="/" className="nav-link" onClick={handleLinkClick}>
                                {('Top 20 Cryptocurrencies')}
                            </Nav.Link>
                            {isAuthenticated() ? (
                                <>
                                    <NavDropdown title={('Account Settings')} id="settings-dropdown" className="settings-dropdown">
                                        <NavDropdown.Item as={Link} to="/account-information" className="nav-link" onClick={handleLinkClick}>
                                            {('Account Information')}
                                        </NavDropdown.Item>
                                        <NavDropdown.Item as={Link} to="/transaction-history" className="nav-link" onClick={handleLinkClick}>
                                            {('Transaction History')}
                                        </NavDropdown.Item>
                                        <NavDropdown.Item as={Link} to="/reset-account" className="nav-link" onClick={handleLinkClick}>
                                            {('Reset Account')}
                                        </NavDropdown.Item>
                                    </NavDropdown>
                                    <Nav.Link as="button" className="nav-link" onClick={handleLogout}>
                                        {('Logout')}
                                    </Nav.Link>
                                </>
                            ) : (
                                <Nav.Link as={Link} to="/login" className="nav-link" onClick={handleLinkClick}>
                                    {('Login')}
                                </Nav.Link>
                            )}

                        </Nav>
                    </Navbar.Collapse>
                </div>

            </div>
        </Navbar>
    );
};

export default Header;
