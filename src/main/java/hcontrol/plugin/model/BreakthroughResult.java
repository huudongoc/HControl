package hcontrol.plugin.model;

/**
 * BREAKTHROUGH RESULT
 * Ket qua dot pha (success / 3 loai that bai: noi thuong / tan phe / chet)
 */
public enum BreakthroughResult {
    // Thanh cong
    SUCCESS("Dot pha thanh cong!"),
    
    // Dieu kien chua du
    INSUFFICIENT_CULTIVATION("Chua du tu vi!"),
    ON_COOLDOWN("Dang trong thoi gian hoi phuc sau dot pha!"),
    
    // THAT BAI LOAI 1: NOI THUONG (co the chua)
    INTERNAL_INJURY_MINOR("Dot pha that bai - noi thuong nhe (10-20%)"),
    INTERNAL_INJURY_MODERATE("Dot pha that bai - noi thuong trung binh (20-40%)"),
    INTERNAL_INJURY_SEVERE("Dot pha that bai - noi thuong nang (40-60%)"),
    
    // THAT BAI LOAI 2: TAN PHE (rat kho chua)
    CRIPPLED_LIGHT("Dot pha that bai - can co bi ton! (giam tran level)"),
    CRIPPLED_SEVERE("Dot pha that bai - kinh mach bi huy! (khong dot pha nua)"),
    CRIPPLED_PERMANENT("Dot pha that bai - dao co sup do! (tro thanh pham nhan)"),
    
    // THAT BAI LOAI 3: CHET (hiem, chi khi cuong ep)
    DEATH("Dot pha that bai - tu si tu vong!"),
    
    // THAT BAI THIEN KIEP
    TRIBULATION_FAILED("Thien kiep that bai!");
    
    private final String message;
    
    BreakthroughResult(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    public boolean isDeath() {
        return this == DEATH;
    }
    
    public boolean isInternalInjury() {
        return this == INTERNAL_INJURY_MINOR 
            || this == INTERNAL_INJURY_MODERATE 
            || this == INTERNAL_INJURY_SEVERE;
    }
    
    public boolean isCrippled() {
        return this == CRIPPLED_LIGHT 
            || this == CRIPPLED_SEVERE 
            || this == CRIPPLED_PERMANENT;
    }
}
