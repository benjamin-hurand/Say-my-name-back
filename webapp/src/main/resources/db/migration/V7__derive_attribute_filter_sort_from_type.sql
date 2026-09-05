-- Filter/sort are no longer admin-configurable per attribute: the admin
-- "Champs" page stopped exposing "Disponible dans les filtres" / "Disponible
-- pour le tri", and the backend now derives both purely from the attribute's
-- type (see AttributeCapabilities: everything except TEXT is both filterable
-- and sortable). New/updated attributes already get these columns recomputed
-- by AttributeService on every create/update; this backfills existing rows
-- so they match the same rule immediately, since a few code paths (global
-- text-search scope, context-attribute inclusion on person cards) still read
-- these columns directly for performance instead of re-deriving in Java.
UPDATE attributes
SET filter = (type <> 'TEXT'),
    sort = (type <> 'TEXT');
