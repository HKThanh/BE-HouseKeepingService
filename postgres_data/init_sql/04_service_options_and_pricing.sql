-- Service configuration and pricing rules
CREATE TABLE service_options (
    option_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    service_id INT NOT NULL REFERENCES service(service_id) ON DELETE CASCADE,
    label TEXT NOT NULL,
    option_type VARCHAR(30) NOT NULL CHECK (
     option_type IN (
                     'SINGLE_CHOICE_RADIO',
                     'SINGLE_CHOICE_DROPDOWN',
                     'MULTIPLE_CHOICE_CHECKBOX',
                     'QUANTITY_INPUT',
                     'TEXT_INPUT'
         )
     ),
    display_order INT,
    is_required BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    parent_option_id INT REFERENCES service_options(option_id),
    parent_choice_id INT,
    validation_rules JSONB
);

CREATE TABLE service_option_choices (
    choice_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    option_id INT NOT NULL REFERENCES service_options(option_id) ON DELETE CASCADE,
    label TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT
);

ALTER TABLE service_options
ADD CONSTRAINT fk_parent_choice
FOREIGN KEY (parent_choice_id)
REFERENCES service_option_choices(choice_id);

CREATE TABLE pricing_rules (
    rule_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    service_id INT NOT NULL REFERENCES service(service_id),
    rule_name VARCHAR(255) UNIQUE NOT NULL,
    condition_logic VARCHAR(10) DEFAULT 'ALL' CHECK (condition_logic IN ('ALL', 'ANY')),
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    price_adjustment DECIMAL(10, 2) DEFAULT 0,
    staff_adjustment INT DEFAULT 0,
    duration_adjustment_hours DECIMAL(5, 2) DEFAULT 0
);

CREATE TABLE rule_conditions (
    rule_id INT NOT NULL REFERENCES pricing_rules(rule_id) ON DELETE CASCADE,
    choice_id INT NOT NULL REFERENCES service_option_choices(choice_id) ON DELETE CASCADE,
    PRIMARY KEY (rule_id, choice_id)
);