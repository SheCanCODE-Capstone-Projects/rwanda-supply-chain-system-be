CREATE TABLE IF NOT EXISTS ${schema}.users (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS ${schema}.user_roles (
                                                    user_id UUID NOT NULL REFERENCES ${schema}.users(id) ON DELETE CASCADE,
    roles VARCHAR(50) NOT NULL
    );