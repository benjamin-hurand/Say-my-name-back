ALTER TABLE workspaces
    ADD CONSTRAINT uq_workspaces_tenant_id
    UNIQUE (tenant_id, id);