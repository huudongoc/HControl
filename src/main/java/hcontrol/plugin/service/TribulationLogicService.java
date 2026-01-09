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
}
