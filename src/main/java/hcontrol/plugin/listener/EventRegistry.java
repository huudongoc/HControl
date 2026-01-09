package hcontrol.plugin.listener;

import org.bukkit.event.Listener;

/**
 * EVENT REGISTRY - Interface để đăng ký events
 * Đặt trong package listener vì cần import Bukkit API (Listener)
 * Core package sẽ sử dụng interface này nhưng không import Bukkit
 */
public interface EventRegistry {
    void registerEvents(Listener listener);
}
