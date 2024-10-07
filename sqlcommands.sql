-- Users table
CREATE TABLE users(
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_type VARCHAR(255) NOT NULL DEFAULT 'general',
    PRIMARY KEY(id),
    UNIQUE(username)
);

-- Categories table
CREATE TABLE categories (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content_path VARCHAR(255),
    PRIMARY KEY (id)
);

-- Question table
CREATE TABLE questions(
    id INT NOT NULL AUTO_INCREMENT,
    category INT NOT NULL,
    question VARCHAR(255) NOT NULL,
    correct_answer VARCHAR(255) NOT NULL,
    wrong_answer_1 VARCHAR(255) NOT NULL,
    wrong_answer_2 VARCHAR(255),
    wrong_answer_3 VARCHAR(255),
    content_path VARCHAR(255),
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id),
    FOREIGN KEY(category) REFERENCES categories(id) ON DELETE CASCADE
);

