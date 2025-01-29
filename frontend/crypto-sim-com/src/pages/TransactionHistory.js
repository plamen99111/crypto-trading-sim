import React, { useState, useEffect } from 'react';
import { getUsername } from '../services/authService';
import axiosInstance from '../axios/axiosConfig';
import apiRoutes from '../config/apiEndpoints';
import styles from '../styles/TransactionHistory.module.css';
import Decimal from 'decimal.js';

const TransactionHistory = () => {
    const [transactions, setTransactions] = useState([]);  // State for transactions
    const username = getUsername();

    const initialBalance = new Decimal(10000); // Default starting balance of $10,000

    // Function to calculate total profit/loss
    const calculateTotalProfit = () => {
        return transactions.reduce((totalProfit, transaction) => {
            const quantity = new Decimal(transaction.quantity);
            const price = new Decimal(transaction.price);
            // Adjust logic for "buy" and "sell" transactions
            const profit = transaction.type === 'SELL'
                ? quantity.mul(price)  // Selling price * quantity
                : -quantity.mul(price); // Negative for "buy" transactions
            return totalProfit.plus(profit);
        }, new Decimal(0));
    };

    // Fetch transaction history
    useEffect(() => {
        const fetchTransactionHistory = async () => {
            try {
                const transactionsResponse = await axiosInstance.get(apiRoutes.user.getUserTransactions(username));
                const formattedTransactions = transactionsResponse.data.map(transaction => ({
                    ...transaction,
                    quantity: new Decimal(transaction.quantity).toFixed(8),
                    price: new Decimal(transaction.price).toFixed(2),
                    transactionDate: new Date(transaction.transactionDate).toLocaleString(), // Format date
                }));
                setTransactions(formattedTransactions);
            } catch (error) {
                console.error("Error fetching transaction history:", error);
            }
        };

        if (username) {
            fetchTransactionHistory();
        }
    }, [username]);

    // Calculate the total profit/loss
    const totalProfit = calculateTotalProfit();

    return (
        <div className={styles.transactionPage}>
            <div className={styles.transactionContainer}>
                <h2 className={styles.transactionTitle}>Transaction History</h2>

                {transactions.length > 0 ? (
                    <>
                        <div className={styles.transactionList}>
                            {transactions.map((transaction) => (
                                <div key={transaction.id} className={styles.transactionItem}>
                                    <div className={styles.transactionDetails}>
                                        <span className={styles.transactionCryptoName}>
                                            {transaction.cryptoName} ({transaction.cryptoSymbol})
                                        </span>
                                        <span className={styles.transactionType}>{transaction.type}</span>
                                    </div>
                                    <div className={styles.transactionPrice}>
                                        <span className={styles.transactionQuantity}>Quantity: {transaction.quantity}</span>
                                        <span className={styles.transactionPriceValue}>Price: ${transaction.price}</span>
                                    </div>
                                    <div className={styles.transactionDate}>
                                        <span>{transaction.transactionDate}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <div className={styles.totalProfit}>
                            <h3>Total Profit/Loss:
                                <span className={totalProfit.isNegative() ? styles.loss : styles.profit}>
                                    ${totalProfit.toFixed(2)}
                                </span>
                            </h3>
                        </div>
                    </>
                ) : (
                    <p className={styles.noTransactions}>No transactions available.</p>
                )}
            </div>
        </div>
    );
};

export default TransactionHistory;
