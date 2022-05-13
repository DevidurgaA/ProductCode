package com.tlc.sql.api.meta;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;


public enum TableType
{
    COMMON(false, false, false, false),
    COMMON_PARTITIONED_BY_ID(true,false, false, false),

    ORG_DEPENDENT(false, true, false, false),
    ORG_PARTITIONED_BY_ID(true, true, false, false),
    ORG_PARTITIONED_BY_ORG(false, true, true, false),

    ORG_MIXED(false, true, false, true),
    ORG_MIXED_BY_ID(true, true, false, true),
    ORG_MIXED_BY_ORG(false, true, true, true)

    ;
    private final boolean partitionById;
    private final boolean partitionByOrgId;

    private final boolean orgDependent;
    private final boolean commonNullExists;
    TableType(boolean partitionById, boolean orgDependent, boolean partitionByOrgId, boolean commonNullExists)
    {
        this.partitionById = partitionById;
        this.orgDependent = orgDependent;
        this.partitionByOrgId = partitionByOrgId;
        this.commonNullExists = commonNullExists;
    }

    public boolean isOrgDependent()
    {
        return orgDependent;
    }

    public boolean isCommonNullExists()
    {
        return commonNullExists;
    }

    public boolean isPartitionById()
    {
        return partitionById;
    }

    public boolean isPartitionByOrgId()
    {
        return partitionByOrgId;
    }

    public static TableType get(int type)
    {
        return switch (type)
                {
                    case 1 -> COMMON;
                    case 2 -> COMMON_PARTITIONED_BY_ID;
                    case 11 -> ORG_DEPENDENT;
                    case 12 -> ORG_PARTITIONED_BY_ID;
                    case 13 -> ORG_PARTITIONED_BY_ORG;
                    case 21 -> ORG_MIXED;
                    case 22 -> ORG_MIXED_BY_ID;
                    case 23 -> ORG_MIXED_BY_ORG;
                    default -> throw ErrorCode.getLite(ErrorCodes.INVALID_DATA, "Table Type : "+ type);
                };
    }
}
