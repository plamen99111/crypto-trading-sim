import axios from 'axios';
import { getAccessToken, removeAccessToken, setAccessToken } from '../services/authService';
import { API_URL, APP_IS_PRODUCTION } from '../config/config';
import { logout, } from '../services/authService';
import apiRoutes from '../config/apiEndpoints';
import log from 'loglevel';

let isRefreshing = false; // Flag to prevent multiple refresh attempts
let refreshSubscribers = []; // To queue requests while refreshing

if (APP_IS_PRODUCTION) {
    log.setLevel('warn')
} else {
    log.setLevel('debug');
}

const sendLogToBackend = async (logData) => {
    try {
        await axios.post(`${API_URL}${apiRoutes.logging.logs}`, logData); // Adjust the endpoint
    } catch (error) {
        log.error("Failed to send log to API");
    }
};


const axiosInstance = axios.create({
    baseURL: API_URL,
    withCredentials: true, // Ensure cookies are sent with requests
});

export const refreshAccessToken = async () => {
    try {
        const response = await axiosInstance.post(apiRoutes.auth.refresh); // Call the refresh endpoint
        const newAccessToken = response.data.token;

        removeAccessToken();
        setAccessToken(newAccessToken);

        log.info("Refreshed token.");

        return newAccessToken;

    } catch (error) {
        log.error("Failed to refresh token");
        await logout();
        throw error;
    }
};

axiosInstance.interceptors.request.use(
    (config) => {
        const excludedEndpoints = [apiRoutes.auth.login, apiRoutes.auth.refresh];
        if (!excludedEndpoints.includes(config.url)) {
            const token = getAccessToken();
            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response) => response, // If the response is successful, return it
    async (error) => {
        const originalRequest = error.config;

        if (!originalRequest) {
            return Promise.reject(error);
        }

        if (axios.isCancel(error)) {
            log.info("Request canceled:", error.message);
            return Promise.reject(error);
        }

        if (originalRequest.url.includes(apiRoutes.auth.refresh)) {
            log.error("Refresh token request failed");
            await logout();
            return Promise.reject(error);
        }

        // Check if the error is due to expired token (401 Unauthorized) and ensure retry flag is set
        if (error.response && (error.response.status === 401 || error.response.status === 403) && !originalRequest._retry) {
            originalRequest._retry = true; // Prevent infinite retry loop by ensuring this only happens once

            if (!isRefreshing) {
                isRefreshing = true;
                try {
                    const newAccessToken = await refreshAccessToken();
                    isRefreshing = false;

                    // Retry all queued requests
                    refreshSubscribers.forEach((callback) => callback(newAccessToken));
                    refreshSubscribers = [];

                    // Retry the original request
                    originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                    return axiosInstance(originalRequest);
                } catch (refreshError) {
                    log.error("Token refresh failed.");
                    isRefreshing = false;

                    await logout();

                    return Promise.reject(refreshError);
                }
            } else {
                // Queue the request until the refresh token process is complete
                return new Promise((resolve, reject) => {
                    refreshSubscribers.push((newAccessToken) => {
                        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                        resolve(axiosInstance(originalRequest));
                    });
                });
            }
        }

        return Promise.reject(error);
    }
);

axiosInstance.interceptors.request.use(
    (config) => {

        sendLogToBackend({
            level: 'info',
            type: 'request',
            url: config.url,
            method: config.method,
            headers: config.headers,
            body: config.data,
            timestamp: new Date().toISOString(),
        });

        return config;
    },
    (error) => {

        sendLogToBackend({
            level: 'error',
            type: 'request-error',
            error: error.message,
            timestamp: new Date().toISOString(),
        });

        return Promise.reject(error);
    }
);


export default axiosInstance;
