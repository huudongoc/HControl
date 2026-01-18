package hcontrol.plugin.service;

import hcontrol.plugin.tribulation.TribulationResult;

/**
 * TRIBULATION LOGIC SERVICE
 * Xu ly logic tinh toan ve tribulation (wave duration, strike interval, etc)
 * KHONG chua UI logic (chi chua logic tinh toan)
 */
public class TribulationLogicService {
    
    /**
     * Tinh thoi gian cua moi wave (tick)
     * Wave cao → thoi gian dai hon
     */
    public int getWaveDuration(int currentWave) {
        return 100 + currentWave * 20; // 5s -> 9s
    }
    
    /**
     * Tinh tan so set danh trong wave (tick)
     * Wave cao → set danh nhanh hon
     */
    public int getStrikeInterval(int currentWave) {
        return Math.max(20, 60 - currentWave * 5);
    }
    
    /**
     * Lay text mo ta ly do that bai
     */
    public String getFailureReasonText(TribulationResult result) {
        return switch (result) {
            case FAIL_DEATH -> "Bị thiên lôi tiêu diệt";
            case FAIL_ANSWER -> "Tâm tính không vững";
            case FAIL_TIMEOUT -> "Hết thời gian";
            default -> "Không rõ";
        };
    }
    
    /**
     * Random value trong khoang [min, max]
     */
    public double random(double min, double max) {
        return min + Math.random() * (max - min);
    }
    
    /**
     * Tinh thoi gian prepare phase (tick)
     */
    public int getPrepareDuration() {
        return 60; // 3 giay
    }
    
    /**
     * Tinh thoi gian question phase (tick)
     */
    public int getQuestionDuration() {
        return 100; // 5 giay
    }
    
    /**
     * Tinh thoi gian finish phase (tick)
     */
    public int getFinishDuration() {
        return 40; // 2 giay
    }
    
    /**
     * Tính damage của thiên lôi theo wave và tỷ lệ thành công
     * @param wave Wave hiện tại (1-9)
     * @param maxWaves Tổng số wave
     * @param successRate Tỷ lệ thành công breakthrough (0-100)
     * @param maxHP Max HP của player
     * @return Damage (số HP bị trừ)
     * 
     * Formula:
     * - Base damage = % maxHP (tăng theo wave)
     * - Success rate modifier: Tỷ lệ thành công cao → damage thấp hơn
     * - Wave 1: 2-3% maxHP
     * - Wave cuối: 8-12% maxHP
     */
    public double calculateTribulationDamage(int wave, int maxWaves, double successRate, double maxHP) {
        // Base damage % tăng theo wave (2% -> 12%)
        double basePercent = 2.0 + (wave * (10.0 / maxWaves));
        
        // Success rate modifier: Tỷ lệ thành công cao → damage giảm
        // Success rate 100% → damage giảm 50%
        // Success rate 0% → damage tăng 50%
        double successModifier = 1.5 - (successRate / 100.0); // 0.5 - 1.5
        
        // Random variation (0.8 - 1.2)
        double randomFactor = 0.8 + (Math.random() * 0.4);
        
        // Final damage
        double damagePercent = basePercent * successModifier * randomFactor;
        double damage = maxHP * (damagePercent / 100.0);
        
        return Math.max(1.0, damage); // Min 1 HP
    }
}
