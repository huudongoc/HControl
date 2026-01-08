package hcontrol.plugin.tribulation;

/**
 * TRIBULATION RESULT — Ket qua thien kiep
 */
public enum TribulationResult {
    SUCCESS,        // vuot qua thanh cong
    FAIL_DEATH,     // chet (HP <= 0)
    FAIL_ANSWER,    // sai cau hoi tam tinh
    FAIL_TIMEOUT    // het thoi gian (logout hoac khong tra loi)
}
