-- Table: CryptoCurrency
CREATE TABLE IF NOT EXISTS cryptocurrency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE COLLATE utf8mb4_bin,
    symbol VARCHAR(10) NOT NULL,
    pair VARCHAR(100) NOT NULL UNIQUE
);

INSERT IGNORE INTO cryptocurrency (name, symbol, pair) VALUES
    ('Bitcoin', 'BTC', 'BTC/USD'),
    ('Ethereum', 'ETH', 'ETH/USD'),
    ('XRP', 'XRP', 'XRP/USD'),
    ('Tether', 'USDT', 'USDT/USD'),
    ('Solana', 'SOL', 'SOL/USD'),
    ('USD Coin', 'USDC', 'USDC/USD'),
    ('Dogecoin', 'DOGE', 'DOGE/USD'),
    ('Cardano', 'ADA', 'ADA/USD'),
    ('TRON', 'TRX', 'TRX/USD'),
    ('Chainlink', 'LINK', 'LINK/USD'),
    ('Avalanche', 'AVAX', 'AVAX/USD'),
    ('Toncoin', 'TON', 'TON/USD'),
    ('Stellar', 'XLM', 'XLM/USD'),
    ('Sui', 'SUI', 'SUI/USD'),
    ('Shiba Inu', 'SHIB', 'SHIB/USD'),
    ('Polkadot', 'DOT', 'DOT/USD'),
    ('Litecoin', 'LTC', 'LTC/USD'),
    ('Bitcoin Cash', 'BCH', 'BCH/USD'),
    ('Uniswap', 'UNI', 'UNI/USD'),
    ('Dai', 'DAI', 'DAI/USD');


CREATE TABLE IF NOT EXISTS role (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     role_name VARCHAR(50) NOT NULL UNIQUE
    );

-- Table: User
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE COLLATE utf8mb4_bin,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 10000.00,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);


-- Table: Transaction
CREATE TABLE IF NOT EXISTS transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crypto_name VARCHAR(255) NOT NULL,
    crypto_symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL(18, 8) NOT NULL,
    price DECIMAL(18, 8) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('BUY', 'SELL')),
    user_id BIGINT NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
    );


-- Insert default roles
INSERT IGNORE INTO role (role_name) VALUES ('USER'), ('ADMIN');

INSERT IGNORE INTO user (id, role_id, username, password) VALUES
    (3, 1, 'user', '$2a$12$maVoEA809wgfw.L/pLBrcuWSJ1.H1dGS8Zb6v9eRoN6x4b8ETdNz2');

CREATE TABLE IF NOT EXISTS user_crypto_assets (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   username VARCHAR(255) NOT NULL COLLATE utf8mb4_bin,
   crypto_name VARCHAR(255) NOT NULL UNIQUE COLLATE utf8mb4_bin,
   quantity DECIMAL(18, 8) NOT NULL DEFAULT 0.00000000,
   FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE,
   FOREIGN KEY (crypto_name) REFERENCES cryptocurrency(name) ON DELETE CASCADE,
   UNIQUE (username, crypto_name) -- To ensure each user can only hold one entry for each cryptocurrency
    );
