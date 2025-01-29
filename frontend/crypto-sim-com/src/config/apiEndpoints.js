const apiRoutes = {
  user: {
    getUserBalance: (username) => `/user/balance?username=${username}`,
    getUserCryptoAssets: (username) => `/user/crypto/assets?username=${username}`,
    getUserTransactions: (username) => `/user/transactions/${username}`,
    buyCrypto: (username, cryptoPair, quantity, price, maxBuyingQuantity) => `/user/crypto/buy?username=${username}&cryptoPair=${cryptoPair}&quantity=${quantity}&price=${price}&maxBuyingQuantity=${maxBuyingQuantity}`,
    sellCrypto: (username, cryptoPair, quantity, price, maxSellingQuantity) => `/user/crypto/sell?username=${username}&cryptoPair=${cryptoPair}&quantity=${quantity}&price=${price}&maxSellingQuantity=${maxSellingQuantity}`,
    resetAccount: (username) => `/user/reset/${username}`,
  },
  crypto: {
    getAllCryptocurrencies: `/cryptocurrencies/get-all-cryptocurrencies`
  },
  auth: {
    login: `/auth/login`,
    refresh: `/auth/refresh`,
    logout: `/auth/logout`,
  },
  logging: {
    logs: `/logs`,
  },
};

export default apiRoutes;