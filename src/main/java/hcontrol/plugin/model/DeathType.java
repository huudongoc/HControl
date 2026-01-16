package hcontrol.plugin.model;

/**
 * DEATH TYPE ENUM
 * Cac loai chet trong he thong tu tien
 */
public enum DeathType {
    NORMAL,         // Chet thuong (fallback)
    POISON,         // Chet vi doc
    BOSS,           // Chet vi boss
    BATTLEFIELD,    // Chet o chien truong ngoai vuc
    SECRET_REALM,   // Chet trong bi canh
    TRIBULATION     // Chet vi thien kiep
}
