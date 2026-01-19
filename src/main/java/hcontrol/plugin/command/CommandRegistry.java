package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.master.MasterManager;
import hcontrol.plugin.master.MasterService;
import hcontrol.plugin.sect.SectManager;
import hcontrol.plugin.sect.SectService;
import hcontrol.plugin.ui.skill.SkillMenuGUI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * COMMAND REGISTRY - Đăng ký commands tự động
 * Đơn giản hóa việc thêm command mới
 * 
 * Lưu ý: Đặt trong package command (không phải core) vì import Bukkit API
 */
public class CommandRegistry {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private final CoreContext coreContext;
    
    public CommandRegistry(JavaPlugin plugin, CoreContext coreContext) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.coreContext = coreContext;
    }
    
    /**
     * Đăng ký một command với executor
     */
    public void register(String commandName, Supplier<CommandExecutor> executorSupplier) {
        register(commandName, executorSupplier, null);
    }
    
    /**
     * Đăng ký một command với executor và tab completer
     */
    public void register(String commandName, Supplier<CommandExecutor> executorSupplier, Supplier<TabCompleter> tabCompleterSupplier) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command == null) {
            logger.warning("Command '" + commandName + "' không được tìm thấy trong plugin.yml!");
            return;
        }
        
        CommandExecutor executor = executorSupplier.get();
        command.setExecutor(executor);
        
        if (tabCompleterSupplier != null) {
            TabCompleter completer = tabCompleterSupplier.get();
            if (completer != null) {
                command.setTabCompleter(completer);
            }
        } else if (executor instanceof TabCompleter completer) {
            // Nếu executor cũng implement TabCompleter, tự động dùng nó
            command.setTabCompleter(completer);
        }
    }
    
    /**
     * Đăng ký tất cả commands
     */
    public void registerAll() {
        logger.info("[PHASE 0] Đang đăng ký commands...");
        
        // HControl main command
        register("hc", () -> new ReloadCommand(coreContext.getLifecycleManager()));
        
        // Tu vi command
        register("tuvi", () -> new TuviCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getPlayerContext().getLevelService()
        ));
        
        // Stat command
        register("stat", () -> new StatCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getPlayerContext().getStatService()
        ));
        
        // Dokiep (breakthrough) command
        register("dokiep", () -> new BreakthroughCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getCultivationContext().getBreakthroughService(),
            coreContext.getUIContext()
        ));
        
        // Ascension command - ENDGAME
        register("ascend", () -> new AscensionCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getCultivationContext().getAscensionService()
        ));
        
        // World Boss command - ENDGAME
        register("worldboss", () -> new WorldBossCommand());
        
        // Spawn boss command
        register("spawnboss", () -> new SpawnBossCommand(
            coreContext.getEntityContext().getBossManager()
        ));
        
        // Zone command (admin)
        register("zone", () -> new ZoneCommand(
            coreContext.getEntityContext().getZoneManager()
        ));
        
        // Title command
        register("title", () -> new TitleCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getCultivationContext().getTitleService()
        ));
        
        // Unlock command
        register("unlock", () -> new UnlockCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getPlayerContext().getLevelService()
        ));
        
        // Role command (user)
        register("role", () -> new RoleCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getCultivationContext().getRoleService()
        ));
        
        // SetRole command (admin)
        register("setrole", () -> new SetRoleCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getCultivationContext().getRoleService()
        ));
        
        // AI Debug command (PHASE 7 testing)
        register("aidebug", () -> new AIDebugCommand());
        
        // Item Test command (PHASE 8A testing)
        // Lazy load ItemService trong command để tránh null khi command đăng ký trước ItemContext init
        register("itemtest", () -> new ItemTestCommand(
            coreContext.getPlayerContext().getPlayerManager()
        ));
        
        // Class command (PHASE 5)
        // Lazy load ClassService trong command để tránh null khi command đăng ký trước ClassContext init
        register("class", () -> new ClassCommand(
            coreContext.getPlayerContext().getPlayerManager()
        ));
        
        // Chat Bubble Test command
        register("bubbletest", () -> new ChatBubbleTestCommand(
            coreContext.getPlayerContext().getPlayerManager()
        ));
        
        logger.info("[PHASE 0] ✓ Commands đã được đăng ký!");
    }
    
    /**
     * SECT SYSTEM: Register sect command
     * Gọi riêng sau khi SectManager đã được init
     */
    public void registerSectCommand(SectService sectService) {
        if (sectService == null) {
            logger.warning("[SECT] SectService chưa được init, skip sect command!");
            return;
        }
        
        SectCommand sectCommand = new SectCommand(
            sectService,
            coreContext.getPlayerContext().getPlayerManager()
        );
        
        PluginCommand command = plugin.getCommand("sect");
        if (command != null) {
            command.setExecutor(sectCommand);
            command.setTabCompleter(sectCommand);
            logger.info("[SECT] ✓ Sect command đã được đăng ký!");
        }
    }
    
    /**
     * MASTER SYSTEM: Register master command
     */
    public void registerMasterCommand(MasterService masterService) {
        if (masterService == null) {
            logger.warning("[MASTER] MasterService chưa được init, skip master command!");
            return;
        }
        
        MasterCommand masterCommand = new MasterCommand(
            masterService,
            coreContext.getPlayerContext().getPlayerManager()
        );
        
        PluginCommand command = plugin.getCommand("master");
        if (command != null) {
            command.setExecutor(masterCommand);
            command.setTabCompleter(masterCommand);
            logger.info("[MASTER] ✓ Master command đã được đăng ký!");
        }
    }
    
    /**
     * PHASE 6: Register Skill command
     * Gọi riêng sau khi PlayerSkillService đã được init
     * @return SkillMenuGUI để đăng ký listener
     */
    public SkillMenuGUI registerSkillCommand() {
        // Check if skill service is ready
        if (coreContext.getPlayerContext().getSkillService() == null) {
            logger.warning("[PHASE 6] SkillService chưa được init, skip skill command!");
            return null;
        }
        
        // Tạo SkillMenuGUI
        SkillMenuGUI menuGUI = new SkillMenuGUI(coreContext.getPlayerContext().getSkillService());
        
        // Tạo SkillCommand và inject MenuGUI
        SkillCommand skillCommand = new SkillCommand(
            coreContext.getPlayerContext().getPlayerManager(),
            coreContext.getPlayerContext().getSkillService()
        );
        skillCommand.setMenuGUI(menuGUI);
        
        // Register command
        PluginCommand command = plugin.getCommand("skill");
        if (command != null) {
            command.setExecutor(skillCommand);
            command.setTabCompleter(skillCommand);
        }
        
        logger.info("[PHASE 6] ✓ Skill command + GUI đã được đăng ký!");
        return menuGUI;
    }
}
