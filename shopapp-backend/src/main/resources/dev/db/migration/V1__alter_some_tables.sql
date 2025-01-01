-- versioned migrations
ALTER TABLE categories MODIFY name VARCHAR(150) UNIQUE;
-- Chuyển trường price thành decimal
