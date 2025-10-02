package com.saymyname.core.multitenancy;

public final class OrgContext {
    private static final ThreadLocal<Long> ORG = new ThreadLocal<>();

    private OrgContext() {
    }

    public static void set(Long id) {
        ORG.set(id);
    }

    public static Long get() {
        return ORG.get();
    }

    public static void clear() {
        ORG.remove();
    }

    public static void runWith(Long orgId, Runnable r) {
        Long before = get();
        try {
            set(orgId);
            r.run();
        } finally {
            if (before != null)
                set(before);
            else
                clear();
        }
    }
}
