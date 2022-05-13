package com.tlc.bootstrap.update;

public enum UpdateMode
{
    OFF,
    MANUAL,
    AUTO;
    static UpdateMode get(String mode)
    {
        if(mode != null)
        {
            final String modeLower = mode.toLowerCase();
            if(modeLower.equals("manual"))
            {
                return MANUAL;
            }
            else if (modeLower.equals("auto"))
            {
                return AUTO;
            }
        }
        return OFF;
    }
}
