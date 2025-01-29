import { jwtDecode } from 'jwt-decode';
import apiRoutes from '../config/apiEndpoints';
import axiosInstance from '../axios/axiosConfig';
import Cookies from 'js-cookie';
import { APP_IS_ACCESS_TOKEN_SAMESITE_STRICT } from '../config/config';

const sameSite = APP_IS_ACCESS_TOKEN_SAMESITE_STRICT === 'true' ? 'Strict' : 'None';

// Login function to store token and username in cookies
export const login = async (username, password) => {
    const response = await axiosInstance.post(apiRoutes.auth.login, { username, password });
    const token = response.data.token;

    // Store access token and username in cookies
    setAccessToken(token);
    setUsername(username);

    return token;
};

// Getters and Setters for Access Token
export const getAccessToken = () => Cookies.get('accessToken');
export const setAccessToken = (token) =>
    Cookies.set('accessToken', token, {
        path: '/',
        secure: true,
        sameSite: sameSite
    });
export const removeAccessToken = () => Cookies.remove('accessToken');

// Getters and Setters for Username
export const setUsername = (username) => {
    Cookies.set('username', username, { path: '/', secure: true, sameSite: sameSite });
};

export const getUsername = () => Cookies.get('username');
export const removeUsername = () => Cookies.remove('username');

// Logout function to remove both token and username from cookies
export const logout = async () => {
    removeAccessToken();
    removeRefreshRoken();
    removeUsername();  // Remove username from cookies
    window.location.href = '/login';

    try {
        await axiosInstance.post(apiRoutes.auth.logout);
    } catch (error) {
        console.error("Logout failed:", error);
    }
};

// Check if user is authenticated
export const isAuthenticated = () => {
    return !!getAccessToken();
};

// Decode JWT token (if needed)
export const decodeToken = (token) => {
    return jwtDecode(token);
};

// Remove refresh token (if required)
export const removeRefreshRoken = () => Cookies.remove('refreshToken');
