CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE bases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    description VARCHAR(255),
    active BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bases_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE equipment_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_equipment_types_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE assets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    serial_number VARCHAR(255),
    model VARCHAR(255),
    unit VARCHAR(255),
    description VARCHAR(255),
    active BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_assets_equipment_type FOREIGN KEY (equipment_type_id) REFERENCES equipment_types (id)
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    base_id BIGINT,
    enabled BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_users_base FOREIGN KEY (base_id) REFERENCES bases (id)
) ENGINE=InnoDB;

CREATE TABLE inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    assigned_quantity INT NOT NULL,
    expended_quantity INT NOT NULL,
    last_updated DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_base_asset UNIQUE (base_id, asset_id),
    CONSTRAINT ck_inventory_nonnegative CHECK (total_quantity >= 0 AND available_quantity >= 0
        AND assigned_quantity >= 0 AND expended_quantity >= 0),
    CONSTRAINT ck_inventory_quantity_balance CHECK (total_quantity = available_quantity + assigned_quantity),
    CONSTRAINT fk_inventory_base FOREIGN KEY (base_id) REFERENCES bases (id),
    CONSTRAINT fk_inventory_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
) ENGINE=InnoDB;

CREATE TABLE opening_balances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    quantity INT NOT NULL,
    effective_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_opening_balances_base_asset UNIQUE (base_id, asset_id),
    CONSTRAINT ck_opening_balances_quantity CHECK (quantity > 0),
    CONSTRAINT fk_opening_balances_base FOREIGN KEY (base_id) REFERENCES bases (id),
    CONSTRAINT fk_opening_balances_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_opening_balances_user FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE purchases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    quantity INT NOT NULL,
    purchase_date DATE NOT NULL,
    reference_number VARCHAR(255),
    vendor VARCHAR(255),
    unit_cost DECIMAL(15,2) NOT NULL,
    total_cost DECIMAL(15,2) NOT NULL,
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchases_base FOREIGN KEY (base_id) REFERENCES bases (id),
    CONSTRAINT fk_purchases_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_purchases_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_purchases_quantity CHECK (quantity > 0),
    CONSTRAINT ck_purchases_cost CHECK (unit_cost >= 0 AND total_cost >= 0),
    INDEX ix_purchases_base_date (base_id, purchase_date)
) ENGINE=InnoDB;

CREATE TABLE transfers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_base_id BIGINT NOT NULL,
    destination_base_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    quantity INT NOT NULL,
    transfer_date DATE NOT NULL,
    reference_number VARCHAR(255),
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transfers_source_base FOREIGN KEY (source_base_id) REFERENCES bases (id),
    CONSTRAINT fk_transfers_destination_base FOREIGN KEY (destination_base_id) REFERENCES bases (id),
    CONSTRAINT fk_transfers_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_transfers_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_transfers_quantity CHECK (quantity > 0),
    INDEX ix_transfers_source_date (source_base_id, transfer_date),
    INDEX ix_transfers_destination_date (destination_base_id, transfer_date)
) ENGINE=InnoDB;

CREATE TABLE assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    assigned_to_user_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    quantity INT NOT NULL,
    assignment_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_assignments_base FOREIGN KEY (base_id) REFERENCES bases (id),
    CONSTRAINT fk_assignments_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_assignments_assigned_user FOREIGN KEY (assigned_to_user_id) REFERENCES users (id),
    CONSTRAINT fk_assignments_created_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_assignments_quantity CHECK (quantity > 0),
    INDEX ix_assignments_base_date (base_id, assignment_date)
) ENGINE=InnoDB;

CREATE TABLE expenditures (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    quantity INT NOT NULL,
    expenditure_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    reference VARCHAR(255),
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_expenditures_base FOREIGN KEY (base_id) REFERENCES bases (id),
    CONSTRAINT fk_expenditures_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_expenditures_user FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_expenditures_quantity CHECK (quantity > 0),
    INDEX ix_expenditures_base_date (base_id, expenditure_date)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id BIGINT,
    event_time DATETIME(6) NOT NULL,
    ip_address VARCHAR(255),
    description VARCHAR(1000),
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX ix_audit_logs_event_time (event_time)
) ENGINE=InnoDB;

INSERT INTO roles (name) VALUES ('ADMIN'), ('BASE_COMMANDER'), ('LOGISTICS_OFFICER');