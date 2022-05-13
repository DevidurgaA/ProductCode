package com.tlc.crm.common.config;

import java.util.List;

/**
 * @author Abishek
 * @version 1.0
 */
public record AuditEntry(Long id, AuditAction action, List<AuditAttribute> attributes)
{
}
