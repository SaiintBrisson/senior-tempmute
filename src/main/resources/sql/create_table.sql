CREATE TABLE IF NOT EXISTS muted_players (

    id CHAR(36) NOT NULL,
    name VARCHAR(16) NOT NULL,

    author VARCHAR(16) NOT NULL,

    reason VARCHAR(255) DEFAULT 'No reason provided',
    expiration TIMESTAMP NULL,

    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (id),
    PRIMARY KEY (name)

)