import React, { useState } from 'react';
import { login } from '../services/authService';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/Login.module.css';
import toastUtils from '../utils/toastUtils';
import errorUtils from '../utils/errorUtils';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        // Start a toast with pending state
        const toastId = toastUtils.showLoadingToast(('Logging in'));

        try {
            await login(username, password);

            // Update the toast to success state
            toastUtils.updateLoadingToast(toastId, ('Login successful!'), 'success')

            navigate('/');
        } catch (error) {
            const errorMessage = errorUtils.handleErrorMessage(error, 'Invalid credentials.');
            toastUtils.updateLoadingToast(toastId, (errorMessage), 'error')
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <div className={styles.loginPage}>
                <div className={styles.loginContainer}>
                    <h2 className={styles.loginTitle}>{('Login')}</h2>
                    <form onSubmit={handleSubmit} className={styles.loginForm}>
                        <input
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder={('Username')}
                            required
                            className={styles.loginInput}
                        />
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder={('Password')}
                            required
                            className={styles.loginInput}
                        />
                        <button
                            type="submit"
                            className={styles.loginButton}
                            disabled={loading}
                        >
                            {loading ? (
                                <div className="spinner-border" role="status">
                                    <span className="visually-hidden">Loading...</span>
                                </div>
                            ) : (
                                ('Login')
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </>
    );
};

export default Login;
