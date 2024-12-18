INSERT INTO users
(id, email, "name", "password")
VALUES(1, 'abc@test.com', 'abc', '123456')  ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM "users"));
