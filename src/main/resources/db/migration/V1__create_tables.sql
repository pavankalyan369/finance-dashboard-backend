-- EXTENSION
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- USERS
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(150) UNIQUE NOT NULL,
                       password TEXT NOT NULL,
                       status VARCHAR(20) DEFAULT 'INACTIVE',
                       last_login TIMESTAMP,
                       deactivated_by VARCHAR(20),
                       token_version INT DEFAULT 0,
                       is_deleted BOOLEAN DEFAULT FALSE,
                       deleted_at TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PROFILES
CREATE TABLE profiles (
                          user_id UUID PRIMARY KEY,
                          full_name VARCHAR(100) NOT NULL,
                          phone VARCHAR(20) NOT NULL,
                          address TEXT,
                          age INT,
                          gender VARCHAR(20),
                          occupation VARCHAR(30),

                          CONSTRAINT fk_profile_user
                              FOREIGN KEY(user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);

-- ROLES
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL
);

-- USER ROLES
CREATE TABLE user_roles (
                            id SERIAL PRIMARY KEY,
                            user_id UUID NOT NULL,
                            role_id INT NOT NULL,
                            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY(role_id) REFERENCES roles(id) ON DELETE CASCADE,
                            UNIQUE(user_id, role_id)
);

-- TRANSACTIONS
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              user_id UUID NOT NULL,

                              amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),

                              type VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),

                              category VARCHAR(50) NOT NULL,

                              notes TEXT,

                              transaction_date DATE NOT NULL,

                              is_deleted BOOLEAN DEFAULT FALSE,

                              deleted_at TIMESTAMP,

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_user
                                  FOREIGN KEY(user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);



-- ACTIVATION TOKENS
CREATE TABLE activation_tokens (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id UUID NOT NULL,
                                   token VARCHAR(255) UNIQUE NOT NULL,
                                   expiry TIMESTAMP NOT NULL,
                                   used BOOLEAN DEFAULT FALSE,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- INDEXES
CREATE INDEX idx_users_email ON users(email);
-- CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_category ON transactions(category);
CREATE INDEX idx_transactions_type ON transactions(type);