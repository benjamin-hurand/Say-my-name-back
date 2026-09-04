-- GENDER enum options previously used code == label (e.g. code='Homme',
-- label='Homme'), verbatim from whatever the admin typed. GENDER is now a
-- backend-owned canonical enum: codes become stable, language-independent
-- values (MALE / FEMALE / OTHER) never exposed for admin customization.
--
-- No facts.value row references a GENDER attribute in any tenant yet (this
-- was verified against the local dev database before writing this
-- migration), so existing attribute_enum_options rows for GENDER attributes
-- can be safely replaced. On an empty database this migration is a no-op:
-- the joins below match nothing until a tenant creates a GENDER attribute.

DELETE eo FROM attribute_enum_options eo
JOIN attributes a ON a.id = eo.attribute_id
JOIN concepts c ON c.id = a.concept_id
WHERE c.code = 'GENDER';

INSERT INTO attribute_enum_options (tenant_id, attribute_id, code, label, order_index, active)
SELECT a.tenant_id, a.id, opt.code, opt.label, opt.order_index, 1
FROM attributes a
JOIN concepts c ON c.id = a.concept_id
JOIN (
    SELECT 'MALE' AS code, 'Homme' AS label, 0 AS order_index
    UNION ALL SELECT 'FEMALE', 'Femme', 1
    UNION ALL SELECT 'OTHER', 'Non-binaire ou autre', 2
) opt ON 1 = 1
WHERE c.code = 'GENDER';
