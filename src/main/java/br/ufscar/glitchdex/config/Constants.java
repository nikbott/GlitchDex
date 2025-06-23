package br.ufscar.glitchdex.config;

public enum Constants {
    ;

    // Roles
    public static final String ADMIN = "ADMIN";
    public static final String TESTER = "TESTER";
    public static final String HAS_AUTHORITY_ADMIN = "hasAuthority('" + ADMIN + "')";
    public static final String HAS_ANY_AUTHORITY_ADMIN_TESTER = "hasAnyAuthority('" + ADMIN + "', '" + TESTER + "')";

    // Request Params
    public static final String SORT = "sort";
    public static final String ORDER = "order";

    // File Storage
    public static final String DOT = ".";

}