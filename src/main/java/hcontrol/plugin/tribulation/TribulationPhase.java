package hcontrol.plugin.tribulation;

/**
 * THIEN KIEP PHASE
 * Cac giai doan cua thien kiep (tu tien RPG)
 */
public enum TribulationPhase {

    PREPARE,        // chuan bi (hieu ung tu linh)
    
    WAVE_1,         // dot 1 (nhe nhat)
    WAVE_2,
    WAVE_3,
    WAVE_4,
    WAVE_5,
    WAVE_6,
    WAVE_7,
    WAVE_8,
    WAVE_9,         // dot 9 (manh nhat)
    
    QUESTION,       // tam tinh hoi (chu y: khong phai moi realm deu co)
    
    SUCCESS,        // thanh cong (hoan thanh)
    FAIL;           // that bai (chet/sai/timeout)
    
    public boolean isWave() {
        return this.ordinal() >= WAVE_1.ordinal() && this.ordinal() <= WAVE_9.ordinal();
    }
    
    public int getWaveNumber() {
        if (!isWave()) return 0;
        return this.ordinal() - WAVE_1.ordinal() + 1;
    }
}
