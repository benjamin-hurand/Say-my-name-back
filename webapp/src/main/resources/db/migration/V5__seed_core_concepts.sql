INSERT INTO concepts (
    code,
    icon_key,
    value_type,
    is_derived,
    portability_kind,
    identity_component_eligible,
    required_max_values,
    default_casing_strategy
)
VALUES
    ('FIRST_NAME', NULL, 'TEXT', 0, 'VALUE_ONLY', 1, 1, 'TITLE_CASE'),
    ('LAST_NAME',  NULL, 'TEXT', 0, 'VALUE_ONLY', 1, 1, 'UPPERCASE'),
    ('GENDER',     NULL, 'ENUM', 0, 'VALUE_ONLY', 0, 1, 'NONE'),
    ('IDENTITY',   NULL, 'TEXT', 1, 'NONE',       0, 1, 'NONE')
AS incoming
ON DUPLICATE KEY UPDATE
    value_type = incoming.value_type,
    is_derived = incoming.is_derived,
    portability_kind = incoming.portability_kind,
    identity_component_eligible = incoming.identity_component_eligible,
    required_max_values = incoming.required_max_values,
    default_casing_strategy = incoming.default_casing_strategy;
