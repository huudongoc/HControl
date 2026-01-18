package hcontrol.plugin.event;

/**
 * Loại thay đổi trạng thái của player
 */
public enum PlayerStateChangeType {
    /**
     * Đột phá cảnh giới
     */
    REALM_CHANGE,
    
    /**
     * Gia nhập môn phái
     */
    SECT_JOIN,
    
    /**
     * Rời môn phái
     */
    SECT_LEAVE,
    
    /**
     * Thay đổi danh hiệu
     */
    TITLE_CHANGE,
    
    /**
     * Thay đổi quan hệ sư đồ (nhận đệ tử, bái sư, rời sư phụ)
     */
    MASTER_RELATION_CHANGE,
    
    /**
     * Thay đổi rank trong môn phái
     */
    SECT_RANK_CHANGE,
    
    /**
     * Level up trong cảnh giới
     */
    LEVEL_UP
}
