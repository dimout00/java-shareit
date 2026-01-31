-- src/main/resources/schema.sql

-- Пользователи
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL UNIQUE
);

-- Вещи
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id),
    request_id BIGINT
);

-- Бронирования
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    item_id BIGINT NOT NULL REFERENCES items(id),
    booker_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING'
);

-- Комментарии
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    item_id BIGINT NOT NULL REFERENCES items(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    created TIMESTAMP NOT NULL
);

-- Индексы для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_items_owner_id ON items(owner_id);
CREATE INDEX IF NOT EXISTS idx_items_available ON items(is_available);
CREATE INDEX IF NOT EXISTS idx_bookings_item_id ON bookings(item_id);
CREATE INDEX IF NOT EXISTS idx_bookings_booker_id ON bookings(booker_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_dates ON bookings(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_comments_item_id ON comments(item_id);