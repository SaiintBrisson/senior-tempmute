INSERT INTO muted_players
    (id, name, author, reason, expiration) VALUES (?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE
    name = ?, author = ?, reason = ?, expiration = ?, date = ?;