INSERT INTO roles (name)
SELECT 'USER'
    WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'USER'
);