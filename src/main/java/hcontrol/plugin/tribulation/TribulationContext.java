package hcontrol.plugin.tribulation;

import hcontrol.plugin.model.CultivationRealm;

import java.util.UUID;

/**
 * TRIBULATION CONTEXT
 * Single source of truth cho trang thai thien kiep
 * (thay vi scatter o Service/UI/Listener)
 */
public class TribulationContext {

    // ===== PLAYER INFO =====
    private final UUID playerUUID;
    private final CultivationRealm fromRealm;   // realm cu (truoc khi dot pha)
    private final CultivationRealm toRealm;     // realm muc tieu (sau khi thanh cong)

    // ===== STATE MACHINE =====
    private TribulationPhase currentPhase;
    private long startTime;                     // thoi diem bat dau thien kiep (ms)
    private long currentPhaseStartTime;         // thoi diem vao phase hien tai

    // ===== WAVE TRACKING =====
    private int currentWave;                    // wave hien tai (1-9)
    private final int maxWaves;                 // tong so wave (phu thuoc realm)

    // ===== QUESTION STATE (optional, khong phai moi realm deu co) =====
    private String questionKey;                 // key cua cau hoi (de tra cuu answer)
    private boolean questionAnswered;           // da tra loi chua
    private boolean answerCorrect;              // tra loi dung/sai

    // ===== RESULT =====
    private TribulationResult result;           // ket qua (null = chua xong)

    public TribulationContext(UUID playerUUID, CultivationRealm fromRealm, CultivationRealm toRealm) {
        this.playerUUID = playerUUID;
        this.fromRealm = fromRealm;
        this.toRealm = toRealm;
        
        // init state
        this.currentPhase = TribulationPhase.PREPARE;
        this.startTime = System.currentTimeMillis();
        this.currentPhaseStartTime = this.startTime;
        
        // wave setup
        this.currentWave = 0;
        this.maxWaves = calculateMaxWaves(toRealm);
        
        // question init (chua co)
        this.questionKey = null;
        this.questionAnswered = false;
        this.answerCorrect = false;
        
        this.result = null;
    }

    // ===== WAVE CALCULATION =====
    
    /**
     * Tinh so wave dua tren realm
     * - Luyen Khi → Truc Co: 3 wave
     * - Truc Co → Kim Dan: 5 wave
     * - Kim Dan → Nguyen Anh: 7 wave
     * - Nguyen Anh → Hoa Than: 9 wave
     */
    private int calculateMaxWaves(CultivationRealm targetRealm) {
        return switch (targetRealm) {
            case TRUCCO -> 3;
            case KIMDAN -> 5;
            case NGUYENANH -> 7;
            case HOATHAN -> 9;
            default -> 3; // mac dinh
        };
    }

    // ===== STATE TRANSITIONS =====

    /**
     * Chuyen sang wave tiep theo
     * @return true neu con wave, false neu het wave
     */
    public boolean advanceToWave() {
        if (currentWave >= maxWaves) {
            return false; // het wave roi
        }
        
        currentWave++;
        
        // set phase WAVE_1 -> WAVE_9
        switch (currentWave) {
            case 1 -> currentPhase = TribulationPhase.WAVE_1;
            case 2 -> currentPhase = TribulationPhase.WAVE_2;
            case 3 -> currentPhase = TribulationPhase.WAVE_3;
            case 4 -> currentPhase = TribulationPhase.WAVE_4;
            case 5 -> currentPhase = TribulationPhase.WAVE_5;
            case 6 -> currentPhase = TribulationPhase.WAVE_6;
            case 7 -> currentPhase = TribulationPhase.WAVE_7;
            case 8 -> currentPhase = TribulationPhase.WAVE_8;
            case 9 -> currentPhase = TribulationPhase.WAVE_9;
        }
        
        currentPhaseStartTime = System.currentTimeMillis();
        return true;
    }

    /**
     * Chuyen sang phase QUESTION (tam tinh hoi)
     * @param questionKey key cau hoi (de tra cuu dap an)
     */
    public void advanceToQuestion(String questionKey) {
        this.currentPhase = TribulationPhase.QUESTION;
        this.questionKey = questionKey;
        this.questionAnswered = false;
        this.answerCorrect = false;
        this.currentPhaseStartTime = System.currentTimeMillis();
    }

    /**
     * Submit answer cho QUESTION phase
     * @param isCorrect dap an dung/sai
     */
    public void submitAnswer(boolean isCorrect) {
        this.questionAnswered = true;
        this.answerCorrect = isCorrect;
    }

    /**
     * Hoan thanh thien kiep (SUCCESS/FAIL)
     * @param result ket qua
     */
    public void complete(TribulationResult result) {
        this.result = result;
        
        if (result == TribulationResult.SUCCESS) {
            this.currentPhase = TribulationPhase.SUCCESS;
        } else {
            this.currentPhase = TribulationPhase.FAIL;
        }
        
        this.currentPhaseStartTime = System.currentTimeMillis();
    }

    // ===== TIMING =====

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public long getPhaseElapsedTime() {
        return System.currentTimeMillis() - currentPhaseStartTime;
    }

    // ===== CHECKS =====

    public boolean isFinished() {
        return currentPhase == TribulationPhase.SUCCESS || currentPhase == TribulationPhase.FAIL;
    }

    public boolean hasQuestionPhase() {
        // chi realm cao moi co question (Nguyen Anh tro len)
        return toRealm.ordinal() >= CultivationRealm.NGUYENANH.ordinal();
    }

    // ===== GETTERS =====

    public UUID getPlayerUUID() { return playerUUID; }
    public CultivationRealm getFromRealm() { return fromRealm; }
    public CultivationRealm getToRealm() { return toRealm; }
    public TribulationPhase getCurrentPhase() { return currentPhase; }
    public int getCurrentWave() { return currentWave; }
    public int getMaxWaves() { return maxWaves; }
    public String getQuestionKey() { return questionKey; }
    public boolean isQuestionAnswered() { return questionAnswered; }
    public boolean isAnswerCorrect() { return answerCorrect; }
    public TribulationResult getResult() { return result; }
}
