package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.sect.Sect;
import hcontrol.plugin.sect.SectManager;
import hcontrol.plugin.sect.SectMember;
import hcontrol.plugin.sect.SectRank;
import hcontrol.plugin.master.MasterManager;
import hcontrol.plugin.master.MasterRelation;
import hcontrol.plugin.master.DiscipleInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HCONTROL ADMIN COMMAND
 * Backdoor tổng hợp cho tất cả hệ thống
 */
public class HControlAdminCommand implements CommandExecutor, TabCompleter {
    
    private static final String PERMISSION = "hcontrol.admin";
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            sender.sendMessage("§c[HControl] Không có quyền!");
            return true;
        }
        
        if (args.length == 0) {
            sendMainHelp(sender);
            return true;
        }
        
        String category = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (category) {
            case "player", "p" -> handlePlayer(sender, subArgs);
            case "realm", "r" -> handleRealm(sender, subArgs);
            case "sect", "s" -> handleSect(sender, subArgs);
            case "master", "m" -> handleMaster(sender, subArgs);
            case "reload" -> handleReload(sender);
            case "stats" -> handleStats(sender);
            case "help" -> sendMainHelp(sender);
            default -> sendMainHelp(sender);
        }
        
        return true;
    }
    
    // ===== PLAYER COMMANDS =====
    
    private void handlePlayer(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendPlayerHelp(sender);
            return;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "info" -> playerInfo(sender, args);
            case "setlevel" -> playerSetLevel(sender, args);
            case "settuvi" -> playerSetTuvi(sender, args);
            case "addtuvi" -> playerAddTuvi(sender, args);
            case "heal" -> playerHeal(sender, args);
            case "reset" -> playerReset(sender, args);
            case "list" -> playerList(sender);
            default -> sendPlayerHelp(sender);
        }
    }
    
    private void playerInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin player info <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player: " + args[1]);
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage("§cKhông tìm thấy profile!");
            return;
        }
        
        sender.sendMessage("§6═══════ PLAYER INFO ═══════");
        sender.sendMessage("§eName: §f" + target.getName());
        sender.sendMessage("§eUUID: §7" + target.getUniqueId());
        sender.sendMessage("§eRealm: " + profile.getRealm().getColor() + profile.getRealm().getDisplayName());
        sender.sendMessage("§eLevel: §f" + profile.getRealmLevel() + "/" + profile.getRealm().getMaxLevelInRealm());
        sender.sendMessage("§eTu Vi: §d" + profile.getCultivation());
        sender.sendMessage("§eSkills Learned: §a" + profile.getLearnedSkills().size());
        sender.sendMessage("§6═══════════════════════════");
    }
    
    private void playerSetLevel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin player setlevel <player> <level>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLevel không hợp lệ!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        profile.setRealmLevel(level);
        sender.sendMessage("§a[Admin] Set level của §f" + target.getName() + " §a= §e" + level);
    }
    
    private void playerSetTuvi(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin player settuvi <player> <tuvi>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        long tuvi;
        try {
            tuvi = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cTu vi không hợp lệ!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        profile.setCultivation(tuvi);
        sender.sendMessage("§a[Admin] Set tu vi của §f" + target.getName() + " §a= §d" + tuvi);
    }
    
    private void playerAddTuvi(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin player addtuvi <player> <amount>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount không hợp lệ!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        profile.addCultivation(amount);
        sender.sendMessage("§a[Admin] Added §d" + amount + " tu vi §acho §f" + target.getName());
    }
    
    private void playerHeal(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin player heal <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        sender.sendMessage("§a[Admin] Đã heal §f" + target.getName() + " §ađầy máu!");
    }
    
    private void playerReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin player reset <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        // Reset to default
        profile.setRealm(CultivationRealm.PHAMNHAN);
        profile.setRealmLevel(1);
        profile.setCultivation(0);
        profile.getLearnedSkills().clear();
        
        sender.sendMessage("§a[Admin] Đã reset §f" + target.getName() + " §avề Phàm Nhân lv1!");
    }
    
    private void playerList(CommandSender sender) {
        Collection<PlayerProfile> online = getPlayerManager().getAllOnline();
        
        sender.sendMessage("§6═══════ ONLINE PLAYERS (" + online.size() + ") ═══════");
        for (PlayerProfile p : online) {
            Player player = Bukkit.getPlayer(p.getUuid());
            String name = player != null ? player.getName() : p.getUuid().toString();
            
            sender.sendMessage("§e" + name + " §7- " + 
                p.getRealm().getColor() + p.getRealm().getShortName() + 
                " §fLv" + p.getRealmLevel());
        }
        sender.sendMessage("§6═══════════════════════════════════");
    }
    
    // ===== REALM COMMANDS =====
    
    private void handleRealm(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendRealmHelp(sender);
            return;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "set" -> realmSet(sender, args);
            case "up" -> realmUp(sender, args);
            case "down" -> realmDown(sender, args);
            case "max" -> realmMax(sender, args);
            case "list" -> realmList(sender);
            default -> sendRealmHelp(sender);
        }
    }
    
    private void realmSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin realm set <player> <realm>");
            sender.sendMessage("§7Realms: PHAMNHAN, LUYENKHI, TRUCCO, KIMDAN, NGUYENANH, HOATHAN...");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        CultivationRealm realm;
        try {
            realm = CultivationRealm.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cRealm không hợp lệ: " + args[2]);
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        profile.setRealm(realm);
        profile.setRealmLevel(1);
        
        sender.sendMessage("§a[Admin] Set realm của §f" + target.getName() + " §a= " + realm.getColor() + realm.getDisplayName());
        target.sendMessage("§6[System] Cảnh giới đã đổi thành: " + realm.getColor() + realm.getDisplayName());
    }
    
    private void realmUp(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin realm up <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        CultivationRealm next = profile.getRealm().getNext();
        if (next == null) {
            sender.sendMessage("§c" + target.getName() + " đã ở cảnh giới cao nhất!");
            return;
        }
        
        profile.setRealm(next);
        profile.setRealmLevel(1);
        
        sender.sendMessage("§a[Admin] Đột phá §f" + target.getName() + " §alên " + next.getColor() + next.getDisplayName());
        target.sendMessage("§6[System] ĐỘT PHÁ THÀNH CÔNG! " + next.getColor() + next.getDisplayName());
    }
    
    private void realmDown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin realm down <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        CultivationRealm prev = profile.getRealm().getPrevious();
        if (prev == null) {
            sender.sendMessage("§c" + target.getName() + " đã ở cảnh giới thấp nhất!");
            return;
        }
        
        profile.setRealm(prev);
        profile.setRealmLevel(prev.getMaxLevelInRealm());
        
        sender.sendMessage("§a[Admin] Hạ §f" + target.getName() + " §axuống " + prev.getColor() + prev.getDisplayName());
    }
    
    private void realmMax(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin realm max <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        PlayerProfile profile = getPlayerManager().get(target.getUniqueId());
        if (profile == null) return;
        
        profile.setRealm(CultivationRealm.CHANTIEN);
        profile.setRealmLevel(10);
        profile.setCultivation(Long.MAX_VALUE / 2);
        
        sender.sendMessage("§a[Admin] Set §f" + target.getName() + " §alên §6§lCHÂN TIÊN MAX!");
        target.sendMessage("§6§l[System] BẠN ĐÃ ĐẠT CHÂN TIÊN CẢNH!");
    }
    
    private void realmList(CommandSender sender) {
        sender.sendMessage("§6═══════ CULTIVATION REALMS ═══════");
        for (CultivationRealm r : CultivationRealm.values()) {
            sender.sendMessage(r.getColor() + r.name() + " §7- " + r.getDisplayName() + 
                " §7(Dmg: " + (int)r.getBaseDamage() + ")");
        }
        sender.sendMessage("§6═══════════════════════════════════");
    }
    
    // ===== SECT COMMANDS =====
    
    private void handleSect(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendSectHelp(sender);
            return;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "list" -> sectList(sender);
            case "info" -> sectInfo(sender, args);
            case "create" -> sectCreate(sender, args);
            case "delete" -> sectDelete(sender, args);
            case "addmember" -> sectAddMember(sender, args);
            case "removemember" -> sectRemoveMember(sender, args);
            default -> sendSectHelp(sender);
        }
    }
    
    private void sectList(CommandSender sender) {
        SectManager manager = getSectManager();
        if (manager == null) {
            sender.sendMessage("§cSect system chưa được khởi tạo!");
            return;
        }
        
        Collection<Sect> sects = manager.getAllSects();
        
        if (sects.isEmpty()) {
            sender.sendMessage("§7Chưa có môn phái nào.");
            return;
        }
        
        sender.sendMessage("§6═══════ SECTS (" + sects.size() + ") ═══════");
        for (Sect s : sects) {
            sender.sendMessage("§e" + s.getName() + " §7[Lv" + s.getLevel() + "] §fMembers: " + s.getMemberCount() + "/" + s.getMaxMembers());
        }
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    private void sectInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin sect info <sect_id>");
            return;
        }
        
        SectManager manager = getSectManager();
        if (manager == null) return;
        
        Sect sect = manager.getSect(args[1]);
        if (sect == null) {
            sender.sendMessage("§cKhông tìm thấy môn phái: " + args[1]);
            return;
        }
        
        sender.sendMessage("§6═══════ SECT INFO ═══════");
        sender.sendMessage("§eName: §f" + sect.getName());
        sender.sendMessage("§eID: §7" + sect.getSectId());
        sender.sendMessage("§eLevel: §f" + sect.getLevel());
        sender.sendMessage("§eMembers: §f" + sect.getMemberCount() + "/" + sect.getMaxMembers());
        sender.sendMessage("§eTreasury: §e" + sect.getTreasury() + " Linh Thạch");
        
        // Members list
        sender.sendMessage("§7─────────────────────────────");
        for (SectMember m : sect.getAllMembers()) {
            sender.sendMessage("  §f" + m.getPlayerName() + " §7- " + m.getRank().getDisplayName());
        }
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    private void sectCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin sect create <id> <leader_player>");
            return;
        }
        
        SectManager manager = getSectManager();
        if (manager == null) return;
        
        Player leader = Bukkit.getPlayer(args[2]);
        if (leader == null) {
            sender.sendMessage("§cKhông tìm thấy player: " + args[2]);
            return;
        }
        
        String id = args[1].toLowerCase();
        String name = args[1]; // Use ID as name
        
        Sect sect = manager.createSect(id, name, leader.getUniqueId(), leader.getName());
        if (sect == null) {
            sender.sendMessage("§cKhông thể tạo môn phái! (ID đã tồn tại hoặc leader đã có môn phái)");
            return;
        }
        
        sender.sendMessage("§a[Admin] Đã tạo môn phái: §e" + name + " §avới chưởng môn §f" + leader.getName());
        leader.sendMessage("§6[System] Bạn đã trở thành Chưởng Môn của §e" + name + "§6!");
    }
    
    private void sectDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin sect delete <sect_id>");
            return;
        }
        
        SectManager manager = getSectManager();
        if (manager == null) return;
        
        boolean removed = manager.disbandSect(args[1]);
        if (removed) {
            sender.sendMessage("§a[Admin] Đã xóa môn phái: §e" + args[1]);
        } else {
            sender.sendMessage("§cKhông tìm thấy môn phái: " + args[1]);
        }
    }
    
    private void sectAddMember(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin sect addmember <sect_id> <player>");
            return;
        }
        
        SectManager manager = getSectManager();
        if (manager == null) return;
        
        Sect sect = manager.getSect(args[1]);
        if (sect == null) {
            sender.sendMessage("§cKhông tìm thấy môn phái!");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        boolean added = sect.addMember(target.getUniqueId(), target.getName(), SectRank.OUTER_DISCIPLE);
        if (added) {
            manager.saveData();
            sender.sendMessage("§a[Admin] Thêm §f" + target.getName() + " §avào §e" + sect.getName());
            target.sendMessage("§6[System] Bạn đã gia nhập môn phái §e" + sect.getName() + "§6!");
        } else {
            sender.sendMessage("§cKhông thể thêm (đã là thành viên hoặc môn phái đầy)!");
        }
    }
    
    private void sectRemoveMember(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /hadmin sect removemember <sect_id> <player>");
            return;
        }
        
        SectManager manager = getSectManager();
        if (manager == null) return;
        
        Sect sect = manager.getSect(args[1]);
        if (sect == null) {
            sender.sendMessage("§cKhông tìm thấy môn phái!");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        boolean removed = sect.removeMember(target.getUniqueId());
        if (removed) {
            manager.saveData();
            sender.sendMessage("§a[Admin] Đã xóa §f" + target.getName() + " §akhỏi §e" + sect.getName());
        } else {
            sender.sendMessage("§cKhông thể xóa (không phải thành viên hoặc là leader)!");
        }
    }
    
    // ===== MASTER COMMANDS =====
    
    private void handleMaster(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendMasterHelp(sender);
            return;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "list" -> masterList(sender);
            case "info" -> masterInfo(sender, args);
            default -> sendMasterHelp(sender);
        }
    }
    
    private void masterList(CommandSender sender) {
        MasterManager manager = getMasterManager();
        if (manager == null) {
            sender.sendMessage("§cMaster system chưa được khởi tạo!");
            return;
        }
        
        Collection<MasterRelation> masters = manager.getAllMasters();
        
        if (masters.isEmpty()) {
            sender.sendMessage("§7Chưa có sư phụ nào.");
            return;
        }
        
        sender.sendMessage("§6═══════ MASTERS (" + masters.size() + ") ═══════");
        for (MasterRelation m : masters) {
            sender.sendMessage("§e" + m.getMasterName() + " §7- Đệ tử: " + m.getDiscipleCount() + "/" + m.getMaxDisciples() + 
                (m.isAcceptingDisciples() ? " §a[Open]" : " §c[Closed]"));
        }
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    private void masterInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /hadmin master info <player>");
            return;
        }
        
        MasterManager manager = getMasterManager();
        if (manager == null) return;
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player!");
            return;
        }
        
        // Check as master
        MasterRelation masterRel = manager.getMaster(target.getUniqueId());
        if (masterRel != null) {
            sender.sendMessage("§6═══════ MASTER INFO ═══════");
            sender.sendMessage("§eName: §f" + masterRel.getMasterName());
            sender.sendMessage("§eTitle: §f" + masterRel.getDisplayTitle());
            sender.sendMessage("§eDisciples: §f" + masterRel.getDiscipleCount() + "/" + masterRel.getMaxDisciples());
            sender.sendMessage("§eAccepting: " + (masterRel.isAcceptingDisciples() ? "§aYes" : "§cNo"));
            sender.sendMessage("§6═══════════════════════════════");
        } else {
            sender.sendMessage("§7" + target.getName() + " không phải sư phụ.");
        }
        
        // Check as disciple
        DiscipleInfo discipleInfo = manager.getDisciple(target.getUniqueId());
        if (discipleInfo != null && discipleInfo.hasMaster()) {
            sender.sendMessage("§e" + target.getName() + " §7là đệ tử của §6" + discipleInfo.getMasterName());
        }
    }
    
    // ===== RELOAD & STATS =====
    
    private void handleReload(CommandSender sender) {
        sender.sendMessage("§e[Admin] Đang reload data...");
        
        try {
            // Reload skill templates
            var templateRegistry = CoreContext.getInstance().getSkillTemplateRegistry();
            if (templateRegistry != null) {
                templateRegistry.reload();
                sender.sendMessage("§a  ✓ SkillTemplateRegistry");
            }
            
            // Reload skill instances
            var instanceManager = CoreContext.getInstance().getSkillInstanceManager();
            if (instanceManager != null) {
                instanceManager.reload();
                sender.sendMessage("§a  ✓ SkillInstanceManager");
            }
            
            sender.sendMessage("§a[Admin] Reload hoàn tất!");
            
        } catch (Exception e) {
            sender.sendMessage("§c[Admin] Lỗi khi reload: " + e.getMessage());
        }
    }
    
    private void handleStats(CommandSender sender) {
        sender.sendMessage("§6═══════ HCONTROL STATS ═══════");
        
        // Players
        PlayerManager pm = getPlayerManager();
        if (pm != null) {
            sender.sendMessage("§ePlayers Online: §f" + pm.getAllOnline().size());
        }
        
        // Sects
        SectManager sm = getSectManager();
        if (sm != null) {
            sender.sendMessage("§eSects: §f" + sm.getAllSects().size());
        }
        
        // Masters
        MasterManager mm = getMasterManager();
        if (mm != null) {
            sender.sendMessage("§eMasters: §f" + mm.getAllMasters().size());
        }
        
        // Skills
        var tr = CoreContext.getInstance().getSkillTemplateRegistry();
        if (tr != null) {
            sender.sendMessage("§eSkill Templates: §f" + tr.getTemplateCount());
        }
        
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    // ===== HELP MESSAGES =====
    
    private void sendMainHelp(CommandSender sender) {
        sender.sendMessage("§6═══════ HCONTROL ADMIN ═══════");
        sender.sendMessage("§e/hadmin player <sub> §7- Quản lý player");
        sender.sendMessage("§e/hadmin realm <sub> §7- Quản lý cảnh giới");
        sender.sendMessage("§e/hadmin sect <sub> §7- Quản lý môn phái");
        sender.sendMessage("§e/hadmin master <sub> §7- Quản lý sư đồ");
        sender.sendMessage("§e/hadmin reload §7- Reload data");
        sender.sendMessage("§e/hadmin stats §7- Thống kê");
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    private void sendPlayerHelp(CommandSender sender) {
        sender.sendMessage("§6═══ PLAYER COMMANDS ═══");
        sender.sendMessage("§e/hadmin player info <player>");
        sender.sendMessage("§e/hadmin player setlevel <player> <level>");
        sender.sendMessage("§e/hadmin player settuvi <player> <tuvi>");
        sender.sendMessage("§e/hadmin player addtuvi <player> <amount>");
        sender.sendMessage("§e/hadmin player heal <player>");
        sender.sendMessage("§e/hadmin player reset <player>");
        sender.sendMessage("§e/hadmin player list");
    }
    
    private void sendRealmHelp(CommandSender sender) {
        sender.sendMessage("§6═══ REALM COMMANDS ═══");
        sender.sendMessage("§e/hadmin realm set <player> <realm>");
        sender.sendMessage("§e/hadmin realm up <player>");
        sender.sendMessage("§e/hadmin realm down <player>");
        sender.sendMessage("§e/hadmin realm max <player>");
        sender.sendMessage("§e/hadmin realm list");
    }
    
    private void sendSectHelp(CommandSender sender) {
        sender.sendMessage("§6═══ SECT COMMANDS ═══");
        sender.sendMessage("§e/hadmin sect list");
        sender.sendMessage("§e/hadmin sect info <sect_id>");
        sender.sendMessage("§e/hadmin sect create <id> <leader>");
        sender.sendMessage("§e/hadmin sect delete <sect_id>");
        sender.sendMessage("§e/hadmin sect addmember <sect_id> <player>");
        sender.sendMessage("§e/hadmin sect removemember <sect_id> <player>");
    }
    
    private void sendMasterHelp(CommandSender sender) {
        sender.sendMessage("§6═══ MASTER COMMANDS ═══");
        sender.sendMessage("§e/hadmin master list");
        sender.sendMessage("§e/hadmin master info <player>");
    }
    
    // ===== HELPERS =====
    
    private PlayerManager getPlayerManager() {
        try {
            return CoreContext.getInstance().getPlayerManager();
        } catch (Exception e) {
            return null;
        }
    }
    
    private SectManager getSectManager() {
        try {
            return CoreContext.getInstance().getSectManager();
        } catch (Exception e) {
            return null;
        }
    }
    
    private MasterManager getMasterManager() {
        try {
            return CoreContext.getInstance().getMasterManager();
        } catch (Exception e) {
            return null;
        }
    }
    
    // ===== TAB COMPLETE =====
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            return Collections.emptyList();
        }
        
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("player", "realm", "sect", "master", "reload", "stats", "help"));
        } else if (args.length == 2) {
            String cat = args[0].toLowerCase();
            switch (cat) {
                case "player", "p" -> suggestions.addAll(Arrays.asList("info", "setlevel", "settuvi", "addtuvi", "heal", "reset", "list"));
                case "realm", "r" -> suggestions.addAll(Arrays.asList("set", "up", "down", "max", "list"));
                case "sect", "s" -> suggestions.addAll(Arrays.asList("list", "info", "create", "delete", "addmember", "removemember"));
                case "master", "m" -> suggestions.addAll(Arrays.asList("list", "info"));
            }
        } else if (args.length >= 3) {
            String cat = args[0].toLowerCase();
            String sub = args[1].toLowerCase();
            
            // Player suggestions
            if ((cat.equals("player") || cat.equals("p") || cat.equals("realm") || cat.equals("r") 
                || cat.equals("master") || cat.equals("m")) && args.length == 3) {
                if (!sub.equals("list")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        suggestions.add(p.getName());
                    }
                }
            }
            
            // Realm suggestions
            if ((cat.equals("realm") || cat.equals("r")) && args.length == 4 && sub.equals("set")) {
                for (CultivationRealm r : CultivationRealm.values()) {
                    suggestions.add(r.name());
                }
            }
            
            // Sect suggestions
            if ((cat.equals("sect") || cat.equals("s"))) {
                SectManager sm = getSectManager();
                if (args.length == 3 && (sub.equals("info") || sub.equals("delete") || sub.equals("addmember") || sub.equals("removemember"))) {
                    if (sm != null) {
                        for (Sect s : sm.getAllSects()) {
                            suggestions.add(s.getSectId());
                        }
                    }
                } else if (args.length == 4 && (sub.equals("addmember") || sub.equals("removemember") || sub.equals("create"))) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        suggestions.add(p.getName());
                    }
                }
            }
        }
        
        String lastArg = args[args.length - 1].toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lastArg))
            .collect(Collectors.toList());
    }
}
