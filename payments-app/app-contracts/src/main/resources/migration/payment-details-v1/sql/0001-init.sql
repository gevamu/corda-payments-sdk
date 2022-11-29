CREATE TABLE payment_details (
    id VARCHAR(64) NOT NULL,
    accountName VARCHAR(64) NOT NULL,
    bic VARCHAR(16) NOT NULL,
    country VARCHAR(2) NOT NULL REFERENCES country(isoCodeAlpha2),
    currency VARCHAR(3) NOT NULL REFERENCES currency(isoCode),
    creditor VARCHAR(64) NOT NULL REFERENCES creditor(account),
    debtor VARCHAR(64) NOT NULL REFERENCES debtor(account),
    PRIMARY KEY (id)
);
