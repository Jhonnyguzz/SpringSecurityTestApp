-- Demo user: admin@example.com / admin (BCrypt hash)
INSERT INTO app_user (name, email, password, role, provider) VALUES ('Admin', 'admin@example.com', '{bcrypt}$2y$10$pUBVpmnLISkNBeHSlkpLzuwEhBtLVcS8ChRcPbiUNIsN7/svjpKUe', 'ADMIN', 'LOCAL');

INSERT INTO song (title, artist, added_by, created_at) VALUES ('Bohemian Rhapsody', 'Queen', 'ana@example.com', DATEADD('HOUR', -30, CURRENT_TIMESTAMP));
INSERT INTO song (title, artist, added_by, created_at) VALUES ('Hotel California', 'Eagles', 'carlos@example.com', DATEADD('HOUR', -20, CURRENT_TIMESTAMP));
INSERT INTO song (title, artist, added_by, created_at) VALUES ('La Bamba', 'Ritchie Valens', 'maria@example.com', DATEADD('HOUR', -12, CURRENT_TIMESTAMP));
INSERT INTO song (title, artist, added_by, created_at) VALUES ('Smells Like Teen Spirit', 'Nirvana', 'juan@example.com', DATEADD('HOUR', -8, CURRENT_TIMESTAMP));
INSERT INTO song (title, artist, added_by, created_at) VALUES ('Billie Jean', 'Michael Jackson', 'laura@example.com', DATEADD('HOUR', -5, CURRENT_TIMESTAMP));
INSERT INTO song (title, artist, added_by, created_at) VALUES ('Clocks', 'Coldplay', 'pedro@example.com', DATEADD('HOUR', -2, CURRENT_TIMESTAMP));
INSERT INTO song (title, artist, added_by, created_at) VALUES ('Despacito', 'Luis Fonsi', 'ana@example.com', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP));
