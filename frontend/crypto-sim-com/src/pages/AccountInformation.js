import React, { useState, useEffect } from 'react';
import { getUsername } from '../services/authService';
import axiosInstance from '../axios/axiosConfig';
import apiRoutes from '../config/apiEndpoints';
import styles from '../styles/AccountInformation.module.css';
import Decimal from 'decimal.js';

const AccountInformation = () => {
    const [balance, setBalance] = useState(null);
    const [assets, setAssets] = useState([]);
    const username = getUsername();

    useEffect(() => {
        const fetchAccountInfo = async () => {
            try {
                const balanceResponse = await axiosInstance.get(apiRoutes.user.getUserBalance(username));
                const formattedBalance = new Decimal(balanceResponse.data).toFixed(2);
                setBalance(formattedBalance);

                const assetsResponse = await axiosInstance.get(apiRoutes.user.getUserCryptoAssets(username));
                const formattedAssets = assetsResponse.data.map(asset => ({
                    ...asset,
                    quantity: new Decimal(asset.quantity).toFixed(8),
                }));
                setAssets(formattedAssets);
            } catch (error) {
                console.error("Error fetching account info:", error);
            }
        };

        if (username) {
            fetchAccountInfo();
        }
    }, [username]);

    return (
        <div className={styles.accountPage}>
            <div className={styles.accountContainer}>
                <h2 className={styles.accountTitle}>Account Information</h2>

                <div className={styles.userInfoContainer}>
                    <div className={styles.userInfoLabel}>Username:</div>
                    <div className={styles.userInfoValue}>{username}</div>
                </div>

                {balance !== null ? (
                    <div className={styles.balanceContainer}>
                        <div className={styles.balanceLabel}>Balance:</div>
                        <div className={styles.balanceValue}>${balance}</div>
                    </div>
                ) : (
                    <p>Loading balance...</p>
                )}

                <h3 className={styles.assetsTitle}>Assets:</h3>
                {assets.length > 0 ? (
                    <div className={styles.assetList}>
                        {assets.map((asset) => (
                            <div key={asset.id} className={styles.assetItem}>
                                <div className={styles.assetDetails}>
                                    <span className={styles.assetName}>{asset.cryptoCurrency.name}</span>
                                    <span className={styles.assetQuantity}>{asset.quantity}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p className={styles.noAssets}>No assets available.</p>
                )}
            </div>
        </div>
    );
};

export default AccountInformation;
