-- Dynamic permission tables
CREATE TABLE features (
    feature_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    feature_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    module VARCHAR(50)
);

CREATE TABLE role_features (
    role_id INT NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    feature_id INT NOT NULL REFERENCES features(feature_id) ON DELETE CASCADE,
    is_enabled BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (role_id, feature_id)
);