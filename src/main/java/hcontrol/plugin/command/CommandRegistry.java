package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
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
        
        // Spawn boss command
        register("spawnboss", () -> new SpawnBossCommand(
            coreContext.getEntityContext().getBossManager()
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
        
        logger.info("[PHASE 0] ✓ Commands đã được đăng ký!");
    }
}
