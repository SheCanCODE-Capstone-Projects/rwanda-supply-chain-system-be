ALTER TABLE user_profile ADD COLUMN district VARCHAR(255);
ALTER TABLE user_profile ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE user_profile ADD COLUMN longitude DOUBLE PRECISION;

CREATE TABLE rfqs (
    id                  UUID PRIMARY KEY,
    buyer_id            UUID NOT NULL,
    category            VARCHAR(255) NOT NULL,
    description         VARCHAR(1000),
    quantity            INTEGER NOT NULL,
    unit                VARCHAR(255) NOT NULL,
    target_price        NUMERIC(19,2),
    delivery_district   VARCHAR(255),
    delivery_latitude   DOUBLE PRECISION,
    delivery_longitude  DOUBLE PRECISION,
    bidding_deadline    TIMESTAMP,
    status              VARCHAR(255) NOT NULL DEFAULT 'OPEN',
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);

CREATE TABLE quotations (
    id           UUID PRIMARY KEY,
    rfq_id       UUID NOT NULL REFERENCES rfqs (id),
    supplier_id  UUID NOT NULL,
    unit_price   NUMERIC(12,2) NOT NULL,
    quantity     INTEGER NOT NULL,
    message      VARCHAR(1000),
    status       VARCHAR(255) NOT NULL DEFAULT 'SUBMITTED',
    valid_until  TIMESTAMP,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE TABLE negotiations (
    id                 UUID PRIMARY KEY,
    quotation_id       UUID NOT NULL REFERENCES quotations (id),
    sender_id          UUID NOT NULL,
    sender_role        VARCHAR(255) NOT NULL,
    proposed_price     NUMERIC(12,2),
    proposed_quantity  INTEGER,
    message            VARCHAR(1000),
    created_at         TIMESTAMP NOT NULL
);

CREATE TABLE purchase_orders (
    id            UUID PRIMARY KEY,
    po_number     VARCHAR(255) NOT NULL UNIQUE,
    rfq_id        UUID NOT NULL REFERENCES rfqs (id),
    quotation_id  UUID NOT NULL UNIQUE REFERENCES quotations (id),
    buyer_id      UUID NOT NULL,
    supplier_id   UUID NOT NULL,
    unit_price    NUMERIC(12,2) NOT NULL,
    quantity      INTEGER NOT NULL,
    total_amount  NUMERIC(14,2) NOT NULL,
    status        VARCHAR(255) NOT NULL DEFAULT 'CREATED',
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL
);

CREATE TABLE notifications (
    id            UUID PRIMARY KEY,
    recipient_id  UUID NOT NULL,
    type          VARCHAR(255) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    message       VARCHAR(1000) NOT NULL,
    reference_id  UUID,
    read          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL
);
