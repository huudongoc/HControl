package hcontrol.plugin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hcontrol.plugin.model.DeathContext;

/**
 * DEATH MESSAGE SERVICE
 * Presentation layer - format death message tu config
 */
public class DeathMessageService {
    
    private final DeathMessageConfig config;
    private final Random random;
    private final DisplayFormatService displayFormatService;
    
    public DeathMessageService(DeathMessageConfig config) {
        this.config = config;
        this.random = new Random();
        this.displayFormatService = DisplayFormatService.getInstance();
    }
    
    /**
     * Build death message tu DeathContext
     */
    public String buildMessage(DeathContext ctx) {
        // DEBUG LOG
        // System.out.println("[DEBUG] DeathMessageService - DeathType: " + ctx.getType());
        // System.out.println("[DEBUG] DeathMessageService - KillerName: " + ctx.getKillerName());
        // System.out.println("[DEBUG] DeathMessageService - WeaponName: " + ctx.getWeaponName());
        
        // Lay templates cho DeathType
        List<String> templates = config.getTemplates(ctx.getType());
        
        if (templates.isEmpty()) {
            // Fallback neu khong co template
            return ctx.getVictimName() + " đã tử vong";
        }
        
        // Filter templates phu hop voi context
        List<String> suitableTemplates = filterSuitableTemplates(templates, ctx);
        
        // DEBUG LOG
        // System.out.println("[DEBUG] Total templates: " + templates.size());
        // System.out.println("[DEBUG] Suitable templates: " + suitableTemplates.size());
        
        // Neu khong co template phu hop, dung tat ca templates
        if (suitableTemplates.isEmpty()) {
            suitableTemplates = templates;
        }
        
        // Chon random template tu danh sach phu hop
        String raw = suitableTemplates.get(random.nextInt(suitableTemplates.size()));
        
        // DEBUG LOG
       // System.out.println("[DEBUG] Selected template: " + raw);
        
        // Format message
        return format(raw, ctx);
    }
    
    /**
     * Filter templates phu hop voi context
     * - Neu co weapon: uu tien template co {weapon}
     * - Neu co killer: uu tien template co {killer} hoac {boss}
     * - Neu khong co killer/weapon: chi chon template khong co {killer}/{weapon}
     */
    private List<String> filterSuitableTemplates(List<String> templates, DeathContext ctx) {
        boolean hasKiller = ctx.getKillerName() != null && !ctx.getKillerName().isEmpty();
        boolean hasWeapon = ctx.getWeaponName() != null && !ctx.getWeaponName().isEmpty();
        
        List<String> suitable = new ArrayList<>();
        
        for (String template : templates) {
            boolean hasKillerPlaceholder = template.contains("{killer}") || template.contains("{boss}");
            boolean hasWeaponPlaceholder = template.contains("{weapon}");
            
            // Neu co weapon thi phai co {weapon} placeholder
            if (hasWeapon && !hasWeaponPlaceholder) {
                continue;
            }
            
            // Neu co killer thi phai co {killer} hoac {boss} placeholder
            if (hasKiller && !hasKillerPlaceholder) {
                continue;
            }
            
            // Neu khong co killer/weapon thi khong chon template co placeholder
            if (!hasKiller && hasKillerPlaceholder) {
                continue;
            }
            
            if (!hasWeapon && hasWeaponPlaceholder) {
                continue;
            }
            
            suitable.add(template);
        }
        
        return suitable;
    }
    
    /**
     * Format message template voi thong tin tu DeathContext
     */
    private String format(String raw, DeathContext ctx) {
        String result = raw;
        
        // Replace {player} voi ten player (co format nameplate neu can)
        String playerName = formatPlayerName(ctx);
        result = result.replace("{player}", playerName);
        
        // Replace {realm} voi ten realm
        result = result.replace("{realm}", ctx.getRealm().getDisplayName());
        
        // Replace {level} voi level
        result = result.replace("{level}", String.valueOf(ctx.getLevel()));
        
        // Replace {killer} voi ten killer (co format nameplate neu la player)
        String killerText = formatKillerName(ctx);
        result = result.replace("{killer}", killerText);
        
        // Replace {boss} voi ten boss (backward compatibility)
        result = result.replace("{boss}", killerText);
        
        // Replace {weapon} voi ten weapon (neu co)
        String weaponText = formatWeapon(ctx);
        result = result.replace("{weapon}", weaponText);
        
        // Replace {location} voi ten location (neu co)
        result = result.replace("{location}", safe(ctx.getLocationName()));
        
        // Clean up double spaces (neu co placeholder rong)
        result = result.replaceAll("\\s+", " ").trim();
        
        return result;
    }
    
    /**
     * Format weapon text - neu co weapon thi hien thi ten weapon
     */
    private String formatWeapon(DeathContext ctx) {
        String weaponName = ctx.getWeaponName();
        if (weaponName == null || weaponName.isEmpty()) {
            return "";
        }
        return weaponName;
    }
    
    /**
     * Format player name (co the them realm/level neu can)
     */
    private String formatPlayerName(DeathContext ctx) {
        // Format nameplate: [Realm Level] PlayerName
        return ctx.getVictimName() + " " + 
               ctx.getRealm().getColor() + "[" + 
               ctx.getRealm().getDisplayName() + " " + 
               ctx.getLevel() + "]";
    }
    
    /**
     * Format killer name - neu la player thi hien thi nameplate, neu la mob/boss thi chi hien thi ten
     */
    private String formatKillerName(DeathContext ctx) {
        // Neu co killerProfile (la player) thi format nameplate
        if (ctx.getKillerProfile() != null) {
            var killerProfile = ctx.getKillerProfile();
            return killerProfile.getName() + " " + 
                   killerProfile.getRealm().getColor() + "[" + 
                   killerProfile.getRealm().getDisplayName() + " " + 
                   killerProfile.getLevel() + "]";
        }
        
        // Neu khong phai player (mob/boss) thi chi hien thi ten
        return safe(ctx.getKillerName());
    }
    
    /**
     * Safe replace - neu null thi tra ve chuoi rong
     */
    private String safe(String value) {
        return value != null ? value : "";
    }
}