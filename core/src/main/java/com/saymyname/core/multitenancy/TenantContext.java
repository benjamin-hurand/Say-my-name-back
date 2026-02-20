package com.saymyname.core.multitenancy;

public final class TenantContext {

    private static final ThreadLocal<Long> TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long id) {
        TENANT.set(id);
    }

    public static Long get() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }

    public static void runWith(Long tenantId, Runnable r) {
        Long before = get();
        try {
            set(tenantId);
            r.run();
        } finally {
            if (before != null)
                set(before);
            else
                clear();
        }
    }
}
