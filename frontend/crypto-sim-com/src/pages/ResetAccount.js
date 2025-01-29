import React, { useState } from 'react';
import { getUsername } from '../services/authService';
import axiosInstance from '../axios/axiosConfig';
import apiRoutes from '../config/apiEndpoints';
import styles from '../styles/ResetAccount.module.css';

const ResetAccount = () => {
    const [isResetting, setIsResetting] = useState(false);
    const [resetSuccess, setResetSuccess] = useState(null);
    const username = getUsername();

    const handleReset = async () => {
        setIsResetting(true);
        try {
            await axiosInstance.get(apiRoutes.user.resetAccount(username));
            setResetSuccess(true);
        } catch (error) {
            console.error("Error resetting account:", error);
            setResetSuccess(false);
        }
        setIsResetting(false);
    };

    return (
        <div className={styles.accountPage}>
            <div className={styles.accountContainer}>
                <h2 className={styles.accountTitle}>Reset Account</h2>

                <div className={styles.userInfoContainer}>
                    <div className={styles.userInfoLabel}>Username:</div>
                    <div className={styles.userInfoValue}>{username}</div>
                </div>

                {resetSuccess === true && (
                    <div className={styles.successMessage}>
                        <p>Account has been successfully reset!</p>
                    </div>
                )}
                {resetSuccess === false && (
                    <div className={styles.errorMessage}>
                        <p>There was an error resetting the account. Please try again later.</p>
                    </div>
                )}

                <button
                    className={styles.resetButton}
                    onClick={handleReset}
                    disabled={isResetting}
                >
                    {isResetting ? 'Resetting...' : 'Reset Account'}
                </button>
            </div>
        </div>
    );
};

export default ResetAccount;
