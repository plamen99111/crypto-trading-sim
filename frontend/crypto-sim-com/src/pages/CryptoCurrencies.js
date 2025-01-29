import React, { useState, useEffect } from 'react';
import Decimal from 'decimal.js';
import styles from '../styles/CryptoCurrencies.module.css';
import { isAuthenticated } from '../services/authService';
import { getUsername } from '../services/authService';
import axiosInstance from '../axios/axiosConfig';
import apiRoutes from '../config/apiEndpoints';
import { PLAIN_API_URL } from '../config/config';

const CryptoCurrencies = () => {
  const [cryptoNameList, setCryptoNameList] = useState([]);
  const [cryptoData, setCryptoData] = useState({});
  const [socket, setSocket] = useState(null);
  const [buyQuantities, setBuyQuantities] = useState({});
  const [sellQuantities, setSellQuantities] = useState({});
  const username = getUsername();

  useEffect(() => {
    const fetchCryptoNames = async () => {
      try {
        const response = await axiosInstance.get(apiRoutes.crypto.getAllCryptocurrencies);
        setCryptoNameList(response.data);
      } catch (error) {
        console.error('Error fetching cryptocurrency names:', error);
      }
    };

    fetchCryptoNames();
  }, []);

  useEffect(() => {
    const newSocket = new WebSocket(`ws://${PLAIN_API_URL}/cryptocurrencies`);
    console.log(`ws://${PLAIN_API_URL}/cryptocurrencies`);
    setSocket(newSocket);

    newSocket.onopen = () => {
      console.log('Connected to backend WebSocket');
    };

    newSocket.onmessage = (event) => {
      const message = event.data;
      console.log('Message from backend:', message);

      try {
        const data = JSON.parse(message);
        if (data && data.crypto) {
          setCryptoData((prevData) => ({
            ...prevData,
            [data.crypto]: data,
          }));
        }
      } catch (error) {
        console.error('Error parsing message:', error);
      }
    };

    newSocket.onclose = () => {
      console.log('Disconnected from backend WebSocket');
    };

    newSocket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    return () => {
      if (newSocket) {
        newSocket.close();
      }
    };
  }, []);

  const handleBuy = async (crypto, quantity) => {
    const parsedQuantity = new Decimal(quantity || 0);
    const maxBuyQuantity = new Decimal(cryptoData[crypto.pair]?.buyingMaxQuantity || 0);
    const price = new Decimal(cryptoData[crypto.pair]?.buyingPrice || 0);

    if (parsedQuantity.isNaN() || parsedQuantity.lte(0)) {
      alert('Please enter a valid numeric and positive buy quantity.');
      return;
    }
    if (parsedQuantity.gt(maxBuyQuantity)) {
      alert(`The buy quantity cannot exceed the max available quantity of ${maxBuyQuantity.toString()}.`);
      return;
    }

    try {
      const username = getUsername();

      const response = await axiosInstance.post(apiRoutes.user.buyCrypto(username, crypto.pair, parsedQuantity.toString(), price.toString(), maxBuyQuantity.toString()));

      if (response.status === 200) {
        alert(response.data);
      } else {
        throw new Error(response.data || 'An error occurred while processing the purchase');
      }
    } catch (error) {
      console.error('Error while buying crypto:', error);
      alert('An error occurred while attempting to buy cryptocurrency. Please try again.');
    }
  };

  const handleSell = async (crypto, quantity) => {
    const parsedQuantity = new Decimal(quantity || 0);
    const maxSellQuantity = new Decimal(cryptoData[crypto.pair]?.sellingMaxQuantity || 0);
    const price = new Decimal(cryptoData[crypto.pair]?.sellingPrice || 0);

    if (parsedQuantity.isNaN() || parsedQuantity.lte(0)) {
      alert('Please enter a valid numeric and positive sell quantity.');
      return;
    }
    if (parsedQuantity.gt(maxSellQuantity)) {
      alert(`The sell quantity cannot exceed the max available quantity of ${maxSellQuantity.toString()}.`);
      return;
    }

    try {
      const username = getUsername();

      const response = await axiosInstance.post(apiRoutes.user.sellCrypto(username, crypto.pair, parsedQuantity.toString(), price.toString(), maxSellQuantity.toString()));

      if (response.status === 200) {
        alert(response.data);
      } else {
        throw new Error(response.data || 'An error occurred while processing the sale');
      }
    } catch (error) {
      console.error('Error while selling crypto:', error);
      alert('An error occurred while attempting to sell cryptocurrency. Please try again.');
    }
  };

  const handleBuyQuantityChange = (pair, value) => {
    setBuyQuantities((prevQuantities) => ({
      ...prevQuantities,
      [pair]: value,
    }));
  };

  const handleSellQuantityChange = (pair, value) => {
    setSellQuantities((prevQuantities) => ({
      ...prevQuantities,
      [pair]: value,
    }));
  };

  const renderCryptoData = (crypto, index) => {
    const liveData = cryptoData[crypto.pair];
    const buyQuantity = buyQuantities[crypto.pair] || '';
    const sellQuantity = sellQuantities[crypto.pair] || '';

    const buyProgress = liveData?.buyingMaxQuantity
      ? (new Decimal(buyQuantity || 0) / new Decimal(liveData.buyingMaxQuantity || 0)) * 100
      : 0;

    const sellProgress = liveData?.sellingMaxQuantity
      ? (new Decimal(sellQuantity || 0) / new Decimal(liveData.sellingMaxQuantity || 0)) * 100
      : 0;

    const buyProgressColor = buyProgress > 100 ? '#dc3545' : '#00aaff';
    const sellProgressColor = sellProgress > 100 ? '#dc3545' : '#00aaff';

    return (
      <div key={crypto.pair} className={styles.cryptoCard}>
        <div className={styles.cryptoCardHeader}>
          <span className={styles.cryptoName}>{crypto.name}</span>
          <span className={styles.cryptoPair}>{crypto.pair}</span>
        </div>
        <div className={styles.cryptoBuyingPrice}>
          {liveData ? `$${new Decimal(liveData.buyingPrice || 0).toFixed(2)}` : '-'}
        </div>
        <div className={styles.cryptoRow}>
          <div className={styles.labelValueWrapper}>
            <span>Max Buy Quantity:</span>
            <span className={styles.maxQuantityValue}>
              {liveData ? `${new Decimal(liveData.buyingMaxQuantity || 0)}` : '-'}
            </span>
          </div>
        </div>

        {isAuthenticated() && (
          <>
            <div className={styles.cryptoRow}>
              <input
                type="number"
                className={styles.quantityInput}
                value={buyQuantity}
                onChange={(e) => handleBuyQuantityChange(crypto.pair, e.target.value)}
              />
              <button className={styles.buyButton} onClick={() => handleBuy(crypto, buyQuantity)}>
                Buy
              </button>
            </div>

            <div className={styles.progressBar}>
              <div
                className={styles.progress}
                style={{
                  width: `${Math.min(buyProgress, 100)}%`,
                  backgroundColor: buyProgressColor,
                }}
              />
            </div>
          </>
        )}

        <div className={styles.cryptoRow}>
          <div className={styles.labelValueWrapper}>
            <span>Max Sell Quantity:</span>
            <span className={styles.maxQuantityValue}>
              {liveData ? `${new Decimal(liveData.sellingMaxQuantity || 0)}` : '-'}
            </span>
          </div>
        </div>
        <div className={styles.cryptoSellingPrice}>
          {liveData ? `$${new Decimal(liveData.sellingPrice || 0).toFixed(2)}` : '-'}
        </div>

        {isAuthenticated() && (
          <>
            <div className={styles.cryptoRow}>
              <input
                type="number"
                className={styles.quantityInput}
                value={sellQuantity}
                onChange={(e) => handleSellQuantityChange(crypto.pair, e.target.value)}
              />
              <button className={styles.sellButton} onClick={() => handleSell(crypto, sellQuantity)}>
                Sell
              </button>
            </div>

            <div className={styles.progressBar}>
              <div
                className={styles.progress}
                style={{
                  width: `${Math.min(sellProgress, 100)}%`,
                  backgroundColor: sellProgressColor,
                }}
              />
            </div>
          </>
        )}
      </div>
    );
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.heading}>Top 20 Cryptocurrencies</h1>
      <div className={styles.cryptoCardContainer}>
        {cryptoNameList.length === 0 ? (
          <p>Loading cryptocurrencies...</p>
        ) : (
          cryptoNameList.map((crypto, index) => renderCryptoData(crypto, index))
        )}
      </div>
    </div>
  );
};

export default CryptoCurrencies;
